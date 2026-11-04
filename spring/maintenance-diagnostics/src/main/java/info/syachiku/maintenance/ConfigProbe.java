package info.syachiku.maintenance;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "demo.print-config", havingValue = "true")
class ConfigProbe implements CommandLineRunner {
    private final Environment environment;

    ConfigProbe(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(String... args) {
        String timeout = environment.getRequiredProperty("demo.timeout");
        String marker = environment.getProperty("demo.jar-marker", "absent");
        System.out.println("timeout.ms=" + DurationStyle.detectAndParse(timeout).toMillis());
        System.out.println("jar.marker=" + marker);
    }
}
