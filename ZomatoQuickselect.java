// Case Study 5 — Zomato Delivery-ETA Median
// Quickselect (deterministic + randomised) and Median-of-Medians
// Find the k-th smallest ETA without fully sorting

import java.util.*;

public class ZomatoQuickselect {

    // ── Part (a): Deterministic trace — pivot = first element of subarray ────
    static int callDepth = 0;

    static int quickselectTrace(int[] arr, int lo, int hi, int k) {
        String indent = "  ".repeat(callDepth);
        System.out.printf("%sCall: arr[%d..%d] = %s, k=%d%n",
            indent, lo, hi, Arrays.toString(Arrays.copyOfRange(arr, lo, hi + 1)), k);

        if (lo == hi) {
            System.out.printf("%s  Base case reached: single element = %d%n", indent, arr[lo]);
            return arr[lo];
        }

        int pivotVal = arr[lo];
        int p = partitionLomuto(arr, lo, hi);
        System.out.printf("%s  Pivot chosen = %d (first element)%n", indent, pivotVal);
        System.out.printf("%s  After partition: %s  (pivot finalised at index %d)%n",
            indent, Arrays.toString(Arrays.copyOfRange(arr, lo, hi + 1)), p);
        System.out.printf("%s  Left  partition arr[%d..%d] = %s%n",
            indent, lo, p - 1, Arrays.toString(Arrays.copyOfRange(arr, lo, p)));
        System.out.printf("%s  Right partition arr[%d..%d] = %s%n",
            indent, p + 1, hi, Arrays.toString(Arrays.copyOfRange(arr, p + 1, hi + 1)));

        int rank = p - lo + 1; // 1-indexed rank of pivot within current subarray
        System.out.printf("%s  Rank of pivot within subarray = %d%n", indent, rank);

        if (k == rank) {
            System.out.printf("%s  k == rank -> FOUND median = %d%n", indent, arr[p]);
            return arr[p];
        } else if (k < rank) {
            System.out.printf("%s  k < rank -> recurse LEFT, same k=%d%n", indent, k);
            callDepth++;
            int res = quickselectTrace(arr, lo, p - 1, k);
            callDepth--;
            return res;
        } else {
            int newK = k - rank;
            System.out.printf("%s  k > rank -> recurse RIGHT, new k=%d-%d=%d%n", indent, k, rank, newK);
            callDepth++;
            int res = quickselectTrace(arr, p + 1, hi, newK);
            callDepth--;
            return res;
        }
    }

    /** Lomuto partition with pivot at arr[lo]. */
    static int partitionLomuto(int[] arr, int lo, int hi) {
        int pivot = arr[lo];
        int i = lo + 1;
        for (int j = lo + 1; j <= hi; j++) {
            if (arr[j] < pivot) { swap(arr, i, j); i++; }
        }
        swap(arr, lo, i - 1);
        return i - 1;
    }

    static void swap(int[] arr, int i, int j) {
        int t = arr[i]; arr[i] = arr[j]; arr[j] = t;
    }

    // ── Part (b): Randomised quickselect (TODOs filled in) ───────────────────

    /** Returns the k-th smallest (1-indexed) element of arr[lo..hi]. */
    static int quickselect(int[] arr, int lo, int hi, int k) {
        if (lo == hi) return arr[lo];

        // Random pivot — swap to position lo so partitionLomuto can start there.
        int pivotIdx = lo + new Random().nextInt(hi - lo + 1);
        swap(arr, lo, pivotIdx);

        int p = partitionLomuto(arr, lo, hi);
        // After partition, arr[lo..p-1] < arr[p] <= arr[p+1..hi]

        int rank = p - lo + 1;       // 1-indexed rank of pivot within arr[lo..hi]

        // TODO 1: if k == rank, return arr[p]
        if (k == rank) {
            return arr[p];
        }

        // TODO 2: if k < rank, recurse on the LEFT partition (lo to p-1) with same k
        if (k < rank) {
            return quickselect(arr, lo, p - 1, k);
        }

        // TODO 3: if k > rank, recurse on the RIGHT partition (p+1 to hi) with k - rank
        return quickselect(arr, p + 1, hi, k - rank);
    }

    static void complexityDiscussion() {
        System.out.println("\n=== Part (b)(iii): Complexity of randomised quickselect ===");
        System.out.println("(i)   Expected time complexity   : O(n)");
        System.out.println("      Each recursive call processes a subarray whose expected size");
        System.out.println("      shrinks by a constant factor (random pivot splits roughly evenly");
        System.out.println("      on average), giving the recurrence T(n) = T(n/2) + O(n) in");
        System.out.println("      expectation, which solves to O(n) total expected work.");
        System.out.println();
        System.out.println("(ii)  Worst-case time complexity : O(n^2)");
        System.out.println("      If the random pivot is unlucky every single time (always the");
        System.out.println("      smallest or largest remaining element), each partition only");
        System.out.println("      removes 1 element, giving T(n) = T(n-1) + O(n) = O(n^2).");
        System.out.println();
        System.out.println("(iii) Probability of worst case for random pivots:");
        System.out.println("      Vanishingly small — roughly (1/n)^(n) in the fully adversarial");
        System.out.println("      sense, but more precisely the probability of needing more than");
        System.out.println("      c*n comparisons decays exponentially in c. In practice the chance");
        System.out.println("      of hitting true O(n^2) behaviour on random pivots is astronomically");
        System.out.println("      small (comparable to '1/n!' style probabilities for adversarial");
        System.out.println("      worst-case pivot sequences), which is why randomised quickselect");
        System.out.println("      is considered O(n) expected with overwhelming practical confidence.");
    }

    // ── Part (c): Median-of-medians — deterministic O(n) worst case ─────────

    static int medianOfMediansSelect(int[] arr, int lo, int hi, int k) {
        while (true) {
            if (lo == hi) return arr[lo];

            int pivot = medianOfMedians(arr, lo, hi);
            int pivotIdx = -1;
            for (int i = lo; i <= hi; i++) if (arr[i] == pivot) { pivotIdx = i; break; }
            swap(arr, lo, pivotIdx);

            int p = partitionLomuto(arr, lo, hi);
            int rank = p - lo + 1;

            if (k == rank) return arr[p];
            else if (k < rank) hi = p - 1;
            else { k = k - rank; lo = p + 1; }
        }
    }

    /** Finds the median-of-medians pivot value for arr[lo..hi]. */
    static int medianOfMedians(int[] arr, int lo, int hi) {
        int n = hi - lo + 1;
        if (n <= 5) {
            insertionSort(arr, lo, hi);
            return arr[lo + (n - 1) / 2];
        }

        int numGroups = (int) Math.ceil(n / 5.0);
        int[] medians = new int[numGroups];
        for (int i = 0; i < numGroups; i++) {
            int groupLo = lo + i * 5;
            int groupHi = Math.min(groupLo + 4, hi);
            insertionSort(arr, groupLo, groupHi);
            medians[i] = arr[groupLo + (groupHi - groupLo) / 2];
        }

        // Recursively find the median of the medians array
        return medianOfMediansSelect(medians, 0, medians.length - 1, (medians.length + 1) / 2);
    }

    static void insertionSort(int[] arr, int lo, int hi) {
        for (int i = lo + 1; i <= hi; i++) {
            int key = arr[i];
            int j = i - 1;
            while (j >= lo && arr[j] > key) { arr[j + 1] = arr[j]; j--; }
            arr[j + 1] = key;
        }
    }

    static void recurrenceAnalysis() {
        System.out.println("\n=== Part (c): Median-of-medians recurrence analysis ===\n");
        System.out.println("Recurrence: T(n) = T(n/5) + T(7n/10) + O(n)");
        System.out.println();
        System.out.println("Where each term comes from:");
        System.out.println("  T(n/5)   : recursively finding the median of the n/5 group-medians");
        System.out.println("  T(7n/10) : recursively recursing into the larger partition after");
        System.out.println("             using that median as pivot (worst case keeps 7n/10 elements)");
        System.out.println("  O(n)     : grouping into n/5 groups + finding each group's median");
        System.out.println("             (O(1) per group of 5) + the Lomuto partition pass");
        System.out.println();
        System.out.println("Proof sketch that T(n) = O(n):");
        System.out.println("  Sum of the recursive-call fractions: 1/5 + 7/10 = 2/10 + 7/10 = 9/10 < 1");
        System.out.println();
        System.out.println("  This satisfies the condition for the Akra-Bazzi / 'decreasing");
        System.out.println("  fraction' recurrence pattern: if the total work fed into recursive");
        System.out.println("  calls is a constant fraction c < 1 of n (here c = 9/10), and each");
        System.out.println("  level does O(n) extra non-recursive work, then the total work across");
        System.out.println("  all levels forms a geometric series:");
        System.out.println("    T(n) <= O(n) * (1 + 9/10 + (9/10)^2 + (9/10)^3 + ...)");
        System.out.println("         = O(n) * 1/(1 - 9/10)");
        System.out.println("         = O(n) * 10");
        System.out.println("         = O(n)");
        System.out.println("  The geometric series converges because the common ratio 9/10 < 1,");
        System.out.println("  so the total work is bounded by a constant multiple of n -> O(n).");
        System.out.println();
        System.out.println("Why pivot = median-of-medians guarantees >= 30% elimination each side:");
        System.out.println("  At least half of the n/5 group-medians are <= the chosen pivot");
        System.out.println("  (that's the definition of being their median). For each of those");
        System.out.println("  >= n/10 groups, at least 3 of its 5 elements are <= the group");
        System.out.println("  median, hence <= the pivot (since the group median <= pivot).");
        System.out.println("  So at least 3 * (n/10) = 3n/10 elements are guaranteed <= the pivot.");
        System.out.println("  By symmetry, at least 3n/10 elements are guaranteed >= the pivot.");
        System.out.println("  This means the partition can never discard more than 7n/10 elements");
        System.out.println("  on either side -- i.e. it always cuts off AT LEAST 30% (3n/10) of");
        System.out.println("  the elements, which is exactly what bounds the recursive call to");
        System.out.println("  T(7n/10) in the worst case.");
    }

    // ── Part (d): Why median-of-medians isn't used in practice ──────────────
    static void practicalDiscussion() {
        System.out.println("\n=== Part (d): Why median-of-medians isn't used in practice ===\n");
        System.out.println("(i)   Constant factor: median-of-medians does roughly 5.4n to 10n");
        System.out.println("      comparisons in practice (grouping, sorting groups of 5, and the");
        System.out.println("      recursive calls on both the median-finding sub-problem and the");
        System.out.println("      main partition all add overhead), compared to randomised");
        System.out.println("      quickselect's roughly 2n to 4n expected comparisons -- making");
        System.out.println("      median-of-medians on the order of 10-30x slower in real");
        System.out.println("      benchmarks despite having the 'better' asymptotic worst case.");
        System.out.println();
        System.out.println("(ii)  Randomised quickselect's true O(n^2) worst case requires an");
        System.out.println("      adversarial sequence of pivot choices that is exponentially");
        System.out.println("      unlikely to occur by chance; for Zomato's 100,000-ETA median");
        System.out.println("      computed every 5 seconds, the realistic expected cost stays");
        System.out.println("      O(n) essentially always, so the rare theoretical bad case never");
        System.out.println("      materially affects the p99 dashboard latency in practice.");
        System.out.println();
        System.out.println("(iii) Median-of-medians is genuinely needed only when a HARD,");
        System.out.println("      adversary-proof worst-case bound is required regardless of input");
        System.out.println("      -- e.g. safety-critical or hard-real-time embedded systems where");
        System.out.println("      an adversarial or pathological input must never blow a deadline,");
        System.out.println("      or in theoretical algorithm design/proofs where a deterministic");
        System.out.println("      O(n) bound is needed as a building block for other guaranteed-");
        System.out.println("      time algorithms. For a logistics dashboard like Zomato's, where");
        System.out.println("      average-case throughput matters far more than a near-impossible");
        System.out.println("      worst case, randomised quickselect is the correct practical choice.");
    }

    // ── Main ──────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        int[] etas = {37, 12, 28, 45, 19, 33, 22, 50, 15};
        int n = etas.length;
        int k = (n + 1) / 2; // median = 5th smallest of 9

        System.out.println("=== Part (a): Deterministic Quickselect Trace (pivot = first element) ===");
        System.out.println("Input ETAs: " + Arrays.toString(etas));
        System.out.println("n = " + n + ", median = " + k + "th smallest\n");

        int[] arrCopy = etas.clone();
        int median = quickselectTrace(arrCopy, 0, n - 1, k);
        System.out.println("\nMedian ETA = " + median + " minutes");

        // Verify against full sort
        int[] sorted = etas.clone();
        Arrays.sort(sorted);
        System.out.println("Verification (sorted array): " + Arrays.toString(sorted));
        System.out.println("Sorted[" + (k - 1) + "] (0-indexed) = " + sorted[k - 1]);
        System.out.println("Match: " + (median == sorted[k - 1]));

        // Part (b): randomised quickselect demo
        System.out.println("\n=== Part (b): Randomised Quickselect (verification) ===");
        int[] arrRand = etas.clone();
        int medianRand = quickselect(arrRand, 0, n - 1, k);
        System.out.println("Randomised quickselect result: " + medianRand);
        System.out.println("Matches expected median (28): " + (medianRand == 28));

        complexityDiscussion();

        // Part (c): median-of-medians demo
        System.out.println("\n=== Part (c): Median-of-Medians Quickselect (verification) ===");
        int[] arrMoM = etas.clone();
        int medianMoM = medianOfMediansSelect(arrMoM, 0, n - 1, k);
        System.out.println("Median-of-medians result: " + medianMoM);
        System.out.println("Matches expected median (28): " + (medianMoM == 28));

        recurrenceAnalysis();
        practicalDiscussion();
    }
}
