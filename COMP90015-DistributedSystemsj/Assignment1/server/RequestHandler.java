//Zhuoyang Hao 1255309
package server;

import Communication.ClientRequest;
import Communication.ServerResponse;

import java.io.*;
import java.net.Socket;

public class RequestHandler {

    public static void handleClientRequest(Socket commSocket, Dictionary dic) {
        ObjectOutputStream objectOutputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            // Initialize streams
            objectInputStream = new ObjectInputStream(commSocket.getInputStream());
            objectOutputStream = new ObjectOutputStream(commSocket.getOutputStream());
            // Read the client request
            ClientRequest request = (ClientRequest) objectInputStream.readObject();
            if (request == null) {
                System.err.println("Null request received, closing socket.");
                return;
            }

            // Process the request
            ServerResponse response = processRequest(request, dic);
            System.out.println("Processing completed, sending response: " + response.getMessage());

            // Send response
            objectOutputStream.writeObject(response);
            objectOutputStream.flush(); // Ensure data is completely sent to the client
            System.out.println("Response sent successfully");

        } catch (IOException e) {
            System.err.println("IO Error handling client: " + e);
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found while reading request: " + e);
        } catch (Exception e) {
            System.err.println("General error in request handling: " + e);
        } finally {
            // Clean up resources, ensuring all are properly closed
            try {
                if (objectInputStream != null) {
                    objectInputStream.close();
                }
                if (objectOutputStream != null) {
                    objectOutputStream.close();
                }
                commSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing streams or socket: " + e);
            }
        }
    }

    private static ServerResponse processRequest(ClientRequest request, Dictionary dic) {
        switch (request.getRequest()) {
            case CREATE, UPDATE, ADD, RETRIEVE, DELETE:
                return handleModifyRequest(request, dic);
            default:
                ServerResponse response = new ServerResponse();
                response.setSuccessful(false);
                response.setMessage("Unknown request type");
                System.err.println("Unknown request type");
                return response;
        }
    }

    // call dictionary methods to handle different requests
    private static ServerResponse handleModifyRequest(ClientRequest request, Dictionary dic) {

        switch (request.getRequest()) {
            case CREATE:
                return dic.create(request.getWord(), request.getMeanings());
            case RETRIEVE:
                return dic.retrieve(request.getWord());
            case ADD:
                return dic.addMeaning(request.getWord(), request.getMeanings().get(0));
            case UPDATE:
                return dic.update(request.getWord(), request.getMeanings());
            case DELETE:
                return dic.delete(request.getWord());
            default:
                ServerResponse response = new ServerResponse();
                response.setSuccessful(false);
                response.setMessage("Invalid request operation");
                return response;
        }
    }

}