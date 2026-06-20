import java.util.*;

/**
 * Module 6 Case Study: Coin Change DP
 * Scenario: RBI denominations {1, 2, 5, 10, 20}, amount = 43
 *
 * Contains:
 *  (a) Greedy "largest coin first" trace
 *  (b) Full DP table builder
 *  (c) minCoins() + coinsUsed() (with backtracking via choice[])
 *  (d) Canonical vs non-canonical demo using {1, 4, 6}, amount = 8
 */
public class CoinChangeDP {

    // ---------------------------------------------------------------
    // (a) GREEDY: largest-coin-first
    // ---------------------------------------------------------------
    static List<Integer> greedyCoins(int[] coins, int amount) {
        // sort descending so we always try the biggest denomination first
        Integer[] sorted = Arrays.stream(coins).boxed().toArray(Integer[]::new);
        Arrays.sort(sorted, Collections.reverseOrder());

        List<Integer> used = new ArrayList<>();
        int remaining = amount;

        System.out.println("Greedy trace for amount = " + amount + ":");
        for (int c : sorted) {
            while (remaining >= c) {
                used.add(c);
                remaining -= c;
                System.out.printf("  take %-3d -> remaining = %d, running total = %d, count = %d%n",
                        c, remaining, amount - remaining, used.size());
            }
        }
        if (remaining != 0) {
            System.out.println("  !! Greedy could not make exact change (remaining = " + remaining + ")");
        }
        return used;
    }

    // ---------------------------------------------------------------
    // (b) DP TABLE (just the array, for display purposes)
    // ---------------------------------------------------------------
    static int[] buildDPTable(int[] coins, int amount) {
        int[] dp = new int[amount + 1];
        Arrays.fill(dp, amount + 1); // sentinel = "impossible"
        dp[0] = 0;

        for (int i = 1; i <= amount; i++) {
            for (int c : coins) {
                if (c <= i && dp[i - c] != amount + 1) {
                    dp[i] = Math.min(dp[i], dp[i - c] + 1);
                }
            }
        }
        return dp;
    }

    static void printDPTable(int[] dp, int from, int to) {
        System.out.println("i   : " + rangeHeader(from, to));
        System.out.print("dp[i]:");
        for (int i = from; i <= to; i++) {
            System.out.printf(" %3d", dp[i] > dp.length ? -1 : dp[i]);
        }
        System.out.println();
    }

    private static String rangeHeader(int from, int to) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i <= to; i++) sb.append(String.format(" %3d", i));
        return sb.toString();
    }

    // ---------------------------------------------------------------
    // (c) minCoins() — TODO 1 filled in
    // ---------------------------------------------------------------
    static int minCoins(int[] coins, int amount) {
        int[] dp = new int[amount + 1];
        Arrays.fill(dp, amount + 1); // sentinel = impossible
        dp[0] = 0;

        for (int i = 1; i <= amount; i++) {
            for (int c : coins) {
                // TODO 1
                if (c <= i && dp[i - c] != amount + 1) {
                    dp[i] = Math.min(dp[i], dp[i - c] + 1);
                }
            }
        }
        return (dp[amount] == amount + 1) ? -1 : dp[amount];
    }

    // ---------------------------------------------------------------
    // (c) coinsUsed() — TODO 2 filled in (backtrace via choice[])
    // ---------------------------------------------------------------
    static List<Integer> coinsUsed(int[] coins, int amount) {
        int[] dp = new int[amount + 1];
        int[] choice = new int[amount + 1]; // which coin was used at i
        Arrays.fill(dp, amount + 1);
        dp[0] = 0;

        for (int i = 1; i <= amount; i++) {
            for (int c : coins) {
                if (c <= i && dp[i - c] + 1 < dp[i]) {
                    dp[i] = dp[i - c] + 1;
                    choice[i] = c; // record decision
                }
            }
        }

        if (dp[amount] == amount + 1) return new ArrayList<>(); // impossible

        // TODO 2: back-trace from `amount` using choice[]
        List<Integer> result = new ArrayList<>();
        int rem = amount; // separate variable so we don't mutate the parameter
        while (rem > 0) {
            result.add(choice[rem]);
            rem -= choice[rem];
        }
        return result;
    }

    // ---------------------------------------------------------------
    // MAIN — runs the full case study
    // ---------------------------------------------------------------
    public static void main(String[] args) {

        System.out.println("=========================================");
        System.out.println(" CASE STUDY: amount = 43, coins = {1,2,5,10,20}");
        System.out.println("=========================================\n");

        int[] coins = {1, 2, 5, 10, 20};
        int amount = 43;

        // ---- (a) Greedy ----
        List<Integer> greedy = greedyCoins(coins, amount);
        System.out.println("Greedy coins used : " + greedy);
        System.out.println("Greedy coin count : " + greedy.size());
        System.out.println();

        // ---- (b) DP table ----
        int[] dp = buildDPTable(coins, amount);
        System.out.println("DP table, dp[0..20]:");
        printDPTable(dp, 0, 20);
        System.out.println();
        System.out.println("dp[43] = " + dp[amount]);
        System.out.println();

        // ---- (c) minCoins + coinsUsed ----
        int minCount = minCoins(coins, amount);
        List<Integer> optimalCoins = coinsUsed(coins, amount);
        System.out.println("minCoins(coins, 43)   = " + minCount);
        System.out.println("coinsUsed(coins, 43)  = " + optimalCoins
                + "  (sum = " + optimalCoins.stream().mapToInt(Integer::intValue).sum() + ")");
        System.out.println();

        System.out.println("Greedy optimal here?  "
                + (greedy.size() == minCount ? "YES — greedy matches DP optimum." : "NO — greedy is suboptimal."));

        // ---- (d) Canonical vs non-canonical demo ----
        System.out.println("\n=========================================");
        System.out.println(" NON-CANONICAL DEMO: coins = {1,4,6}, amount = 8");
        System.out.println("=========================================\n");

        int[] weirdCoins = {1, 4, 6};
        int weirdAmount = 8;

        List<Integer> weirdGreedy = greedyCoins(weirdCoins, weirdAmount);
        int weirdDP = minCoins(weirdCoins, weirdAmount);
        List<Integer> weirdOptimal = coinsUsed(weirdCoins, weirdAmount);

        System.out.println("Greedy coins used : " + weirdGreedy + "  -> count = " + weirdGreedy.size());
        System.out.println("DP optimal count  : " + weirdDP);
        System.out.println("DP optimal coins  : " + weirdOptimal);
        System.out.println("Greedy optimal here? "
                + (weirdGreedy.size() == weirdDP ? "YES" : "NO — greedy is suboptimal, DP wins."));
    }
}
