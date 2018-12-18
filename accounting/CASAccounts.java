import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class CASAccounts implements Accounts {
    private volatile AtomicInteger[] accounts;
    private volatile AtomicInteger sum = new AtomicInteger();

    public CASAccounts(int n) {
        accounts = new AtomicInteger[n];
        for (int i = 0; i < accounts.length; i++) {
            accounts[i] = new AtomicInteger(0);
        }
    }

    public void init(int n) {
        accounts = new AtomicInteger[n];
        for (int i = 0; i < accounts.length; i++) {
            accounts[i] = new AtomicInteger(0);
        }
    }

    public int get(int account) {
        return accounts[account].get();
    }

    public int sumBalances() {
        return sum.get();
    }

    public void deposit(int to, int amount) {
        int previous, previousSum;
        do {
            previous = accounts[to].get();
            previousSum = sum.get();
        } while (!(accounts[to].compareAndSet(previous, previous + amount)
                && sum.compareAndSet(previousSum, previousSum + amount)));
    }

    public void transfer(int from, int to, int amount) {
        int previousTo, previousFrom;
        do {
            previousFrom = accounts[from].get();
            previousTo = accounts[to].get();
        } while (!(accounts[from].compareAndSet(previousFrom, previousFrom - amount)
                && accounts[to].compareAndSet(previousTo, previousTo + amount)));
    }

    public void transferAccount(Accounts other) {
        for (int i = 0; i < accounts.length; i++) {
            int previous, otherValue, sumPrevious;
            do {
                previous = accounts[i].get();
                otherValue = other.get(i);
                sumPrevious = sum.get();
            } while (otherValue == other.get(i) && !(accounts[i].compareAndSet(previous, otherValue + previous)
                    && sum.compareAndSet(sumPrevious, sumPrevious + otherValue)));
        }
    }

    public String toString() {
        String res = "";
        if (accounts.length > 0) {
            res = "" + accounts[0].get();
            for (int i = 1; i < accounts.length; i++) {
                res = res + " " + accounts[i].get();
            }
        }
        return res;
    }
}
