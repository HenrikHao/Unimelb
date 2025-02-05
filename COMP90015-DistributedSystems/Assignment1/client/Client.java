//Zhuoyang Hao 1255309
package client;

import Communication.ClientRequest;
import Communication.ServerResponse;

import javax.swing.*;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    private String serverAddress;
    private int serverPort;

    public Client(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    private static boolean isValidAddress(String address) {
        try {
            InetAddress.getByName(address); // Attempt to resolve the address
            return true;
        } catch (UnknownHostException e) {
            return false; // Address is not valid
        }
    }
    private static void validate(String[] args) {
        if (args.length != 2) {
            System.err.println("Number of argument is not 2");
            System.err.println("The command to run the client: java –jar client.jar <server-address> <port-number>");
            System.exit(1);
        }
        if (!isValidAddress(args[0])) {
            System.err.println("Invalid server address. Please provide a valid IP address or hostname.");
            System.exit(1);
        }
        try {
            int port = Integer.parseInt(args[1]);
            if (port < 1024 || port > 49151) {
                System.err.println("Port must be between 1024 and 49151");
                System.exit(1);
            }
        } catch (NumberFormatException e) {
            System.err.println("Port must be an integer");
            System.exit(1);
        }
    }
    public String sendRequestAndGetResponse(ClientRequest request) {
        try (Socket socket = new Socket(serverAddress, serverPort);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject(request);
            out.flush(); // Ensure data is sent completely

            ServerResponse response;
            try {
                response = (ServerResponse) in.readObject();
            } catch (EOFException e) {
                return "Server closed connection unexpectedly.";
            }

            if (response != null) {
                return formatResponse(response);
            } else {
                return "Received null response from server.";
            }

        } catch (IOException e) {
            return "Network error: " + e.getMessage();
        } catch (ClassNotFoundException e) {
            return "Class not found error: " + e.getMessage();
        } catch (Exception e) {
            return "General error: " + (e.getMessage() == null ? "No message available" : e.getMessage());
        }
    }

    private String formatResponse(ServerResponse response) {
        if (response.isSuccessful()) {
            if (response.getMeanings() != null) {
                return "Request was successful. Retrieved meanings: " + response.getMeanings();
            }
        }
        return response.getMessage();
    }

    public static void main(String[] args) {
        validate(args);
        String serverAddress = args[0];
        int serverPort = Integer.parseInt(args[1]);

        // Initialize the GUI from the main method
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ClientGUI(serverAddress, serverPort);
            }
        });
    }
}