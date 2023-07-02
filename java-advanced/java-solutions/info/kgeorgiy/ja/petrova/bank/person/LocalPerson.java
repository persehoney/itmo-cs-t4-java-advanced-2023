package info.kgeorgiy.ja.petrova.bank.person;

import info.kgeorgiy.ja.petrova.bank.account.Account;
import info.kgeorgiy.ja.petrova.bank.account.LocalAccount;

import java.io.Serializable;
import java.util.Map;

public final class LocalPerson implements Person, Serializable {
    private final String name;
    private final String lastName;
    private final String passportId;
    private final Map<String, LocalAccount> accounts;

    public LocalPerson(String name, String lastName, String passportId, Map<String, LocalAccount> accounts) {
        this.name = name;
        this.lastName = lastName;
        this.passportId = passportId;
        this.accounts = accounts;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public String getPassportId() {
        return passportId;
    }

    public Account findAccountById(String accountId) {
        return accounts.get(accountId);
    }
}
