package info.syachiku.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRegistrationForm(@NotBlank String name, @NotBlank @Email String email) {}
