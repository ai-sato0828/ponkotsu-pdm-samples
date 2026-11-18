package example.postal;

import example.postal.fixed.Address;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackageClasses = Address.class)
@EntityScan(basePackageClasses = Address.class)
public class PostalApplication {
    public static void main(String[] args) {
        SpringApplication.run(PostalApplication.class, args);
    }
}
