package info.kgeorgiy.ja.petrova.bank.account;

import java.io.Serializable;

public class LocalAccount implements Account, Serializable {
    private final String id;
    private final String personId;
    private int amount;

    public LocalAccount(final String id, final String personId) {
        this.id = id;
        this.personId = personId;
        amount = 0;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getPersonId() {
        return personId;
    }

    @Override
    public int getAmount() {
        return amount;
    }

    @Override
    public void setAmount(final int amount) {
        this.amount = amount;
    }
}
