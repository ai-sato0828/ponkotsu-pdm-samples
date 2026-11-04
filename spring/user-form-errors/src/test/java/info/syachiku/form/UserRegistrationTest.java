package info.syachiku.form;

import jakarta.servlet.ServletException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserRegistrationTest {
    @Autowired
    MockMvc mvc;
    @Autowired
    RecordingUserService service;

    @BeforeEach
    void clearCalls() {
        service.calls.clear();
    }

    @Test
    void initialGetRendersDefaultsAndChoices() throws Exception {
        mvc.perform(get("/users/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/new"))
                .andExpect(model().attributeExists("userRegisterForm", "departments"))
                .andExpect(content().string(containsString("value=\"DEV\" selected=\"selected\"")))
                .andExpect(content().string(containsString("value=\"OPS\"")))
                .andExpect(content().string(containsString("name=\"seatLimit\" value=\"1\"")));
        assertThat(service.calls).isEmpty();
    }

    @Test
    void wrongFormNamePreventsTemplateRendering() {
        assertThatThrownBy(() -> mvc.perform(get("/demo/users/wrong-form-name")))
                .isInstanceOf(ServletException.class)
                .hasRootCauseMessage(
                        "Neither BindingResult nor plain target object for bean name 'userRegisterForm' available as request attribute");
        assertThat(service.calls).isEmpty();
    }

    @Test
    void conversionErrorRetainsSubmittedTextAndDepartment() throws Exception {
        mvc.perform(post("/users").formField("name", "Sato")
                        .formField("department", "OPS").formField("seatLimit", "abc"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/new"))
                .andExpect(model().attributeHasFieldErrorCode("userRegisterForm", "seatLimit", "typeMismatch"))
                .andExpect(content().string(containsString("name=\"name\" value=\"Sato\"")))
                .andExpect(content().string(containsString("value=\"OPS\" selected=\"selected\"")))
                .andExpect(content().string(containsString("name=\"seatLimit\" value=\"abc\"")))
                .andExpect(content().string(containsString("利用枠は整数で入力してください")));
        assertThat(service.calls).isEmpty();
    }

    @Test
    void adjacentBindingResultReceivesConstraintError() throws Exception {
        mvc.perform(post("/users").formField("name", "Sato")
                        .formField("department", "OPS").formField("seatLimit", "0"))
                .andExpect(status().isOk())
                .andExpect(handler().methodName("register"))
                .andExpect(model().attributeHasFieldErrorCode("userRegisterForm", "seatLimit", "Min"))
                .andExpect(content().string(containsString("name=\"seatLimit\" value=\"0\"")))
                .andExpect(content().string(containsString("利用枠は1以上で入力してください")));
        assertThat(service.calls).isEmpty();
    }

    @Test
    void modelBetweenFormAndBindingResultMakesInvalidPostFailBeforeHandlerBody() throws Exception {
        var result = mvc.perform(post("/demo/users/wrong-order").formField("name", "Sato")
                        .formField("department", "OPS").formField("seatLimit", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(handler().methodName("wrongOrder"))
                .andReturn();
        assertThat(result.getResolvedException())
                .isInstanceOfSatisfying(MethodArgumentNotValidException.class, error ->
                        assertThat(error.getBindingResult().getFieldError("seatLimit").getCode()).isEqualTo("Min"));
        assertThat(service.calls).isEmpty();
    }

    @Test
    void validPostWithWrongOrderCanPass() throws Exception {
        mvc.perform(post("/demo/users/wrong-order").formField("name", "Sato")
                        .formField("department", "OPS").formField("seatLimit", "2"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/users/new?registered"));
        assertThat(service.calls).containsExactly(new UserService.Registration("Sato", "OPS", 2));
    }

    @Test
    void implicitFormUsesSameConventionalModelName() throws Exception {
        mvc.perform(post("/demo/users/implicit").formField("name", "Sato")
                        .formField("department", "OPS").formField("seatLimit", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/new"))
                .andExpect(model().attributeHasFieldErrorCode("userRegisterForm", "seatLimit", "Min"))
                .andExpect(content().string(containsString("value=\"OPS\" selected=\"selected\"")));
        assertThat(service.calls).isEmpty();
    }

    @Test
    void validPostCallsServiceWithConvertedValuesThenRedirects() throws Exception {
        mvc.perform(post("/users").formField("name", "Sato")
                        .formField("department", "OPS").formField("seatLimit", "2"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/users/new?registered"));
        assertThat(service.calls).containsExactly(new UserService.Registration("Sato", "OPS", 2));
    }

    @Test
    void departmentNotInChoicesIsRejected() throws Exception {
        mvc.perform(post("/users").formField("name", "Sato")
                        .formField("department", "UNKNOWN").formField("seatLimit", "2"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode("userRegisterForm", "department", "unknownDepartment"));
        assertThat(service.calls).isEmpty();
    }

    @TestConfiguration
    static class TestServices {
        @Bean
        @Primary
        RecordingUserService recordingUserService() {
            return new RecordingUserService();
        }
    }

    static class RecordingUserService extends UserService {
        final List<Registration> calls = new ArrayList<>();

        @Override
        void register(String name, String department, int seatLimit) {
            calls.add(new Registration(name, department, seatLimit));
            super.register(name, department, seatLimit);
        }
    }
}
