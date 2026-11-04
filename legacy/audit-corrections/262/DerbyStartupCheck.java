import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class DerbyStartupCheck {
    public static void main(String[] args) throws Exception {
        Path home = Files.createTempDirectory("derby-occupied-port-");
        try (var listener = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
            listener.setSoTimeout(250);
            try {
                DerbyModesExample.runNetwork(home, listener.getLocalPort());
                throw new AssertionError("unexpected startup success on occupied port");
            } catch (IllegalStateException expected) {
                if (!expected.getMessage().startsWith("Own Derby Network Server did not start")) {
                    throw expected;
                }
            }

            try (var unexpected = listener.accept()) {
                throw new AssertionError("the sample contacted another process on the port");
            } catch (SocketTimeoutException expected) {
                System.out.println("occupied port: child rejected; no connection attempted");
            }
        } finally {
            try (var paths = Files.walk(home)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                    Files.delete(path);
                }
            }
        }
    }
}
