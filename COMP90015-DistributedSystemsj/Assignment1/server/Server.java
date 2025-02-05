//Zhuoyang Hao 1255309
package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import threadpool.ThreadPool;

public class Server {
    private final static int THREAD_COUNT = 5;
    private static Dictionary dict;

    // check arguments
    private static void validate(String[] args) {
        if (args.length != 2) {
            System.err.println("Number of argument is not 2");
            System.err.println("The command to run the server: java –jar server.jar <port> <dictionary-file> (end with .json)");
            System.exit(1);
        }
        try {
            int port = Integer.parseInt(args[0]);
            if (port < 1024 || port > 49151) {
                System.err.println("Port must be between 1024 and 49151");
                System.exit(1);
            }
        } catch (NumberFormatException e) {
            System.err.println("Port must be an integer");
            System.exit(1);
        }
        String dictionaryFile = args[1];
        if (!dictionaryFile.endsWith(".json")) {
            System.err.println("Dictionary file must be a JSON file (.json)");
            System.exit(1);
        }
    }

    // run the server
    private static void run(int port) {
        ThreadPool threadPool = null;
        try {
            threadPool = new ThreadPool(THREAD_COUNT);
            ServerSocket listenSocket = new ServerSocket(port);
            System.out.println("Listening on port " + port);
            while (true) {
                Socket commSocket = listenSocket.accept();
                System.out.println("New connection from " + commSocket.getInetAddress());
                threadPool.submit(() -> {
                    try {
                        RequestHandler.handleClientRequest(commSocket, dict);
                    } finally {
                        try {
                            commSocket.close();
                        } catch (IOException e) {
                            System.out.println("Failed to close socket: " + e.getMessage());
                        }
                    }
                });
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            System.exit(1);
        } finally {
            if (threadPool != null) {
                threadPool.shutdown();
            }
        }
    }
    public static void main(String[] args) {
        validate(args);
        System.out.println("Server started");
        dict = new Dictionary(args[1]);
        System.out.println("Dictionary found");
        run(Integer.parseInt(args[0]));
    }
}
