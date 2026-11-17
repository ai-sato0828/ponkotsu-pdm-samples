package info.syachiku.audit;

import java.time.Instant;
import java.util.List;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PageableInputApplication {
    public static void main(String[] args) {
        SpringApplication.run(PageableInputApplication.class, args);
    }

    // The executable sample starts with only four fixed, in-memory rows.
    @Bean
    ApplicationRunner seedSample(InquiryRepository repository) {
        return arguments -> {
            Instant time = Instant.parse("2026-11-01T00:00:00Z");
            repository.saveAll(List.of(
                    new Inquiry(1L, "問い合わせ1", Status.OPEN, time),
                    new Inquiry(2L, "問い合わせ2", Status.OPEN, time),
                    new Inquiry(3L, "問い合わせ3", Status.OPEN, time),
                    new Inquiry(4L, "完了済み", Status.CLOSED, time)));
            System.out.println("SEED_READY open=3 closed=1");
        };
    }
}
