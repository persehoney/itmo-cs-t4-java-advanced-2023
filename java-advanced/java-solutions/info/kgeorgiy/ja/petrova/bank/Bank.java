package info.kgeorgiy.ja.petrova.bank;

import info.kgeorgiy.ja.petrova.bank.account.Account;
import info.kgeorgiy.ja.petrova.bank.person.LocalPerson;
import info.kgeorgiy.ja.petrova.bank.person.Person;
import info.kgeorgiy.ja.petrova.bank.person.RemotePerson;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface Bank extends Remote {
    /**
     * Creates a new account with specified identifier if it does not already exist.
     * @param subId account id
     * @return created or existing account.
     */
    Account createAccount(String subId, String personId) throws RemoteException;

    /**
     * Returns account by identifier.
     * @param id account id
     * @return account with specified identifier or {@code null} if such account does not exist.
     */
    Account getAccount(String id) throws RemoteException;

    Person registerPerson(String name, String lastName, String passportId) throws RemoteException;

    Person getPerson(String name, String lastName, String passportId) throws RemoteException;

    RemotePerson findRemotePerson(String passportId) throws RemoteException;

    LocalPerson findLocalPerson(String passportId) throws RemoteException;

    Set<String> getAccounts(String personId) throws RemoteException;
}
