package info.syachiku.employee;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HttpMethodTest {
    @Autowired
    MockMvc mvc;
    @Autowired
    EmployeeDirectory employees;

    @Test
    void GETを繰り返しても社員データは変わらない() throws Exception {
        var before = employees.findAll();

        mvc.perform(get("/employees")).andExpect(status().isOk());
        mvc.perform(get("/employees/10")).andExpect(status().isOk());
        mvc.perform(get("/employees/10")).andExpect(status().isOk());

        assertThat(employees.findAll()).containsExactlyElementsOf(before);
    }

    @Test
    void POSTで一件登録しリダイレクト後のGETでさらに増えない() throws Exception {
        int before = employees.findAll().size();

        String location = mvc.perform(post("/employees").param("name", "鈴木"))
                .andExpect(status().isFound())
                .andReturn().getResponse().getRedirectedUrl();
        assertThat(location).startsWith("/employees/");
        assertThat(employees.findAll()).hasSize(before + 1);
        mvc.perform(get(location)).andExpect(status().isOk())
                .andExpect(content().string(containsString("鈴木")));
        mvc.perform(get(location)).andExpect(status().isOk());

        assertThat(employees.findAll()).hasSize(before + 1);
    }

    @Test
    void 無効なPOSTはフォームを返し社員データを変えない() throws Exception {
        var before = employees.findAll();

        mvc.perform(post("/employees").param("name", " "))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/form"))
                .andExpect(model().attributeHasFieldErrors("employeeForm", "name"));

        assertThat(employees.findAll()).containsExactlyElementsOf(before);
    }

    @Test
    void 削除URLをGETで開いても消えずPOSTのときだけ消える() throws Exception {
        long id = employees.create("削除対象").id();
        var before = employees.findAll();

        mvc.perform(get("/employees/{id}/delete", id))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string("Allow", containsString("POST")));
        assertThat(employees.findAll()).containsExactlyElementsOf(before);

        mvc.perform(post("/employees/{id}/delete", id))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/employees"));
        assertThat(employees.findAll()).noneMatch(employee -> employee.id() == id);
    }

    @Test
    void APIではDELETEが登録済みの社員を削除して204を返す() throws Exception {
        long id = employees.create("API削除対象").id();

        mvc.perform(delete("/api/employees/{id}", id)).andExpect(status().isNoContent());
        assertThat(employees.findAll()).noneMatch(employee -> employee.id() == id);
    }
}
