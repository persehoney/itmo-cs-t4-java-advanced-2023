package info.kgeorgiy.ja.petrova.bank;

import info.kgeorgiy.ja.petrova.bank.account.Account;
import info.kgeorgiy.ja.petrova.bank.person.LocalPerson;
import info.kgeorgiy.ja.petrova.bank.person.Person;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

class ClientTest {
    private final Bank bank = new RemoteBank(8888);

    private final List<Person> persons = Arrays.asList(
            new LocalPerson("Georgiy", "Korneev", "123", new HashMap<>()),
            new LocalPerson("Nikolay", "Vedernikov", "110", new HashMap<>()),
            new LocalPerson("Andrew", "Stankevich", "2", new HashMap<>()),
            new LocalPerson("Dmitry", "Shtukenberg", "8", new HashMap<>())
    );

    @Test
    @DisplayName("Unregistered person tests")
    void unregisteredPersonTest() {
        try {
            Assertions.assertNull(bank.findRemotePerson("12663"));
            Assertions.assertNull(bank.findLocalPerson("202"));
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Find person tests")
    void findPersonTest() {
        persons.forEach((person -> {
            try {
                bank.registerPerson(person.getName(), person.getLastName(), person.getPassportId());
                Person foundPerson = bank.findRemotePerson(person.getPassportId());

                Assertions.assertEquals(foundPerson.getName(), person.getName());
                Assertions.assertEquals(foundPerson.getLastName(), person.getLastName());
                Assertions.assertEquals(foundPerson.getPassportId(), person.getPassportId());

                foundPerson = bank.findLocalPerson(person.getPassportId());

                Assertions.assertEquals(foundPerson.getName(), person.getName());
                Assertions.assertEquals(foundPerson.getLastName(), person.getLastName());
                Assertions.assertEquals(foundPerson.getPassportId(), person.getPassportId());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    @Test
    @DisplayName("Remote changes and localPerson")
    void remoteChangesTest() {
        try {
            String subId = "34";
            Person person = bank.registerPerson("Ksenia", "Petrova", "45");
            Account account = bank.createAccount(subId, person.getPassportId());
            account.setAmount(10);
            LocalPerson foundPerson = bank.findLocalPerson(person.getPassportId());
            Assertions.assertEquals(foundPerson.findAccountById(person.getPassportId() + ":" + subId).getAmount(), account.getAmount());
            account.setAmount(100);
            Assertions.assertEquals(foundPerson.findAccountById(person.getPassportId() + ":" + subId).getAmount(), 10);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Null account tests")
    void nullAccountTest() throws RemoteException {
        Assertions.assertNull(bank.getAccount("1"));
    }

    @Test
    @DisplayName("Not found owner")
    void notFoundOwnerTest() throws RemoteException {
        Assertions.assertNull(bank.createAccount("1", "13"));
    }

    @Test
    @DisplayName("Account tests")
    void accountTest() throws RemoteException {
        final Account account = bank.createAccount("1", "123");
        Assertions.assertEquals(account.getAmount(), 0);

        IntStream.range(0, 10).forEach((i) -> {
            try {
                account.setAmount(100 * i);
                Assertions.assertEquals(account.getAmount(), 100 * i);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    @DisplayName("Amount multithreading")
    void multithreadingTest() throws RemoteException {
        Person person = bank.registerPerson(persons.get(0).getName(), persons.get(0).getLastName(), persons.get(0).getPassportId());
        Account account = bank.createAccount("666", person.getPassportId());
        IntStream.range(0, 10).forEach(i -> new Thread(() -> {
            try {
                account.setAmount(i);
                Assertions.assertEquals(account.getAmount(), i);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }).start());
    }
}