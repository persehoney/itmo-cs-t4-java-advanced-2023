package info.kgeorgiy.ja.petrova.bank.person;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Person extends Remote {
    String getName() throws RemoteException;

    String getLastName() throws RemoteException;

    String getPassportId() throws RemoteException;
}
