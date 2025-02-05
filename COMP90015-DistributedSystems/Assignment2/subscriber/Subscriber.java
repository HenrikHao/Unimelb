// Zhuoyang Hao 1255309
package subscriber;

import remote.ISubscriber;
import remote.IPubSubInterface;
import remote.IDirectoryService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Subscriber extends UnicastRemoteObject implements ISubscriber {
    private final String name;
    private final String directoryServiceIp;
    private final int directoryServicePort;
    private IPubSubInterface brokerStub;
    private final List<String> subscribedTopics = new ArrayList<>(); // Stores subscribed topic IDs

    public Subscriber(String name, String directoryServiceIp, int directoryServicePort) throws RemoteException {
        this.name = name;
        this.directoryServiceIp = directoryServiceIp;
        this.directoryServicePort = directoryServicePort;
    }

    // Heartbeat
    private void startHeartbeat() {
        new Thread(() -> {
            while (true) {
                try {
                    brokerStub.receiveSubscriberHeartbeat(name); // Send heartbeat to broker
                    Thread.sleep(2000); // Send heartbeat every 2 seconds
                } catch (Exception e) {
                    System.out.println("[error] Failed to send heartbeat: " + e.getMessage());
                }
            }
        }).start();
    }

    // Connect to a broker via the directory service
    public void connectToBroker() {
        try {
            // Connect to the directory service
            Registry registry = LocateRegistry.getRegistry(directoryServiceIp, directoryServicePort);
            IDirectoryService directoryService = (IDirectoryService) registry.lookup("DirectoryService");

            // Get the list of active brokers
            List<String> activeBrokers = directoryService.getBrokers();
            if (activeBrokers.isEmpty()) {
                System.out.println("[error] No active brokers found.");
                return;
            }

            // Randomly select a broker from the activeBrokers list
            Random random = new Random();
            int randomIndex = random.nextInt(activeBrokers.size()); // Get a random index
            String brokerInfo = activeBrokers.get(randomIndex); // Select the broker based on the random index
            String[] parts = brokerInfo.split(":");
            String brokerIp = parts[0];
            int brokerPort = Integer.parseInt(parts[1]);

            // Connect to the broker using RMI
            Registry brokerRegistry = LocateRegistry.getRegistry(brokerIp, brokerPort);
            brokerStub = (IPubSubInterface) brokerRegistry.lookup("Broker_" + brokerPort);
            System.out.println("[success] Connected to broker at " + brokerInfo);
            startHeartbeat();
        } catch (Exception e) {
            System.err.println("[error] Error connecting to broker: " + e.getMessage());

        }
    }

    // List all available topics
    public void listTopics() {
        try {
            List<String> topics = brokerStub.getTopics();
            if (topics.isEmpty()) {
                System.out.println("No topics available.");
                return;
            }
            for (String topic : topics) {
                System.out.println(topic);
            }
        } catch (RemoteException e) {
            System.err.println("Error listing topics: " + e.getMessage());
        }
    }

    // Subscribe to a topic
    public void subscribeToTopic(String topicId) {
        if (subscribedTopics.contains(topicId)) {
            System.out.println("[info] Already subscribed to topic: " + topicId);
            return;
        }
        try {
            brokerStub.subscribe(topicId, this); // Subscribe to the topic via the broker
            subscribedTopics.add(topicId); // Add topic to the list after successful subscription
            System.out.println("[success] Subscribed to topic: " + topicId);
        } catch (RemoteException e) {
            String errorMessage = e.getMessage();
            // If the error message contains "Topic not found", extract only the relevant part
            if (errorMessage != null && errorMessage.contains("Topic not found")) {
                String filteredMessage = errorMessage.substring(errorMessage.indexOf("Topic not found"));
                System.err.println("[error] Could not subscribe to topic: " + filteredMessage);
            } else {
                System.err.println("[error] Could not subscribe to topic: Unknown error.");
            }
        } catch (Exception e) {
            System.err.println("Error subscribing to topic: " + e.getMessage());
        }
    }

    // Show current subscriptions
    public void showCurrentSubscriptions() {
        if (subscribedTopics.isEmpty()) {
            System.out.println("[info] No active subscriptions.");
            return;
        }
        try {
            for (String topicId : subscribedTopics) {
                String topicName = brokerStub.getTopicName(topicId);
                String publisherName = brokerStub.getPublisherName(topicId);
                System.out.println("[ " + topicId + " ] [ " + topicName + " ] [ " + publisherName + " ]");
            }
        } catch (RemoteException e) {
            System.err.println("Error retrieving subscriptions: " + e.getMessage());
        }
    }

    // Unsubscribe from a topic
    public void unsubscribeFromTopic(String topicId){
        try {
            if (!subscribedTopics.contains(topicId)) {
                System.out.println("[info] Not subscribing to the topic:  " + topicId);
                return;
            }
            brokerStub.unsubscribe(topicId, this); // Unsubscribe via the broker
            subscribedTopics.remove(topicId);
            System.out.println("[success] Unsubscribed from topic: " + topicId);
        } catch (Exception e) {
            System.err.println("Error unsubscribing from topic: " + e.getMessage());
        }
    }

    // When the publisher deletes a topic
    @Override
    public void topicDeleted(String topicId) throws RemoteException {
        try {
            if (!subscribedTopics.contains(topicId)) {
                System.out.println("[info] Not subscribing to the topic:  " + topicId);
                return;
            }
            subscribedTopics.remove(topicId);
            System.out.println("[success] Unsubscribed from topic: " + topicId);
        } catch (Exception e) {
            System.err.println("Error unsubscribing from topic: " + e.getMessage());
        }
    }

    @Override
    public String getSubscriberName() throws RemoteException {
        return name;
    }

    // Receive a message and display it
    @Override
    public void receiveMessage(String topicId, String message, String topicName) throws RemoteException {
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm:ss");
        String formattedTime = currentTime.format(formatter);

        System.out.println("[Received] Time: " + formattedTime + " Topic ID: " + topicId + " Topic Name: " +
                topicName + " Message: " + message);
    }

    // Menu options for subscribers
    public void displayMenu() {
        System.out.println("Please select command: list, sub, current, unsub");
        System.out.println("1. list # list all topics available");
        System.out.println("2. sub {topic_id}  # subscribe to the topic");
        System.out.println("3. current # show current subscriptions");
        System.out.println("4. unsub {topic_id} # unsubscribe from the topic");
    }

    // Handle user input commands via console
    public void handleCommand() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            displayMenu();
            String command = scanner.nextLine();
            String[] parts = command.split(" ");

            switch (parts[0].toLowerCase()) {
                case "list":
                    listTopics();
                    break;
                case "sub":
                    if (parts.length < 2) {
                        System.out.println("Usage: sub {topic_id}");
                    } else {
                        subscribeToTopic(parts[1]);
                    }
                    break;
                case "current":
                    showCurrentSubscriptions();
                    break;
                case "unsub":
                    if (parts.length < 2) {
                        System.out.println("Usage: unsub {topic_id}");
                    } else {
                        unsubscribeFromTopic(parts[1]);
                    }
                    break;
                default:
                    System.out.println("Invalid command.");
                    break;
            }
        }
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java -jar subscriber.jar <name> <directoryServiceIp> <directoryServicePort>");
            return;
        }

        String name = args[0];
        String directoryServiceIp = args[1];
        int directoryServicePort = Integer.parseInt(args[2]);

        try {
            Subscriber subscriber = new Subscriber(name, directoryServiceIp, directoryServicePort);
            subscriber.connectToBroker(); // Connect to a broker
            subscriber.handleCommand(); // Start handling commands from the console
        } catch (RemoteException e) {
            System.err.println("Error starting subscriber: " + e.getMessage());
        }
    }
}