package info.syachiku.roomslice;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockingDetails;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Opt in with mvn -Dtest=MockFieldOnlyReproduction test; it is expected to fail.
@WebMvcTest(RoomAvailabilityController.class)
class MockFieldOnlyReproduction {
    @Mock RoomAvailabilityService service;
    @Autowired MockMvc mockMvc;

    MockFieldOnlyReproduction() throws Exception {
        // Create the field mock before Spring injects the test instance.
        try (AutoCloseable mocks = MockitoAnnotations.openMocks(this)) {
            assertThat(mockingDetails(service).isMock()).isTrue();
            System.out.println("REPRO_FIELD_MOCK_CREATED");
        }
    }

    @Test
    void requestIsNeverReachedWhenTheControllerDependencyIsMissing() throws Exception {
        System.out.println("REPRO_MOCKMVC_REQUEST_REACHED");
        mockMvc.perform(get("/rooms/R-100"))
                .andExpect(status().isOk());
    }
}
