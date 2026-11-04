package info.syachiku.user;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserInputTest {
    @Autowired
    MockMvc mvc;
    @Autowired
    RecordingUserService service;

    @BeforeEach
    void resetCounts() {
        service.readCalls.set(0);
        service.createCalls.set(0);
    }

    @Test
    void pathSuppliesTheUserId() throws Exception {
        mvc.perform(get("/api/users/10"))
                .andExpect(status().isOk())
                .andExpect(handler().methodName("detail"))
                .andExpect(jsonPath("$.id").value(10));
        assertThat(service.readCalls.get()).isEqualTo(1);
    }

    @Test
    void nonNumericPathFailsBeforeCallingService() throws Exception {
        var result = mvc.perform(get("/api/users/tanaka"))
                .andExpect(status().isBadRequest())
                .andExpect(handler().methodName("detail"))
                .andReturn();
        assertThat(result.getResolvedException()).isInstanceOf(MethodArgumentTypeMismatchException.class);
        assertThat(service.readCalls.get()).isZero();
    }

    @Test
    void querySuppliesIndependentKeywordAndPage() throws Exception {
        mvc.perform(get("/api/users").queryParam("keyword", "tanaka").queryParam("page", "1"))
                .andExpect(status().isOk())
                .andExpect(handler().methodName("search"))
                .andExpect(jsonPath("$[0].id").value(10));
        mvc.perform(get("/api/users").queryParam("keyword", "tanaka"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));
        assertThat(service.readCalls.get()).isEqualTo(2);
    }

    @Test
    void nonNumericQueryFailsBeforeCallingService() throws Exception {
        var result = mvc.perform(get("/api/users").queryParam("page", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(handler().methodName("search"))
                .andReturn();
        assertThat(result.getResolvedException()).isInstanceOf(MethodArgumentTypeMismatchException.class);
        assertThat(service.readCalls.get()).isZero();
    }

    @Test
    void formSuppliesUrlEncodedFields() throws Exception {
        mvc.perform(post("/users").formField("name", "Sato").formField("email", "sato@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(handler().methodName("register"))
                .andExpect(redirectedUrl("/users/new"));
        assertThat(service.createCalls.get()).isEqualTo(1);
    }

    @Test
    void invalidFormKeepsInputWithoutCallingService() throws Exception {
        mvc.perform(post("/users").formField("name", "Sato").formField("email", "bad"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/new"))
                .andExpect(model().attributeHasFieldErrorCode("registration", "email", "Email"))
                .andExpect(content().string(containsString("name=\"email\" value=\"bad\"")));
        assertThat(service.createCalls.get()).isZero();
    }

    @Test
    void validJsonPassesTheMappedValuesAndCreatesAUser() throws Exception {
        var result = mvc.perform(post("/api/users").contentType(APPLICATION_JSON)
                        .content("""
                                {"name":"Suzuki","email":"suzuki@example.com","notificationLimit":5}
                                """))
                .andExpect(status().isCreated())
                .andExpect(handler().methodName("create"))
                .andExpect(jsonPath("$.name").value("Suzuki"))
                .andExpect(jsonPath("$.notificationLimit").value(5))
                .andExpect(jsonPath("$.role").doesNotExist())
                .andReturn();
        String location = result.getResponse().getHeader("Location");
        assertThat(location).startsWith("/api/users/");
        mvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("suzuki@example.com"));
        assertThat(service.createCalls.get()).isEqualTo(1);
    }

    @Test
    void malformedJsonFailsInTheMessageConverter() throws Exception {
        assertUnreadable("""
                {"name":"Suzuki","email":"suzuki@example.com","notificationLimit":5
                """, JsonParseException.class);
    }

    @Test
    void unconvertibleJsonFieldFailsInTheMessageConverter() throws Exception {
        assertUnreadable("""
                {"name":"Suzuki","email":"suzuki@example.com","notificationLimit":"many"}
                """, InvalidFormatException.class);
    }

    @Test
    void emptyBodyFailsBeforeValidation() throws Exception {
        assertUnreadable("", null);
    }

    @Test
    void parsedJsonCanFailValidationBeforeCallingService() throws Exception {
        var result = mvc.perform(post("/api/users").contentType(APPLICATION_JSON)
                        .content("""
                                {"name":"Suzuki","email":"suzuki@example.com","notificationLimit":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(handler().methodName("create"))
                .andReturn();
        assertThat(result.getResolvedException()).isInstanceOfSatisfying(
                MethodArgumentNotValidException.class, error ->
                        assertThat(error.getBindingResult().getFieldError("notificationLimit").getCode())
                                .isEqualTo("Min"));
        assertThat(service.createCalls.get()).isZero();
    }

    @Test
    void extraRoleFieldIsRejectedByTheSampleConfiguration() throws Exception {
        assertUnreadable("""
                {"name":"Suzuki","email":"suzuki@example.com","notificationLimit":5,"role":"ADMIN"}
                """, UnrecognizedPropertyException.class);
    }

    private void assertUnreadable(String body, Class<? extends Throwable> cause) throws Exception {
        var result = mvc.perform(post("/api/users").contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(handler().methodName("create"))
                .andReturn();
        var exception = assertThat(result.getResolvedException())
                .isInstanceOf(HttpMessageNotReadableException.class);
        if (cause != null) {
            exception.hasCauseInstanceOf(cause);
        }
        assertThat(service.createCalls.get()).isZero();
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
        final AtomicInteger readCalls = new AtomicInteger();
        final AtomicInteger createCalls = new AtomicInteger();

        @Override
        Optional<UserView> find(long userId) {
            readCalls.incrementAndGet();
            return super.find(userId);
        }

        @Override
        List<UserView> search(String keyword, int page) {
            readCalls.incrementAndGet();
            return super.search(keyword, page);
        }

        @Override
        UserView create(String name, String email, int notificationLimit) {
            createCalls.incrementAndGet();
            return super.create(name, email, notificationLimit);
        }
    }
}
