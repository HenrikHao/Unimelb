// Zhuoyang Hao 1255309
package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ISubscriber extends Remote {
    void receiveMessage(String topicId, String message, String topicName) throws RemoteException;
    String getSubscriberName() throws RemoteException;
    void topicDeleted(String topicId) throws RemoteException;
}
