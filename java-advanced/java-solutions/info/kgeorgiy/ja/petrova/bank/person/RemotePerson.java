package info.kgeorgiy.ja.petrova.bank.person;

import info.kgeorgiy.ja.petrova.bank.Bank;
import info.kgeorgiy.ja.petrova.bank.RemoteBank;

import java.rmi.RemoteException;
import java.util.Set;

public final class RemotePerson implements Person {
    private final String name;
    private final String lastName;
    private final String passportId;
    private final RemoteBank bank;

    public RemotePerson(RemoteBank bank, String name, String lastName, String passportId) {
        this.name = name;
        this.lastName = lastName;
        this.passportId = passportId;
        this.bank = bank;
    }

    @Override
    public String getName() throws RemoteException {
        return name;
    }

    @Override
    public String getLastName() throws RemoteException {
        return lastName;
    }

    @Override
    public String getPassportId() throws RemoteException {
        return passportId;
    }

    public Set<String> getAccounts() throws RemoteException {
        return bank.getAccounts(passportId);
    }
}
