package info.syachiku.maintenance;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import org.springframework.core.io.support.ResourcePatternResolver;

class ChannelSettingsLoader {
    private final ResourcePatternResolver resolver;

    ChannelSettingsLoader(ResourcePatternResolver resolver) {
        this.resolver = resolver;
    }

    Map<String, Properties> load(String pattern) throws IOException {
        Map<String, Properties> settings = new LinkedHashMap<>();
        for (var resource : resolver.getResources(pattern)) {
            String fileName = resource.getFilename();
            if (fileName == null || !fileName.matches("application_[a-z]+\\.properties")) {
                throw new IllegalArgumentException("Unexpected configuration file: " + fileName);
            }
            String channel = fileName.substring("application_".length(), fileName.indexOf('.'));
            Properties properties = new Properties();
            try (var reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
            if (settings.putIfAbsent(channel, properties) != null) {
                throw new IllegalArgumentException("Duplicate channel: " + channel);
            }
        }
        return Map.copyOf(settings);
    }
}
