import java.util.stream.*;
import java.util.function.*;

public class Runner {
    public static void main(String[] args) {
        final int n = 10000;
        var timer = new Timer();
        testAccounts(new UnsafeAccounts(n), n, false);
        var serialResult = timer.check();
        System.out.println("serialResult: " + serialResult);

        final int numberOfTransactions = 200000;
        timer = new Timer();
        applyTransactionsLoop(n, numberOfTransactions, () -> new UnsafeAccounts(n));
        var streamResult = timer.check();
        System.out.println("applyTransactionsLoop result: " + streamResult);
        // applyTransactionsCollect(n, numberOfTransactions, () -> new
        // UnsafeAccounts(n));
    }

    public static void testAccounts(Accounts accounts, final int n, boolean concurrent) {
        if (n <= 2) {
            System.out.println("Accounts must be larger that 2 for this test to work");
            assert (false); // test only supports larger accounts that 2.
            return;
        }
        assert (accounts.sumBalances() == 0);
        accounts.deposit(n - 1, 55);
        assert (accounts.get(n - 1) == 55);
        assert (accounts.get(n - 2) == 0);
        assert (accounts.sumBalances() == 55);
        accounts.deposit(0, 45);
        assert (accounts.sumBalances() == 100);

        accounts.transfer(0, n - 1, -10);
        assert (accounts.sumBalances() == 100);
        assert (accounts.get(n - 1) == 45);
        assert (accounts.get(0) == 55);
        accounts.transfer(1, n - 1, 10);
        assert (accounts.get(n - 1) == 55);
        assert (accounts.get(1) == -10);
        assert (accounts.get(0) == 55);
        assert (accounts.sumBalances() == 100);

        accounts.transferAccount(accounts);
        assert (accounts.get(n - 1) == 55 * 2);
        assert (accounts.get(1) == -10 * 2);
        assert (accounts.get(0) == 55 * 2);
        assert (accounts.sumBalances() == 200);

        System.out.printf(accounts.getClass() + " passed sequential tests\n");

        if (!concurrent)
            return;
        // ---- Concurrent tests ---- //
        accounts.deposit(n - 1, 100);
        for (int i = 0; i < 20000; i++) {
            new Thread(() -> {
                accounts.deposit(n - 1, 1);
                int value = accounts.get(n - 1);
                accounts.transfer(n - 1, n - 2, value);
                assert (accounts.get(n - 1) == 0);
            });
            new Thread(() -> {
                accounts.deposit(n - 1, 1);
                int value = accounts.get(n - 1);
                accounts.transfer(n - 1, n - 2, value);
                assert (accounts.get(n - 1) == 0);
            });
            new Thread(() -> {
                accounts.deposit(n - 1, 1);
                int value = accounts.get(n - 1);
                accounts.transfer(n - 1, n - 2, value);
                assert (accounts.get(n - 1) == 0);
            });
            new Thread(() -> {
                accounts.deposit(n - 1, 1);
                int value = accounts.get(n - 1);
                accounts.transfer(n - 1, n - 2, value);
                assert (accounts.get(n - 1) == 0);
            });
        }

        accounts.deposit(n - 1, 99);
        new Thread(() -> {
            while (accounts.get(n - 1) != 100) {
            }
            assert (true);
            System.out.println("I'm never run.");
        });
        new Thread(() -> {
            accounts.deposit(n - 1, 1);
            accounts.deposit(n - 1, 1);
            accounts.deposit(n - 1, 1);
        });

        System.out.printf(accounts.getClass() + " passed concurrent tests\n");
    }

    private static void printAccounts(Accounts accounts, int numberOfAccounts) {
        System.out.println("sumBalances is: " + accounts.sumBalances());
        if (numberOfAccounts <= 100) {
            System.out.println("accounts contain: ");
            for (int i = 0; i < numberOfAccounts; i++) {
                System.out.println("Account " + i + " is: " + accounts.get(i));
            }
        }
    }

    // Question 1.7.1
    private static void applyTransactionsLoop(int numberOfAccounts, int numberOfTransactions,
            Supplier<Accounts> generator) {
        final Accounts accounts = generator.get();
        Stream<Transaction> transaction = IntStream.range(0, numberOfTransactions).parallel()
                .mapToObj((i) -> new Transaction(numberOfAccounts, i));

        transaction.parallel().forEach(t -> {
            if (t.from == -1) {
                accounts.deposit(t.to, t.amount);
            } else {
                accounts.transfer(t.from, t.to, t.amount);
            }
        });
        // printAccounts(accounts, numberOfAccounts);
    }

    // Question 1.7.2
    private static void applyTransactionsCollect(int numberOfAccounts, int numberOfTransactions,
            Supplier<Accounts> generator) {
        Stream<Transaction> transactions = IntStream.range(0, numberOfTransactions).parallel()
                .mapToObj((i) -> new Transaction(numberOfAccounts, i));

        // var collect = transactions.collect(Collectors.mapping(t -> generator.get(),
        // Accounts::transferAccount));
        // var mapping = transactions.parallel().map(t -> {
        // var a = generator.get();
        // if (t.from == -1) {
        // a.deposit(t.to, t.amount);
        // } else {
        // a.transfer(t.from, t.to, t.amount);
        // }
        // return a;
        // }).collect(Accounts::transferAccount);
    }
}
