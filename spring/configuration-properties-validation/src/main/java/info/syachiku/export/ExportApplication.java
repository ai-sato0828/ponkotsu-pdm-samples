package info.syachiku.export;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ExportProperties.class)
public class ExportApplication {

    public static void main(String[] args) {
        try (var context = SpringApplication.run(ExportApplication.class, args)) {
            var settings = context.getBean(ExportProperties.class);
            System.out.printf("EXPORT_READY batchSize=%d%n", settings.batchSize());
        }
    }
}
