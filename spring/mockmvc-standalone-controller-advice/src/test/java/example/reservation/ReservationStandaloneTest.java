package example.reservation;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReservationStandaloneTest {
    private final ReservationController controller =
            new ReservationController(new ReservationService());

    @Test
    void withoutAdvicePerformThrowsServletException() {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();

        ServletException exception = assertThrows(ServletException.class, () ->
                mvc.perform(post("/rooms/{roomId}/reservations", "R-100")));

        assertInstanceOf(ReservationService.ReservationConflict.class, exception.getCause());
    }

    @Test
    void registeredAdviceReturns409AndJson() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ReservationAdvice())
                .build();

        mvc.perform(post("/rooms/{roomId}/reservations", "R-100"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("RESERVATION_CONFLICT"))
                .andExpect(jsonPath("$.roomId").value("R-100"));
    }

    @Test
    void freeRoomStillReturns201() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();

        mvc.perform(post("/rooms/{roomId}/reservations", "R-200"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RESERVED"))
                .andExpect(jsonPath("$.roomId").value("R-200"));
    }
}
