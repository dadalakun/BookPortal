import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.net.Socket;

public class ServerTest {
    private Server server;
    private final int testPort = 6668;

    @Before
    public void setUp() throws IOException {
        server = new Server();
        new Thread(() -> server.start(testPort)).start();
    }

    @Test
    public void testServerAcceptsConnections() throws IOException {
        // Attempt to connect to the server
        try (Socket client = new Socket("localhost", testPort);
             PrintWriter out = new PrintWriter(client.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {

            out.println("{\"command\":\"Fake Request\"}");
            String response = in.readLine();
            assertNotNull(response);
            assertTrue(response.contains("Invalid request"));
        }
    }

    @After
    public void clearUp() throws Exception {
        server.stop();
    }

}
