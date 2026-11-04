import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import org.apache.derby.drda.NetworkServerControl;

public class DerbyModesExample {
    private static final String HOST = "127.0.0.1";

    public static void main(String[] args) throws Exception {
        Path home = Files.createTempDirectory("derby-demo-");
        System.setProperty("derby.system.home", home.toString());
        try {
            runEmbedded(home.resolve("embedded"));
            runNetwork(home.resolve("server"), findAvailablePort());
        } finally {
            removeTree(home);
        }
    }

    private static void runEmbedded(Path database) throws SQLException {
        String url = "jdbc:derby:" + database;
        try {
            insertAndRead(url, "embedded");
        } finally {
            try {
                DriverManager.getConnection(url + ";shutdown=true").close();
            } catch (SQLException e) {
                if (!"08006".equals(e.getSQLState())) {
                    throw e;
                }
            }
        }
    }

    private static int findAvailablePort() throws IOException {
        try (var socket = new ServerSocket(0, 1, InetAddress.getByName(HOST))) {
            return socket.getLocalPort();
        }
    }

    static void runNetwork(Path serverHome, int port) throws Exception {
        Files.createDirectories(serverHome);
        InetAddress host = InetAddress.getByName(HOST);
        Path serverLog = serverHome.resolve("server.log");
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        var server = new ProcessBuilder(java,
                "-Duser.language=en", "-Duser.country=US",
                "-Dderby.system.home=" + serverHome,
                "-cp", System.getProperty("java.class.path"),
                "org.apache.derby.drda.NetworkServerControl",
                "start", "-h", HOST, "-p", Integer.toString(port))
                .redirectErrorStream(true)
                .redirectOutput(serverLog.toFile())
                .start();

        try {
            waitForServerLog(server, serverLog, port);
            new NetworkServerControl(host, port).ping();
            if (!server.isAlive()) {
                throw new IllegalStateException("Derby Network Server stopped after startup");
            }

            String url = "jdbc:derby://" + HOST + ":" + port + "/sample";
            insertAndRead(url, "network");
            if (!Files.isDirectory(serverHome.resolve("sample"))) {
                throw new AssertionError("network database was not created under server home");
            }
        } finally {
            server.destroy();
            if (!server.waitFor(5, TimeUnit.SECONDS)) {
                server.destroyForcibly().waitFor();
            }
        }
    }

    private static void waitForServerLog(Process server, Path log, int port)
            throws IOException, InterruptedException {
        String ready = "started and ready to accept connections on port " + port;
        for (int attempt = 0; attempt < 60 && server.isAlive(); attempt++) {
            if (Files.readAllLines(log).stream().anyMatch(line -> line.strip().endsWith(ready))) {
                return;
            }
            Thread.sleep(100);
        }
        throw new IllegalStateException("Own Derby Network Server did not start on port " + port);
    }

    private static void insertAndRead(String url, String value) throws SQLException {
        try (var connection = DriverManager.getConnection(url + ";create=true");
             var sql = connection.createStatement()) {
            sql.executeUpdate("CREATE TABLE sample_user (id INT PRIMARY KEY, name VARCHAR(20))");
            try (var insert = connection.prepareStatement("INSERT INTO sample_user VALUES (?, ?)")) {
                insert.setInt(1, 1);
                insert.setString(2, value);
                if (insert.executeUpdate() != 1) {
                    throw new AssertionError("expected one inserted row");
                }
            }
        }
        try (var connection = DriverManager.getConnection(url);
             var sql = connection.createStatement();
             var rows = sql.executeQuery("SELECT name FROM sample_user WHERE id = 1")) {
            if (!rows.next() || !value.equals(rows.getString(1)) || rows.next()) {
                throw new AssertionError("expected one " + value + " row");
            }
        }
        System.out.println(value + ": 1 row read");
    }

    private static void removeTree(Path directory) throws IOException {
        try (var paths = Files.walk(directory)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(path);
            }
        }
    }
}
