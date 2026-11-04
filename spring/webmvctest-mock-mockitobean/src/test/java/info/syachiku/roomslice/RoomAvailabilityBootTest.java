package info.syachiku.roomslice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockingDetails;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RoomAvailabilityBootTest {
    @Autowired MockMvc mockMvc;
    @Autowired RoomAvailabilityService service;

    @Test
    void theFullContextFindsTheRealService() throws Exception {
        assertThat(mockingDetails(service).isMock()).isFalse();

        mockMvc.perform(get("/rooms/R-100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value("R-100"))
                .andExpect(jsonPath("$.available").value(true));
    }
}
