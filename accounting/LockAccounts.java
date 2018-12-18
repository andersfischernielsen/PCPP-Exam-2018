import java.util.Arrays;

public class LockAccounts implements Accounts {
    private volatile Integer[] accounts;

    public LockAccounts(int n) {
        accounts = new Integer[n];
        Arrays.fill(accounts, 0, accounts.length, 0);
    }

    public void init(int n) {
        synchronized (accounts) {
            accounts = new Integer[n];
            Arrays.fill(accounts, 0, accounts.length, 0);
        }
    }

    public int get(int account) {
        synchronized (accounts[account]) {
            return accounts[account];
        }
    }

    public int sumBalances() {
        synchronized (accounts) {
            int sum = 0;
            for (int i = 0; i < accounts.length; i++) {
                sum += accounts[i];
            }
            return sum;
        }
    }

    public void deposit(int to, int amount) {
        synchronized (accounts[to]) {
            accounts[to] += amount;
        }
    }

    public void transfer(int from, int to, int amount) {
        synchronized (accounts[from]) {
            synchronized (accounts[to]) {
                accounts[from] -= amount;
                accounts[to] += amount;
            }
        }
    }

    public void transferAccount(Accounts other) {
        synchronized (this) {
            synchronized (other) {
                for (int i = 0; i < accounts.length; i++) {
                    accounts[i] += other.get(i);
                }
            }
        }
    }

    public String toString() {
        String res = "";
        if (accounts.length > 0) {
            synchronized (accounts) {
                res = "" + accounts[0];
                for (int i = 1; i < accounts.length; i++) {
                    res = res + " " + accounts[i];
                }
            }
        }
        return res;
    }
}
