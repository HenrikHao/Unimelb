// Zhuoyang Hao 1255309
package broker;

import remote.ISubscriber;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class Topic {
    private final String topicName;
    private final String topicId;
    private final Map<ISubscriber, String> subscribers; // storing subscribers of this topic

    public Topic(String topicId, String topicName) {
        this.topicName = topicName;
        this.topicId = topicId;
        this.subscribers = new ConcurrentHashMap<>();
    }


    public String getTopicName() {
        return topicName;
    }

    public List<ISubscriber> getSubscribers() {
        return new ArrayList<>(subscribers.keySet());
    }

    public void addSubscriber(ISubscriber subscriber) {
        if (!subscribers.containsKey(subscriber)) {
            try {
                subscribers.put(subscriber, subscriber.getSubscriberName());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void removeSubscriber(ISubscriber subscriber) {
        subscribers.remove(subscriber);
    }

    public String getSubscriberName(ISubscriber subscriber) {
        return subscribers.get(subscriber); // Concurrent get
    }

    public void broadcastMessage(String message) {
        System.out.println("Broadcasting message to topic: " + topicName);
        for (ISubscriber subscriber : subscribers.keySet()) {
            try {
                subscriber.receiveMessage(topicId, message, topicName);
                System.out.println("Sent message to subscriber.");
            } catch (Exception e) {
                System.err.println("Error sending message to subscriber: " + e.getMessage());
            }
        }
    }

    public int getSubscriberCount() {
        return subscribers.size();
    }

}
