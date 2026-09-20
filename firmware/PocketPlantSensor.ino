#include "DHT.h"
#include <WiFi.h>
#include <HTTPClient.h>
#include <Preferences.h>
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>

#define DHTPIN A4
#define DHTTYPE DHT11
#define LIGHTPIN A0
#define SOILPIN A1
#define RELAY_PIN D6

const char* SERVER_BASE_URL = "http://3.25.69.13:8080";
const char* SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b";
const char* CHARACTERISTIC_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8";
const unsigned long SENSOR_INTERVAL_MS = 5000;
const unsigned long COMMAND_INTERVAL_MS = 5000;
const unsigned long WIFI_RETRY_INTERVAL_MS = 10000;
const unsigned long MAX_PUMP_RUNTIME_MS = 30000;
const int SOIL_DRY_ADC = 3200;
const int SOIL_WET_ADC = 1300;
const uint8_t RELAY_ON = LOW;
const uint8_t RELAY_OFF = HIGH;

DHT dht(DHTPIN, DHTTYPE);
Preferences preferences;
String targetSsid;
String targetPassword;
volatile bool provisioned = false;
unsigned long lastSensorAt = 0;
unsigned long lastCommandAt = 0;
unsigned long lastWifiRetryAt = 0;

class WifiProvisioningCallbacks : public BLECharacteristicCallbacks {
  void onWrite(BLECharacteristic* characteristic) override {
    size_t length = characteristic->getLength();
    uint8_t* data = characteristic->getData();
    if (length == 0) return;
    String received;
    received.reserve(length);
    for (size_t i = 0; i < length; i++) received += static_cast<char>(data[i]);
    int separator = received.indexOf(":::");
    if (separator <= 0) return;
    targetSsid = received.substring(0, separator);
    targetPassword = received.substring(separator + 3);
    preferences.putString("ssid", targetSsid);
    preferences.putString("password", targetPassword);
    provisioned = true;
  }
};

void startBleProvisioning() {
  BLEDevice::init("PocketPlant");
  BLEServer* server = BLEDevice::createServer();
  BLEService* service = server->createService(SERVICE_UUID);
  BLECharacteristic* characteristic = service->createCharacteristic(
    CHARACTERISTIC_UUID, BLECharacteristic::PROPERTY_WRITE
  );
  characteristic->setCallbacks(new WifiProvisioningCallbacks());
  service->start();
  BLEAdvertising* advertising = BLEDevice::getAdvertising();
  advertising->addServiceUUID(SERVICE_UUID);
  advertising->setScanResponse(true);
  BLEDevice::startAdvertising();
  while (!provisioned) {
    setPump(false);
    delay(200);
  }
  advertising->stop();
  delay(1500);
}

bool connectWifi() {
  if (targetSsid.isEmpty()) return false;
  WiFi.mode(WIFI_STA);
  WiFi.begin(targetSsid.c_str(), targetPassword.c_str());
  for (int i = 0; i < 30 && WiFi.status() != WL_CONNECTED; i++) delay(500);
  return WiFi.status() == WL_CONNECTED;
}

int readAveragedAdc(uint8_t pin) {
  long total = 0;
  for (int i = 0; i < 8; i++) { total += analogRead(pin); delay(5); }
  return static_cast<int>(total / 8);
}

float soilPercentFromRaw(int raw) {
  if (SOIL_DRY_ADC == SOIL_WET_ADC) return 0;
  return constrain((SOIL_DRY_ADC - raw) * 100.0f /
                   (SOIL_DRY_ADC - SOIL_WET_ADC), 0.0f, 100.0f);
}

long extractJsonLong(const String& json, const char* key) {
  String token = String('"') + key + "\":";
  int start = json.indexOf(token);
  if (start < 0) return -1;
  start += token.length();
  while (start < json.length() && json[start] == ' ') start++;
  int end = start;
  while (end < json.length() && isDigit(json[end])) end++;
  return end == start ? -1 : json.substring(start, end).toInt();
}

bool beginHttp(HTTPClient& http, WiFiClient& client, const String& url) {
  http.setConnectTimeout(5000);
  http.setTimeout(7000);
  return http.begin(client, url);
}

void sendSensorData() {
  float humidity = dht.readHumidity();
  float temperature = dht.readTemperature();
  if (isnan(humidity) || isnan(temperature)) return;
  int lightRaw = readAveragedAdc(LIGHTPIN);
  int soilRaw = readAveragedAdc(SOILPIN);
  float soilPercent = soilPercentFromRaw(soilRaw);
  String payload = "{\"macAddress\":\"" + WiFi.macAddress() + "\",\"temperature\":" + String(temperature, 1)
    + ",\"humidity\":" + String(humidity, 1) + ",\"light\":" + String(lightRaw)
    + ",\"moisture\":" + String(soilPercent, 1) + ",\"moistureRaw\":" + String(soilRaw) + "}";
  WiFiClient client;
  HTTPClient http;
  if (!beginHttp(http, client, String(SERVER_BASE_URL) + "/api/sensor")) return;
  http.addHeader("Content-Type", "application/json");
  Serial.printf("[HTTP] Sensor status=%d soil=%.1f%% raw=%d\n", http.POST(payload), soilPercent, soilRaw);
  http.end();
}

void setPump(bool on) { digitalWrite(RELAY_PIN, on ? RELAY_ON : RELAY_OFF); }

void reportResult(long id, bool success, const String& message) {
  WiFiClient client;
  HTTPClient http;
  String url = String(SERVER_BASE_URL) + "/api/device/" + WiFi.macAddress()
               + "/watering/" + String(id) + "/result";
  if (!beginHttp(http, client, url)) return;
  http.addHeader("Content-Type", "application/json");
  String payload = "{\"success\":" + String(success ? "true" : "false")
                   + ",\"message\":\"" + message + "\"}";
  http.POST(payload);
  http.end();
}

void pollWateringCommand() {
  WiFiClient client;
  HTTPClient http;
  String url = String(SERVER_BASE_URL) + "/api/device/" + WiFi.macAddress() + "/watering/next";
  if (!beginHttp(http, client, url)) return;
  int status = http.GET();
  if (status == 204) { http.end(); return; }
  if (status != 200) { http.end(); return; }
  String body = http.getString();
  http.end();
  long id = extractJsonLong(body, "commandId");
  long duration = extractJsonLong(body, "durationMs");
  if (id <= 0 || duration < 300 || duration > MAX_PUMP_RUNTIME_MS) {
    if (id > 0) reportResult(id, false, "invalid command");
    return;
  }
  if (preferences.getULong64("lastCommand", 0) == static_cast<uint64_t>(id)) {
    reportResult(id, true, "already completed");
    return;
  }
  if (preferences.putULong64("lastCommand", static_cast<uint64_t>(id)) == 0) {
    reportResult(id, false, "command persistence failed");
    return;
  }
  setPump(true);
  unsigned long startedAt = millis();
  while (millis() - startedAt < static_cast<unsigned long>(duration)) delay(10);
  setPump(false);
  reportResult(id, true, "completed");
}

void setup() {
  pinMode(RELAY_PIN, OUTPUT);
  setPump(false);
  Serial.begin(115200);
  dht.begin();
  analogReadResolution(12);
  preferences.begin("pocketplant", false);
  targetSsid = preferences.getString("ssid", "");
  targetPassword = preferences.getString("password", "");
  if (targetSsid.isEmpty() || !connectWifi()) { startBleProvisioning(); connectWifi(); }
  lastSensorAt = millis() - SENSOR_INTERVAL_MS;
  lastCommandAt = millis() - COMMAND_INTERVAL_MS;
  lastWifiRetryAt = millis();
}

void loop() {
  if (WiFi.status() != WL_CONNECTED) {
    setPump(false);
    unsigned long now = millis();
    if (now - lastWifiRetryAt >= WIFI_RETRY_INTERVAL_MS) {
      lastWifiRetryAt = now;
      WiFi.reconnect();
    }
    delay(100);
    return;
  }
  unsigned long now = millis();
  if (now - lastCommandAt >= COMMAND_INTERVAL_MS) { lastCommandAt = now; pollWateringCommand(); }
  if (now - lastSensorAt >= SENSOR_INTERVAL_MS) { lastSensorAt = now; sendSensorData(); }
  delay(50);
}
