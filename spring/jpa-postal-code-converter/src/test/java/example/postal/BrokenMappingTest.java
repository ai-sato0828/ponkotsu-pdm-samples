package example.postal;

import example.postal.broken.UnconvertedAddress;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import static org.junit.jupiter.api.Assertions.*;

class BrokenMappingTest {
    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = UnconvertedAddress.class)
    static class BrokenApplication {}

    @Test
    void anUnconvertedNonSerializableValueTypeFailsDuringEntityManagerFactoryCreation() {
        var failure = assertThrows(RuntimeException.class, () -> {
            try (var ignored = new SpringApplicationBuilder(BrokenApplication.class)
                    .web(WebApplicationType.NONE)
                    .properties("spring.datasource.url=jdbc:h2:mem:broken-postal",
                            "spring.jpa.hibernate.ddl-auto=create-drop",
                            "spring.main.banner-mode=off").run(
                            "--spring.datasource.url=jdbc:h2:mem:broken-postal")) {
                fail("mapping must fail before persistence is usable");
            }
        });
        Throwable root = failure;
        while (root.getCause() != null) root = root.getCause();
        System.out.println("BROKEN root=" + root.getClass().getName());
        System.out.println("BROKEN message=" + root.getMessage());
        assertFalse(java.io.Serializable.class.isAssignableFrom(PostalCode.class));
        assertEquals("org.hibernate.type.descriptor.java.spi.JdbcTypeRecommendationException",
                root.getClass().getName());
        assertTrue(root.getMessage().contains("Could not determine recommended JdbcType"));
        assertTrue(root.getMessage().contains("example.postal.PostalCode"));
    }
}
