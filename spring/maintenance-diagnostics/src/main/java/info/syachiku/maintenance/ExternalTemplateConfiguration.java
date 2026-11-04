package info.syachiku.maintenance;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;

@Configuration(proxyBeanMethods = false)
class ExternalTemplateConfiguration {
    @Bean
    FileTemplateResolver externalTemplateResolver(
            @Value("${demo.templates-directory:external-templates}") Path directory) {
        var resolver = new FileTemplateResolver();
        resolver.setPrefix(directory.toAbsolutePath().normalize() + "/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setOrder(1);
        resolver.setCheckExistence(true);
        resolver.setCacheable(false);
        return resolver;
    }
}
