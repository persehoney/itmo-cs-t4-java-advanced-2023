package info.kgeorgiy.ja.petrova.bank.account;

public class RemoteAccount implements Account {
    private final String id;
    private final String personId;
    private int amount;

    public RemoteAccount(final String id, final String personId) {
        this.id = id;
        this.personId = personId;
        amount = 0;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getPersonId() { return personId; }

    @Override
    public synchronized int getAmount() {
        return amount;
    }

    @Override
    public synchronized void setAmount(final int amount) {
        this.amount = amount;
    }
}
