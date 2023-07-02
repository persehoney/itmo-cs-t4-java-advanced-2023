package info.kgeorgiy.ja.petrova.bank;

import info.kgeorgiy.ja.petrova.bank.account.Account;
import info.kgeorgiy.ja.petrova.bank.person.Person;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Client {
    public final static String BINDING_NAME = "rmi://localhost:8888/bank";

    public static void main(String[] args) {
        Server.main();

        final Bank bank;
        try {
            bank = (Bank) Naming.lookup(BINDING_NAME);
            if (args.length != 5) {
                System.err.println("5 arguments expected");
                return;
            }

            String name = args[0];
            String lastName = args[1];
            String passportId = args[2];
            String accountId = args[3];
            int amount = Integer.parseInt(args[4]);

            Person person = bank.registerPerson(name, lastName, passportId);
            Account account = bank.createAccount(accountId, person.getPassportId());
            account.setAmount(amount);
            System.out.println("New balance: " + account.getAmount());
            System.exit(0);
        } catch (MalformedURLException e) {
            System.err.println("Malformed URL");
            System.exit(1);
        } catch (NumberFormatException e) {
            System.err.println("Format: name, lastName, passportId, accountId, amount");
            System.exit(1);
        } catch (NotBoundException e) {
            System.err.println("Name isn't bound");
            System.exit(1);
        } catch (RemoteException e) {
            System.err.println("Unable to contact registry or export the object");
            System.exit(1);
        }
    }
}
