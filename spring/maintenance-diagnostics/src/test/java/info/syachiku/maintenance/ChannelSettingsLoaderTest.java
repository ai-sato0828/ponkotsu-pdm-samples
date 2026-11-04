package info.syachiku.maintenance;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import static org.junit.jupiter.api.Assertions.*;

class ChannelSettingsLoaderTest {
    @TempDir
    Path directory;

    @Test
    void jar内のファイルをストリームで開き同名の設定キーを経路ごとに保持する() throws Exception {
        Path jar = jar("channels.jar", "intra", "internal", "internet", "external");
        try (var loader = classLoader(jar)) {
            var configs = new ChannelSettingsLoader(new PathMatchingResourcePatternResolver(loader))
                    .load("classpath*:channels/application_*.properties");
            assertEquals(2, configs.size());
            assertEquals("internal", configs.get("intra").getProperty("target"));
            assertEquals("external", configs.get("internet").getProperty("target"));
        }
    }

    @Test
    void 別jarに同名の経路設定があれば読み込み順で上書きせず止める() throws Exception {
        Path first = jar("first.jar", "intra", "first");
        Path second = jar("second.jar", "intra", "second");
        try (var loader = classLoader(first, second)) {
            var settings = new ChannelSettingsLoader(new PathMatchingResourcePatternResolver(loader));
            var failure = assertThrows(IllegalArgumentException.class,
                    () -> settings.load("classpath*:channels/application_*.properties"));
            assertEquals("Duplicate channel: intra", failure.getMessage());
        }
    }

    private URLClassLoader classLoader(Path... jars) throws IOException {
        URL[] urls = new URL[jars.length];
        for (int i = 0; i < jars.length; i++) {
            urls[i] = jars[i].toUri().toURL();
        }
        return new URLClassLoader(urls, null);
    }

    private Path jar(String name, String... channelAndTarget) throws IOException {
        Path jar = directory.resolve(name);
        try (var archive = new JarOutputStream(Files.newOutputStream(jar))) {
            archive.putNextEntry(new JarEntry("channels/"));
            archive.closeEntry();
            for (int i = 0; i < channelAndTarget.length; i += 2) {
                archive.putNextEntry(new JarEntry("channels/application_" + channelAndTarget[i] + ".properties"));
                archive.write(("target=" + channelAndTarget[i+1] + "\n").getBytes(StandardCharsets.UTF_8));
                archive.closeEntry();
            }
        }
        return jar;
    }
}
