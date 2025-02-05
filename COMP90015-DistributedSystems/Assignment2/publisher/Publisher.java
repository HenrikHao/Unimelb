// Zhuoyang Hao 1255309
package publisher;

import remote.IDirectoryService;
import remote.IPubSubInterface;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Random;

public class Publisher {
    private IPubSubInterface brokerStub;
    private final String name;
    private final String directoryServiceIp;
    private final int directoryServicePort;
    private final List<String> publishedTopics = new ArrayList<>();
    public Publisher(String name, String directoryServiceIp, int directoryServicePort) {
        this.name = name;
        this.directoryServiceIp = directoryServiceIp;
        this.directoryServicePort = directoryServicePort;
    }

    // Heartbeat
    private void startHeartbeat() {
        new Thread(() -> {
            while (true) {
                try {
                    brokerStub.receivePublisherHeartbeat(name); // Send heartbeat to broker
                    Thread.sleep(2000); // Send heartbeat every 2 seconds
                } catch (Exception e) {
                    System.out.println("[error] Failed to send heartbeat: " + e.getMessage());
                }
            }
        }).start();
    }


    // Connect to a broker via RMI
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

            // Connect to the chosen broker using RMI
            Registry brokerRegistry = LocateRegistry.getRegistry(brokerIp, brokerPort);
            brokerStub = (IPubSubInterface) brokerRegistry.lookup("Broker_" + brokerPort);
            System.out.println("[success] Connected to broker at " + brokerInfo);
            startHeartbeat();
        } catch (Exception e) {
            System.err.println("Error connecting to broker: " + e.getMessage());
        }
    }

    // Menu options for publishers
    public void displayMenu() {
        System.out.println("Please select command: create, publish, show, delete.");
        System.out.println("1. create {topic_id} {topic_name} # create a new topic");
        System.out.println("2. publish {topic_id} {message} # publish a message to an existing topic");
        System.out.println("3. show {topic_id} # show subscriber count for current publisher");
        System.out.println("4. delete {topic_id} # delete a topic");
    }

    // Handle the console commands
    public void handleCommand() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            displayMenu();
            String command = scanner.nextLine();
            String[] parts = command.split(" ", 3);
            String action = parts[0];

            switch (action.toLowerCase()) {
                case "create":
                    if (parts.length < 3) {
                        System.out.println("Usage: create {topic_id} {topic_name}");
                    } else {
                        createTopic(parts[1], parts[2]);
                    }
                    break;
                case "publish":
                    if (parts.length < 3) {
                        System.out.println("Usage: publish {topic_id} {message}");
                    } else {
                        publishMessage(parts[1], parts[2]);
                    }
                    break;
                case "show":
                    if (parts.length < 2) {
                        System.out.println("Usage: show {topic_id}");
                    } else {
                        showSubscriberCount(parts[1]);
                    }
                    break;
                case "delete":
                    if (parts.length < 2) {
                        System.out.println("Usage: delete {topic_id}");
                    } else {
                        deleteTopic(parts[1]);
                    }
                    break;
                default:
                    System.out.println("Invalid command.");
            }
        }
    }

    // Create a new topic
    public void createTopic(String topicId, String topicName) {
        try {
            brokerStub.createTopic(name, topicId, topicName);
            publishedTopics.add(topicId);
            System.out.println("[success] Created topic: " + topicId + " " + topicName);
        } catch (Exception e) {
            System.err.println("[error] " + e.getMessage());
        }
    }


    // Publish a message to a topic
    public void publishMessage(String topicId, String message) {
        try {
            if (message.length() > 100) {
                System.out.println("[error] Message exceeds the maximum length of 100 characters.");
                return;
            }
            if (!publishedTopics.contains(topicId)) {
                System.out.println("[error] Topic does not exist or not created by you.");
                return;
            }
            brokerStub.publish(topicId, message);
            System.out.println("[success] Published message to topic: " + topicId);
        } catch (Exception e) {
            System.err.println("[error] " + e.getMessage());
        }
    }

    // Show subscriber count for a topic
    public void showSubscriberCount(String topicId) {
        try {
            if (!publishedTopics.contains(topicId)) {
                System.out.println("[error] Topic does not exist or not created by you.");
                return;
            }
            int count = brokerStub.getSubscriberCount(topicId);
            System.out.println("Topic ID: " + topicId + " has " + count + " subscribers.");
        } catch (Exception e) {
            System.err.println("[error] " + e.getMessage());
        }
    }

    // Delete a topic
    public void deleteTopic(String topicId) {
        if (!publishedTopics.contains(topicId)) {
            System.out.println("[error] Topic does not exist or not published.");
            return;
        }
        publishedTopics.remove(topicId);
        try {
            brokerStub.deleteTopic(topicId, this.name);
            System.out.println("[success] Deleted topic: " + topicId);
        } catch (Exception e) {
            System.err.println("[error] " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java -jar publisher.jar <username> <directoryServiceIp> <directoryServicePort>");
            return;
        }
        String name = args[0];
        String directoryServiceIp = args[1];
        int directoryServicePort = Integer.parseInt(args[2]);

        Publisher publisher = new Publisher(name, directoryServiceIp, directoryServicePort);
        publisher.connectToBroker();
        publisher.handleCommand();
    }
}