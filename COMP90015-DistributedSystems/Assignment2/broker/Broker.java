// Zhuoyang Hao 1255309
package broker;

import java.net.InetAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import remote.IPubSubInterface;
import remote.IDirectoryService;
import remote.IBroker;
import remote.ISubscriber;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class Broker extends UnicastRemoteObject implements IPubSubInterface, IBroker {
    private final int port; // port of broker
    private final String directoryServiceIp;
    private final int directoryServicePort;
    // other brokers already connected
    private final List<String> connectedBrokers = new ArrayList<>();
    private final Map<String, String> publishers = new ConcurrentHashMap<>();

    // topics
    private final Map<String, Topic> topics = new ConcurrentHashMap<>();
    private final Map<String, IBroker> brokerStubs = new ConcurrentHashMap<>();

    public Broker(int port, String directoryServiceIp, int directoryServicePort) throws RemoteException {
        this.port = port;
        this.directoryServiceIp = directoryServiceIp;
        this.directoryServicePort = directoryServicePort;

        // Start heartbeat checker every 2 seconds
        ScheduledExecutorService heartbeatScheduler = Executors.newScheduledThreadPool(1);
        heartbeatScheduler.scheduleAtFixedRate(this::checkHeartbeats, 2, 2, TimeUnit.SECONDS);
    }

    private final Map<String, Long> publisherHeartbeats = new ConcurrentHashMap<>();
    private final Map<String, Long> subscriberHeartbeats = new ConcurrentHashMap<>();

    // register broker into the directory service
    public void registerInDirectory() {
        try {
            // directory ip and port
            Registry registry = LocateRegistry.getRegistry(directoryServiceIp, directoryServicePort);
            IDirectoryService directoryService = (IDirectoryService) registry.lookup("DirectoryService");

            String localIp = InetAddress.getLocalHost().getHostAddress();
            directoryService.registerBroker(localIp, port);

            List<String> activeBrokers = directoryService.getBrokers();
            System.out.println("[info] Active Brokers: " + activeBrokers);
            for (String broker : activeBrokers) {
                if (!broker.equals(localIp + ":" + port)) {
                    connectToBroker(broker);
                }
            }
        } catch (NotBoundException e) {
            System.err.println("[error] Directory service not bound, unable to register broker");
            System.exit(1);
        } catch (RemoteException e) {
            System.err.println("[error] Directory service is unavailable");
            System.exit(1);
        } catch (Exception e) {
            System.err.println("[error] Broker registration failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private void connectToBroker(String brokerInfo) {
        try {
            String[] parts = brokerInfo.split(":");
            String brokerIp = parts[0].trim();
            int brokerPort = Integer.parseInt(parts[1].trim());

            // Get the registry and look up the broker
            Registry registry = LocateRegistry.getRegistry(brokerIp, brokerPort);
            IBroker brokerStub = (IBroker) registry.lookup("Broker_" + brokerPort);

            // Add to the list of connected brokers
            connectedBrokers.add(brokerInfo);
            brokerStubs.put(brokerInfo, brokerStub);
            System.out.println("[success] Connected to broker: " + brokerInfo);

            // Notify the broker that we are a new broker
            brokerStub.registerNewBroker(InetAddress.getLocalHost().getHostAddress() + ":" + port);
        } catch (Exception e) {
            System.err.println("[error] Error connecting to broker: " + brokerInfo);
        }
    }

    public void startServer() throws RemoteException {
        try {
            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind("Broker_" + port, this);
            System.out.println("[success] Broker started on port: " + port);
        } catch (RemoteException e) {
            if (e.getMessage().contains("Port already in use")) {
                System.err.println("[error] Error starting broker: Port " + port + " is already in use. Please choose a different port.");
                System.exit(1);
            } else {
                System.err.println("[error] Broker start failed: " + e.getMessage());
                System.exit(1);
            }
        }
    }

    @Override
    public void registerNewBroker(String brokerInfo) throws RemoteException {
        if (!connectedBrokers.contains(brokerInfo)) {
            connectToBroker(brokerInfo);
        }
    }

    // Method to handle heartbeat from publishers
    @Override
    public void receivePublisherHeartbeat(String publisherName) throws RemoteException {
        publisherHeartbeats.put(publisherName, System.currentTimeMillis());
    }

    // Method to handle heartbeat from subscribers
    @Override
    public void receiveSubscriberHeartbeat(String subscriberName) throws RemoteException {
        subscriberHeartbeats.put(subscriberName, System.currentTimeMillis());
    }

    private void checkHeartbeats() {
        long currentTime = System.currentTimeMillis();

        // Check publisher heartbeats
        // 5 seconds timeout
        long HEARTBEAT_TIMEOUT = 5000;
        for (String publisherName : new ArrayList<>(publisherHeartbeats.keySet())) {
            if (currentTime - publisherHeartbeats.get(publisherName) > HEARTBEAT_TIMEOUT) {
                handlePublisherCrash(publisherName);
            }
        }

        // Check subscriber heartbeats
        for (String subscriberName : new ArrayList<>(subscriberHeartbeats.keySet())) {
            if (currentTime - subscriberHeartbeats.get(subscriberName) > HEARTBEAT_TIMEOUT) {
                handleSubscriberCrash(subscriberName);
            }
        }
    }

    // Handle the crash of a publisher
    private void handlePublisherCrash(String publisherName) {
        System.out.println("[error] Publisher " + publisherName + " crashed. Deleting their topics.");
        List<String> topicsToRemove = new ArrayList<>();
        for (Map.Entry<String, String> entry : publishers.entrySet()) {
            if (entry.getValue().equals(publisherName)) {
                topicsToRemove.add(entry.getKey());
            }
        }

        for (String topicId : topicsToRemove) {
            try {
                deleteTopic(topicId, publisherName);
            } catch (RemoteException e) {
                System.err.println("[error] Failed to delete topic: " + e.getMessage());
            }
        }

        publisherHeartbeats.remove(publisherName);
    }

    // Handle the crash of a subscriber
    private void handleSubscriberCrash(String subscriberName) {
        System.out.println("[error] Subscriber " + subscriberName + " crashed. Removing their subscriptions.");
        for (Topic topic : topics.values()) {
            List<ISubscriber> subscribersToRemove = new ArrayList<>();

            for (ISubscriber subscriber : topic.getSubscribers()) {
                String storedSubscriberName = topic.getSubscriberName(subscriber);

                // Compare the locally stored subscriber name
                if (storedSubscriberName.equals(subscriberName)) {
                    subscribersToRemove.add(subscriber); // Mark for removal
                }
            }

            // Remove all subscribers that were unreachable
            for (ISubscriber subscriberToRemove : subscribersToRemove) {
                topic.removeSubscriber(subscriberToRemove);
                System.out.println("[info] Removed subscriber " + subscriberName + " from topic: " + topic.getTopicName());
            }
        }

        subscriberHeartbeats.remove(subscriberName);
    }

    // create topic and notify other brokers
    @Override
    public void createTopic(String publisherName, String topicId, String topicName) throws RemoteException {
        createLocalTopic(publisherName, topicId, topicName);
        for (IBroker brokerStub : brokerStubs.values()) {
            brokerStub.notifyTopicCreated(publisherName, topicId, topicName);
        }
    }

    @Override
    public void notifyTopicCreated(String publisherName, String topicId, String topicName) throws RemoteException {
        createLocalTopic(publisherName, topicId, topicName);
    }

    public void createLocalTopic(String publisherName, String topicId, String topicName) {
        topics.put(topicId, new Topic(topicId, topicName));
        publishers.put(topicId, publisherName);
        System.out.println("[success] Created topic: " + topicId + " " + topicName + " by publisher: " + publisherName);
    }

    // topic subscription
    @Override
    public void subscribe(String topicId, ISubscriber subscriber) throws RemoteException {
        Topic topic = topics.get(topicId);
        if (topic != null) {
            topic.addSubscriber(subscriber);
            System.out.println("[success] Subscriber added to topic: " + topicId);
        } else {
            System.out.println("[error] Topic not found: " + topicId);
            throw new RemoteException("Topic not found: " + topicId);
        }
    }

    // publish new message
    @Override
    public void publish(String topicId, String message) throws RemoteException {
        Topic topic = topics.get(topicId);
        if (topic != null) {
            topic.broadcastMessage(message);

            // Broadcast the message to other brokers
            for (IBroker brokerStub : brokerStubs.values()) {
                brokerStub.broadcastMessage(topicId, message);
            }
        } else {
            System.out.println("Topic not found: " + topicId);
        }
    }

    // topic broadcast the message to its subscribers
    @Override
    public void broadcastMessage(String topicId, String message) throws RemoteException {
        Topic topic = topics.get(topicId);
        if (topic != null) {
            topic.broadcastMessage(message);
            System.out.println("[info] Broadcast message to topic: " + topicId);
        } else {
            System.out.println("[info] Topic not found for broadcast: " + topicId);
        }
    }

    // Get the subscriber count for a topic
    @Override
    public int getSubscriberCount(String topicId) throws RemoteException {
        int count = getLocalSubscriberCount(topicId);
        for (IBroker brokerStub : brokerStubs.values()) {
            count += brokerStub.getLocalSubscriberCount(topicId);
        }
        return count;
    }

    // Get the subscriber counts locally
    @Override
    public int getLocalSubscriberCount(String topicId) throws RemoteException {
        Topic topic = topics.get(topicId);
        if (topic != null) {
            return topic.getSubscriberCount();
        } else {
            System.out.println("[error] Topic not found: " + topicId);
        }
        return 0;
    }

    // Get the topics available in the system
    @Override
    public List<String> getTopics() throws RemoteException {
        List<String> topicList = new ArrayList<>();
        for (Map.Entry<String, Topic> entry : topics.entrySet()) {
            String topicId = entry.getKey();
            Topic topic = entry.getValue();
            String publisherName = publishers.get(topicId);
            topicList.add("[ " + topicId + " ] [ " + topic.getTopicName() + " ] [ " + publisherName + " ]");
        }
        return topicList;
    }

    // Get the topic name by topicId
    @Override
    public String getTopicName(String topicId) throws RemoteException {
        Topic topic = topics.get(topicId);
        if (topic != null) {
            return topic.getTopicName();
        } else {
            return "[error] Topic not found: " + topicId;
        }
    }

    // Get the publisher name by topicId
    @Override
    public String getPublisherName(String topicId) throws RemoteException {
        if (publishers.containsKey(topicId)) {
            return publishers.get(topicId);
        } else {
            return "[error] Publisher not found for topic: " + topicId;
        }
    }

    // Unsubscribe from a topic
    @Override
    public void unsubscribe(String topicId, ISubscriber subscriber) throws RemoteException {
        Topic topic = topics.get(topicId);
        if (topic != null) {
            topic.removeSubscriber(subscriber);
            System.out.println("[success] Subscriber removed from topic: " + topicId);
        } else {
            System.out.println("[error] Topic not found: " + topicId);
        }
    }

    // Delete a topic
    @Override
    public void deleteTopic(String topicId, String publisherName) throws RemoteException {
        notifySubscribersAndUnsubscribe(topicId); // Notify subscribers and start deletion

        // Notify other brokers to remove the topic
        for (IBroker brokerStub : brokerStubs.values()) {
            try {
                brokerStub.removeTopic(topicId);
            } catch (RemoteException e) {
                System.out.println("[error] Failed to notify broker to delete topic: " + e.getMessage());
            }
        }
    }

    @Override
    public void removeTopic(String topicId) throws RemoteException {
        notifySubscribersAndUnsubscribe(topicId);
    }

    private void notifySubscribersAndUnsubscribe(String topicId) throws RemoteException {
        Topic topic = topics.get(topicId);
        if (topic != null) {
            List<ISubscriber> toRemove = new ArrayList<>(); // Collect subscribers to remove

            // Iterate over the subscribers and collect them for removal
            for (ISubscriber subscriber : topic.getSubscribers()) {
                subscriber.receiveMessage(topicId, "Publisher crashed or topic deleted" ,topic.getTopicName());
                subscriber.topicDeleted(topicId);
                toRemove.add(subscriber); // Add to removal list
            }

            // remove all collected subscribers
            topic.getSubscribers().removeAll(toRemove);

            // Remove the topic locally
            topics.remove(topicId);
            publishers.remove(topicId);
            System.out.println("[success] Deleted topic locally: " + topicId);
        } else {
            System.out.println("[error] Topic not found: " + topicId);
        }
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java -jar broker.jar <port> <directoryServiceIp> <directoryServicePort>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        String directoryServiceIp = args[1];
        int directoryServicePort = Integer.parseInt(args[2]);

        try {
            Broker broker = new Broker(port, directoryServiceIp, directoryServicePort);
            broker.startServer();
            broker.registerInDirectory();
        } catch (RemoteException e) {
            System.err.println("Error starting broker: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
        }
    }
}
