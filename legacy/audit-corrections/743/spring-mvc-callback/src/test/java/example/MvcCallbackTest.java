package example;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

class MvcCallbackTest {
    @Test
    void getCallsTheControllerAndUsesItsReturnValue() throws Exception {
        var controller = new GreetingController();
        var mvc = MockMvcBuilders.standaloneSetup(controller).build();

        var response = mvc.perform(get("/greeting")).andReturn().getResponse();

        assertEquals(200, response.getStatus());
        assertEquals("Hello", response.getContentAsString());
        assertEquals(1, controller.callCount());
    }

    @Test
    void postDoesNotCallAControllerMappedToGet() throws Exception {
        var controller = new GreetingController();
        var mvc = MockMvcBuilders.standaloneSetup(controller).build();

        var response = mvc.perform(post("/greeting")).andReturn().getResponse();

        assertEquals(405, response.getStatus());
        assertEquals(0, controller.callCount());
    }
}
