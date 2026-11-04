package info.syachiku.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserCreateRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotNull @Min(1) Integer notificationLimit) {}
