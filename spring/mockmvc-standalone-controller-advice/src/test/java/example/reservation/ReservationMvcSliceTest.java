package example.reservation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
@Import(ReservationService.class)
class ReservationMvcSliceTest {
    @Autowired
    private MockMvc mvc;

    @Test
    void mvcSliceFindsAdviceInApplicationPackage() throws Exception {
        mvc.perform(post("/rooms/{roomId}/reservations", "R-100"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESERVATION_CONFLICT"))
                .andExpect(jsonPath("$.roomId").value("R-100"));
    }
}
