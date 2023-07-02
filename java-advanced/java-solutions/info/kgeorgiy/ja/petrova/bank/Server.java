package info.kgeorgiy.ja.petrova.bank;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public final class Server {
    private final static int DEFAULT_PORT = 8888;
    public final static String BINDING_NAME = "rmi://localhost:8888/bank";

    public static void main(final String... args) {
        final int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;

        final RemoteBank bank = new RemoteBank(port);
        try {
            final Registry registry = LocateRegistry.createRegistry(port);
            Remote stub = UnicastRemoteObject.exportObject(bank, port);
            Naming.rebind(BINDING_NAME, stub);
        } catch (MalformedURLException e) {
            System.err.println("Malformed URL");
        } catch (RemoteException e) {
            System.err.println("Unable to export the registry or the object");
        }
    }
}