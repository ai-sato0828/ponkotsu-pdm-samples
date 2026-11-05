package info.syachiku.export;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("export")
@Validated
public record ExportProperties(
        @NotNull @Min(1) @Max(500) Integer batchSize) {
}
