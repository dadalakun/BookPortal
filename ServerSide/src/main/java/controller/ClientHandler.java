package controller;

import java.net.Socket;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import model.*;
import java.util.List;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ClientHandler implements Runnable {
    private static final Logger logger = LogManager.getLogger(ClientHandler.class);
    private final Socket clientSocket;
    private final Gson gson = JsonUtil.createGson();
    private PrintWriter out;
    private BufferedReader in;
    private DatabaseService databaseService;

    public ClientHandler(Socket socket, DatabaseService databaseService) {
        this.clientSocket = socket;
        this.databaseService = databaseService;
    }

    @Override
    public void run() {
        logger.info("ClientHandler started for client: {}", clientSocket.getRemoteSocketAddress());
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                logger.debug("Request received: {}", inputLine);
                handleRequest(inputLine);
            }
        } catch (Exception e) {
            logger.error("Error in ClientHandler: ", e);
        } finally {
            closeConnections();
        }
    }

    private void handleRequest(String jsonInput) {
        try {
            JsonObject request = JsonParser.parseString(jsonInput).getAsJsonObject();
            String command = request.get("command").getAsString();
            // null if no params
            JsonObject params = request.getAsJsonObject("params");

            switch(command) {
                case "fetchAllItems":
                    List<ClientItem> items = databaseService.fetchAllItems();
                    sendResponse(true, "Fetched all items successfully", items);
                    break;
                case "fetchBorrowedItems":
                    String userId = params.get("userId").getAsString();
                    List<ClientItem> borrowedItems = databaseService.fetchBorrowedItems(userId);
                    sendResponse(true, "Fetched borrowed items successfully", borrowedItems);
                    break;
                case "login":
                    String userName = params.get("userName").getAsString();
                    String password = params.get("password").getAsString();
                    userId = databaseService.login(userName, password);
                    if (userId.equals("User not found.") || userId.equals("Invalid password.")) {
                        sendResponse(false, userId, null);
                    } else {
                        sendResponse(true, "Login successful.", userId);
                    }
                    break;
                case "signup":
                    userName = params.get("userName").getAsString();
                    password = params.get("password").getAsString();
                    String signUpResponse = databaseService.signup(userName, password);
                    if (signUpResponse.equals("Username and password cannot be empty.") || signUpResponse.equals("Username already taken.")) {
                        sendResponse(false, signUpResponse, null);
                    } else {
                        sendResponse(true, "Signup successful.", signUpResponse);
                    }
                    break;
                case "resetPassword":
                    userId = params.get("userId").getAsString();
                    String newPassword = params.get("password").getAsString();

                    // Call the resetPassword method from the DatabaseService
                    boolean success = databaseService.resetPassword(userId, newPassword);

                    // Send the response back to the client based on the result of the reset password operation
                    if (success) {
                        sendResponse(true, "Password reset successfully.", null);
                    } else {
                        sendResponse(false, "Failed to reset password.", null);
                    }
                    break;
                case "borrowItem":
                    String itemId = params.get("itemId").getAsString();
                    userId = params.get("userId").getAsString();
                    String result = databaseService.borrowItem(itemId, userId);
                    success = result.startsWith("Success");
                    sendResponse(success, result, null);
                    break;
                case "returnItem":
                    itemId = params.get("itemId").getAsString();
                    userId = params.get("userId").getAsString();
                    result = databaseService.returnItem(itemId, userId);
                    success = result.startsWith("Success");
                    sendResponse(success, result, null);
                    break;
                default:
                    sendResponse(false, "Invalid request", null);
            }
        } catch (Exception e) {
            sendResponse(false, "Error processing request: " + e.getMessage(), null);
        }
    }

    private void sendResponse(boolean success, String message, Object data) {
        logger.info("Sending response: success={}, message={}", success, message);
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("success", success);
        jsonResponse.addProperty("message", message);

        if (data != null) {
            jsonResponse.add("data", gson.toJsonTree(data));
        }

        out.println(jsonResponse);
    }

    private void closeConnections() {
        logger.info("Closing connections for client: {}", clientSocket.getRemoteSocketAddress());
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
