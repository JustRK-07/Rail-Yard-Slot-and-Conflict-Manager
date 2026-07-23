package com.justrk07.railyard.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.justrk07.railyard.TestcontainersConfiguration;
import com.justrk07.railyard.common.web.CorrelationIdFilter;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class MasterDataApiIntegrationTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CorrelationIdFilter correlationIdFilter;

    private MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvc() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(correlationIdFilter)
                .build();
    }

    @Test
    void createsYardReturnsCorrelationIdAndSupportsGet() throws Exception {
        String body = """
                {
                  "code": "APX-1",
                  "name": "Alpha Yard",
                  "location": "Pune",
                  "timeZone": "Asia/Kolkata"
                }
                """;
        String response = mockMvc.perform(post("/api/yards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.code").value("APX-1"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String id = extractId(response);

        mockMvc.perform(get("/api/yards/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alpha Yard"));
    }

    @Test
    void duplicateYardCodeReturns409WithApiErrorEnvelope() throws Exception {
        String body = """
                {
                  "code": "DUP-1",
                  "name": "First",
                  "location": "Pune",
                  "timeZone": "Asia/Kolkata"
                }
                """;
        mockMvc.perform(post("/api/yards").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/yards").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"))
                .andExpect(jsonPath("$.correlationId").isNotEmpty());
    }

    @Test
    void missingYardReturns404WithApiErrorEnvelope() throws Exception {
        UUID id = new java.util.UUID(0L, 1L);
        mockMvc.perform(get("/api/yards/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void invalidYardPayloadReturnsValidationFailed() throws Exception {
        String body = """
                {
                  "code": "",
                  "name": "",
                  "location": "",
                  "timeZone": ""
                }
                """;
        mockMvc.perform(post("/api/yards").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void createsTrackInYardWithCapabilities() throws Exception {
        String yardId = createYard("TR-YARD", "Track Yard", "Mumbai", "Asia/Kolkata");
        String trackBody = """
                {
                  "code": "T01",
                  "usableLengthMeters": 900,
                  "purpose": "STAGING",
                  "status": "OPERATIONAL",
                  "setupBufferMinutes": 10,
                  "clearanceBufferMinutes": 10,
                  "capabilities": ["DIESEL", "HEAVY_FREIGHT"]
                }
                """;
        mockMvc.perform(post("/api/yards/{yardId}/tracks", yardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(trackBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.yardId").value(yardId))
                .andExpect(jsonPath("$.capabilities", org.hamcrest.Matchers.containsInAnyOrder("DIESEL", "HEAVY_FREIGHT")));
    }

    @Test
    void trackStatusPatchRejectsInvalidValue() throws Exception {
        String yardId = createYard("TR-YARD-2", "Track Yard 2", "Mumbai", "Asia/Kolkata");
        String trackId = createTrack(yardId, "T02");
        mockMvc.perform(patch("/api/tracks/{id}/status", trackId).param("status", "BOGUS"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createsAndFindsTrainByNumber() throws Exception {
        String body = """
                {
                  "trainNumber": "TR-101",
                  "lengthMeters": 700,
                  "serviceType": "FREIGHT",
                  "priority": 2,
                  "origin": "Pune",
                  "destination": "Delhi",
                  "requiredCapabilities": ["DIESEL", "HEAVY_FREIGHT"]
                }
                """;
        mockMvc.perform(post("/api/trains").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.trainNumber").value("TR-101"));

        mockMvc.perform(get("/api/trains").param("query", "tr-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].trainNumber").value("TR-101"));
    }

    @Test
    void trainUpdateReplacesAllFields() throws Exception {
        String create = """
                {
                  "trainNumber": "TR-202",
                  "lengthMeters": 640,
                  "serviceType": "FREIGHT",
                  "priority": 3,
                  "origin": "Mumbai",
                  "destination": "Chennai",
                  "requiredCapabilities": ["DIESEL"]
                }
                """;
        String response = mockMvc.perform(post("/api/trains")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(create))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String id = extractId(response);

        String update = """
                {
                  "trainNumber": "TR-202",
                  "lengthMeters": 720,
                  "serviceType": "FREIGHT",
                  "priority": 2,
                  "origin": "Mumbai",
                  "destination": "Chennai",
                  "active": false,
                  "requiredCapabilities": ["DIESEL", "HEAVY_FREIGHT"]
                }
                """;
        mockMvc.perform(put("/api/trains/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(update))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lengthMeters").value(720))
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.requiredCapabilities", org.hamcrest.Matchers.containsInAnyOrder("DIESEL", "HEAVY_FREIGHT")));
    }

    @Test
    void trainPriorityOutsideRangeReturnsValidation() throws Exception {
        String body = """
                {
                  "trainNumber": "TR-303",
                  "lengthMeters": 600,
                  "serviceType": "FREIGHT",
                  "priority": 9,
                  "origin": "A",
                  "destination": "B"
                }
                """;
        mockMvc.perform(post("/api/trains").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    private String createYard(String code, String name, String location, String timeZone) throws Exception {
        String body = String.format("""
                {
                  "code": "%s",
                  "name": "%s",
                  "location": "%s",
                  "timeZone": "%s"
                }
                """, code, name, location, timeZone);
        String response = mockMvc.perform(post("/api/yards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return extractId(response);
    }

    private String createTrack(String yardId, String code) throws Exception {
        String body = String.format("""
                {
                  "code": "%s",
                  "usableLengthMeters": 800,
                  "purpose": "STAGING",
                  "status": "OPERATIONAL",
                  "setupBufferMinutes": 5,
                  "clearanceBufferMinutes": 5,
                  "capabilities": ["DIESEL"]
                }
                """, code);
        String response = mockMvc.perform(post("/api/yards/{yardId}/tracks", yardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return extractId(response);
    }

    private static String extractId(String response) {
        int start = response.indexOf("\"id\":\"") + 6;
        int end = response.indexOf("\"", start);
        assertThat(start).isGreaterThan(5);
        return response.substring(start, end);
    }
}
