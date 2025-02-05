// Zhuoyang Hao 1255309
package remote;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
public interface IPubSubInterface extends Remote{
    void publish(String topicId, String message) throws RemoteException;
    void subscribe(String topicId, ISubscriber subscriber) throws RemoteException;
    void unsubscribe(String topicId, ISubscriber subscriber) throws RemoteException;
    void createTopic(String publisherName, String topicId, String topicName) throws RemoteException;
    void deleteTopic(String topicId, String publisherName) throws RemoteException;
    int getSubscriberCount(String topicId) throws RemoteException;
    List<String> getTopics() throws RemoteException;
    String getTopicName(String topicId) throws RemoteException;
    String getPublisherName(String topicId) throws RemoteException;
    void receivePublisherHeartbeat(String publisherName) throws RemoteException;
    void receiveSubscriberHeartbeat(String subscriberId) throws RemoteException;
}
