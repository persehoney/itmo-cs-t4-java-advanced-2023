package info.kgeorgiy.ja.petrova.bank;

import info.kgeorgiy.ja.petrova.bank.account.Account;
import info.kgeorgiy.ja.petrova.bank.account.LocalAccount;
import info.kgeorgiy.ja.petrova.bank.account.RemoteAccount;
import info.kgeorgiy.ja.petrova.bank.person.LocalPerson;
import info.kgeorgiy.ja.petrova.bank.person.Person;
import info.kgeorgiy.ja.petrova.bank.person.RemotePerson;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemoteBank implements Bank {
    private final int port;
    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, RemotePerson> persons = new ConcurrentHashMap<>();

    public RemoteBank(final int port) {
        this.port = port;
    }

    @Override
    public Account createAccount(final String subId, final String personId) throws RemoteException {
        String accountId = personId + ":" + subId;
        System.out.println("Creating account " + accountId);
        final Account account = new RemoteAccount(accountId, personId);
        final RemotePerson person = findRemotePerson(personId);
        if (person == null) {
            System.err.println("Account's owner not found");
            return null;
        }
        if (accounts.putIfAbsent(accountId, account) == null) {
            UnicastRemoteObject.exportObject(account, port);
            return account;
        } else {
            return getAccount(accountId);
        }
    }

    @Override
    public Account getAccount(final String id) {
        System.out.println("Retrieving account " + id);
        if (accounts.get(id) == null) {
            System.err.println("No account with given id");
            return null;
        }
        return accounts.get(id);
    }

    @Override
    public Person registerPerson(String name, String lastName, String passportId) throws RemoteException {
        final RemotePerson person = new RemotePerson(this, name, lastName, passportId);
        if (persons.putIfAbsent(passportId, person) == null) {
            UnicastRemoteObject.exportObject(person, port);
            return person;
        } else {
            return getPerson(name, lastName, passportId);
        }
    }

    @Override
    public Person getPerson(String name, String lastName, String passportId) throws RemoteException {
        Person person = persons.get(passportId);
        if (person.getName().equals(name) && person.getLastName().equals(lastName)) {
            return person;
        } else {
            System.err.println("Incorrect credentials");
            return null;
        }
    }

    @Override
    public RemotePerson findRemotePerson(String passportId) throws RemoteException {
        if (persons.get(passportId) == null) {
            System.err.println("No person with given id");
            return null;
        }
        return persons.get(passportId);
    }

    @Override
    public LocalPerson findLocalPerson(String passportId) throws RemoteException {
        RemotePerson remotePerson = persons.get(passportId);
        if (remotePerson != null) {
            final ConcurrentMap<String, LocalAccount> localAccounts = new ConcurrentHashMap<>();
            getAccounts(passportId).forEach((accountId -> {
                try {
                    LocalAccount localAccount = new LocalAccount(accountId, findAccount(accountId).getPersonId());
                    localAccount.setAmount(findAccount(accountId).getAmount());
                    localAccounts.put(accountId, localAccount);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }));
            return new LocalPerson(remotePerson.getName(), remotePerson.getLastName(), remotePerson.getPassportId(), localAccounts);
        } else {
            System.err.println("No person with given id");
            return null;
        }
    }

    private Account findAccount(final String accountId) {
        return accounts.get(accountId);
    }

    @Override
    public Set<String> getAccounts(String personId) throws RemoteException {
        Set<String> personAccounts = new HashSet<>();
        for (Account account : accounts.values()) {
            if (account.getPersonId().equals(personId)) {
                personAccounts.add(account.getId());
            }
        }
        return personAccounts;
    }
}
