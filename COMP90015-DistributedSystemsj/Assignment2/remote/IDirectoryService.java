// Zhuoyang Hao 1255309
package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IDirectoryService extends Remote {
    void registerBroker(String brokerIP, int brokerPort) throws RemoteException;
    List<String> getBrokers() throws RemoteException;
}
