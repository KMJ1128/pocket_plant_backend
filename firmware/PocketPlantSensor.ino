#include "DHT.h"
#include <WiFi.h>
#include <HTTPClient.h>
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

#define DHTPIN A4
#define DHTTYPE DHT11
#define LIGHTPIN A0
#define SOILPIN A1

static const char* SERVER_URL = "http://3.25.69.13:8080/api/sensor";
static const unsigned long SEND_INTERVAL_MS = 30000;
static const char* SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b";
static const char* CHARACTERISTIC_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8";

DHT dht(DHTPIN, DHTTYPE);
bool isBleProvisioned = false;
String targetSsid;
String targetPassword;
unsigned long lastSendAt = 0;

class MyCallbacks : public BLECharacteristicCallbacks {
  void onWrite(BLECharacteristic* characteristic) override {
    size_t length = characteristic->getLength();
    uint8_t* data = characteristic->getData();
    if (length == 0) return;

    String received;
    received.reserve(length);
    for (size_t index = 0; index < length; index++) {
      received += static_cast<char>(data[index]);
    }

    int separator = received.indexOf(":::");
    if (separator <= 0) {
      Serial.println("[BLE] Invalid Wi-Fi payload");
      return;
    }

    targetSsid = received.substring(0, separator);
    targetPassword = received.substring(separator + 3);
    isBleProvisioned = true;
  }
};

void setup() {
  Serial.begin(9600);
  dht.begin();
  WiFi.mode(WIFI_STA);
  WiFi.disconnect(true, true);

  BLEDevice::init("PocketPlant");
  BLEServer* server = BLEDevice::createServer();
  BLEService* service = server->createService(SERVICE_UUID);
  BLECharacteristic* characteristic = service->createCharacteristic(
    CHARACTERISTIC_UUID,
    BLECharacteristic::PROPERTY_WRITE
  );
  characteristic->setCallbacks(new MyCallbacks());
  service->start();

  BLEAdvertising* advertising = BLEDevice::getAdvertising();
  advertising->addServiceUUID(SERVICE_UUID);
  advertising->setScanResponse(true);
  advertising->setMinPreferred(0x06);
  advertising->setMinPreferred(0x12);
  BLEDevice::startAdvertising();

  Serial.println("[BLE] Waiting for Wi-Fi settings");
  while (!isBleProvisioned) delay(200);

  delay(3000);
  WiFi.begin(targetSsid.c_str(), targetPassword.c_str());

  int retry = 0;
  while (WiFi.status() != WL_CONNECTED && retry < 30) {
    delay(500);
    retry++;
  }

  if (WiFi.status() == WL_CONNECTED) {
    Serial.print("[Wi-Fi] Connected: ");
    Serial.println(WiFi.localIP());
    lastSendAt = millis() - SEND_INTERVAL_MS;
  } else {
    Serial.println("[Wi-Fi] Connection failed");
  }
}

void loop() {
  if (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    return;
  }

  unsigned long now = millis();
  if (now - lastSendAt < SEND_INTERVAL_MS) {
    delay(100);
    return;
  }
  lastSendAt = now;

  float humidity = dht.readHumidity();
  float temperature = dht.readTemperature();
  int lightValue = analogRead(LIGHTPIN);
  int soilValue = analogRead(SOILPIN);
  if (isnan(humidity) || isnan(temperature)) {
    Serial.println("[Sensor] DHT read failed");
    return;
  }

  String payload = "{\"macAddress\":\"" + WiFi.macAddress() + "\"";
  payload += ",\"temperature\":" + String(temperature);
  payload += ",\"humidity\":" + String(humidity);
  payload += ",\"light\":" + String(lightValue);
  payload += ",\"moisture\":" + String(soilValue) + "}";

  WiFiClient client;
  HTTPClient http;
  http.setConnectTimeout(5000);
  http.setTimeout(5000);

  if (!http.begin(client, SERVER_URL)) {
    Serial.println("[HTTP] Initialization failed");
    return;
  }

  http.addHeader("Content-Type", "application/json");
  int statusCode = http.POST(payload);

  if (statusCode >= 200 && statusCode < 300) {
    Serial.printf("[HTTP] Sensor saved: %d\n", statusCode);
  } else {
    Serial.printf("[HTTP] Sensor save failed: %d\n", statusCode);
    if (statusCode > 0) Serial.println(http.getString());
  }

  http.end();
}
