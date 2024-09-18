import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.net.SocketException;

import com.mongodb.client.MongoDatabase;
import controller.ClientHandler;
import model.DatabaseService;
import model.MongoDBUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Server {
    private static final Logger logger = LogManager.getLogger(Server.class);
    private ServerSocket serverSocket;

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            logger.info("Server started on port {}", port);
            MongoDatabase database = MongoDBUtil.connectToDB("mongodb+srv://tjchang:dadalakun25@cluster0.rcits1a.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0", "library");
            DatabaseService databaseService = DatabaseService.getInstance(database);
            // databaseService.initializeData("src/main/resources/db/items.json");

            while (!serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    logger.info("Accepted connection from {}", clientSocket.getRemoteSocketAddress());
                    ClientHandler clientHandler = new ClientHandler(clientSocket, databaseService);
                    Thread thread = new Thread(clientHandler);
                    thread.start();
                } catch (SocketException e) {
                    if (serverSocket.isClosed()) {
                        logger.info("Server socket was closed, exiting accept loop.");
                        break;
                    }
                    logger.error("SocketException during accept", e);
                }
            }
        } catch (IOException e) {
            logger.error("Exception occurred while starting the server", e);
        } finally {
            stop();
        }
    }

    public void stop() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                logger.info("Server socket closed");
            }
        } catch (IOException e) {
            logger.error("Error when closing server socket", e);
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        logger.info("Initializing server...");
        server.start(6667);
    }
}
