// Zhuoyang Hao 1255309
package directory;

import remote.IDirectoryService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

public class DirectoryService extends UnicastRemoteObject implements IDirectoryService {
    private final List<String> activeBrokers;

    protected DirectoryService() throws RemoteException {
        activeBrokers = new ArrayList<>();
    }

    @Override
    public synchronized void registerBroker(String brokerIP, int brokerPort) throws RemoteException {
        String brokerInfo = brokerIP + ":" + brokerPort;
        if (!activeBrokers.contains(brokerInfo)) {
            activeBrokers.add(brokerInfo);
            System.out.println("[success] Broker " + brokerInfo + " registered");
        }
    }

    @Override
    public synchronized List<String> getBrokers() throws RemoteException {
        return new ArrayList<>(activeBrokers);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java -jar directory.jar <directoryServicePort>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        try {
            DirectoryService directoryService = new DirectoryService();

            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind("DirectoryService", directoryService);

            System.out.println("DirectoryService is ready");
        } catch (RemoteException e) {
            System.out.println("DirectoryService is unavailable");
        }
    }
}
