package info.syachiku.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UserRegisterForm {
    @NotBlank(message = "氏名を入力してください")
    private String name;

    @NotBlank(message = "部署を選択してください")
    private String department;

    @NotNull(message = "利用枠を入力してください")
    @Min(value = 1, message = "利用枠は1以上で入力してください")
    private Integer seatLimit;

    public UserRegisterForm() {}

    public UserRegisterForm(String name, String department, Integer seatLimit) {
        this.name = name;
        this.department = department;
        this.seatLimit = seatLimit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Integer getSeatLimit() {
        return seatLimit;
    }

    public void setSeatLimit(Integer seatLimit) {
        this.seatLimit = seatLimit;
    }
}
