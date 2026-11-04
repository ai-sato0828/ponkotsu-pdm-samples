package info.syachiku.roomslice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoomAvailabilityController.class)
class RoomAvailabilityWebMvcTest {
    @Autowired MockMvc mockMvc;
    @Autowired ApplicationContext context;
    @MockitoBean RoomAvailabilityService service;

    @Test
    void suppliesTheMockAsTheControllerDependency() throws Exception {
        assertThat(context.getBean(RoomAvailabilityService.class)).isSameAs(service);
        when(service.isAvailable("R-100")).thenReturn(true);

        mockMvc.perform(get("/rooms/R-100"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.roomId").value("R-100"))
                .andExpect(jsonPath("$.available").value(true));

        verify(service).isAvailable("R-100");
    }

    @Test
    void reportsTheMockResultForTheSameRoom() throws Exception {
        when(service.isAvailable("R-100")).thenReturn(false);

        mockMvc.perform(get("/rooms/R-100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value("R-100"))
                .andExpect(jsonPath("$.available").value(false));

        verify(service).isAvailable("R-100");
    }
}
