package com.pocket_plant.backend.service;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;

class PlantDiseaseServiceTest {
    private PlantDiseaseService service(HttpServer server) {
        PlantDiseaseService service = new PlantDiseaseService();
        ReflectionTestUtils.setField(service, "fastApiUrl", "http://127.0.0.1:" + server.getAddress().getPort() + "/predict");
        ReflectionTestUtils.setField(service, "fastApiToken", "test-only-secret");
        ReflectionTestUtils.setField(service, "connectTimeoutMs", 1000);
        ReflectionTestUtils.setField(service, "readTimeoutMs", 2000);
        return service;
    }

    @Test
    void forwardsAuthenticationAndRetainsCandidatesWithoutProbability() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/predict", exchange -> {
            if (!"test-only-secret".equals(exchange.getRequestHeaders().getFirst("X-Model-Token"))
                || !"PocketPlant-Backend/1.0".equals(exchange.getRequestHeaders().getFirst("User-Agent"))) {
                exchange.sendResponseHeaders(401, -1); exchange.close(); return;
            }
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            if (!body.contains("name=\"image\"") || !body.contains("SnakePlant")) {
                exchange.sendResponseHeaders(422, -1); exchange.close(); return;
            }
            byte[] payload = ("{\"disease_symptom\":\"candidate\",\"confidence\":null,\"status\":\"experimental\","
                + "\"score_kind\":\"relative_model_score\",\"normal_result_blocked\":false,"
                + "\"candidates\":[{\"name\":\"candidate\",\"comparison_score\":0.6,\"features\":\"spots\",\"actions\":\"inspect\"}]}").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, payload.length);
            exchange.getResponseBody().write(payload); exchange.close();
        });
        server.start();
        try {
            var result = service(server).predictDisease(new MockMultipartFile("image", "leaf.png", "image/png", new byte[]{1}), "SnakePlant");
            assertNull(result.getConfidence());
            assertEquals("relative_model_score", result.getScoreKind());
            assertEquals(.6, result.getCandidates().getFirst().getComparisonScore());
            assertEquals("inspect", result.getCandidates().getFirst().getActions());
        } finally { server.stop(0); }
    }

    @Test
    void invalidSpeciesResponseRemainsInputError() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/predict", exchange -> { exchange.sendResponseHeaders(422, -1); exchange.close(); });
        server.start();
        try {
            var error = assertThrows(PlantDiseaseService.DiseaseInputException.class,
                () -> service(server).predictDisease(new MockMultipartFile("image", new byte[]{1}), "stuki"));
            assertEquals(422, error.getStatus());
        } finally { server.stop(0); }
    }
}
