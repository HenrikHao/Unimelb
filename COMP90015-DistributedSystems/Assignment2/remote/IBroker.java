// Zhuoyang Hao 1255309
package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IBroker extends Remote {
    int getLocalSubscriberCount(String topicId) throws RemoteException;
    void notifyTopicCreated(String topicId, String topicName, String publisherName) throws RemoteException;
    void broadcastMessage(String topicId, String message) throws RemoteException;
    void removeTopic(String topicId) throws RemoteException;
    void registerNewBroker(String brokerInfo) throws RemoteException;
}
