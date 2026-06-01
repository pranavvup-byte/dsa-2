// Case Study 2 — Uber Bengaluru: Segment Tree with Lazy Propagation
// Supports: range-add (update) + range-max (query) over n=16 zones
// Operations trace: 5 ops from 7:00-7:01 PM surge-multiplier trace

public class UberSegTree {

    // ── Segment Tree ──────────────────────────────────────────────────────────
    static class SegTreeLazy {
        double[] tree;   // max surge multiplier at each node
        double[] lazy;   // pending add-delta for each node
        int n;

        SegTreeLazy(int n) {
            this.n = n;
            tree = new double[4 * n];
            lazy = new double[4 * n];
            build(1, 0, n - 1);
        }

        // Build: all leaves start at 1.0
        void build(int node, int lo, int hi) {
            lazy[node] = 0;
            if (lo == hi) { tree[node] = 1.0; return; }
            int mid = (lo + hi) / 2;
            build(2 * node,     lo,      mid);
            build(2 * node + 1, mid + 1, hi);
            tree[node] = Math.max(tree[2 * node], tree[2 * node + 1]);
        }

        // ── pushDown: propagate pending lazy value to children ─────────────────
        void pushDown(int node) {
            if (lazy[node] != 0) {
                tree[2 * node]       += lazy[node];
                lazy[2 * node]       += lazy[node];
                tree[2 * node + 1]   += lazy[node];
                lazy[2 * node + 1]   += lazy[node];
                lazy[node] = 0;
            }
        }

        // ── updateRange (TODO filled in) ──────────────────────────────────────
        // Add delta to every zone in [l, r].
        // node = current tree index, [lo, hi] = segment covered by this node.
        void updateRange(int node, int lo, int hi, int l, int r, double delta) {
            // Case 1: No overlap — do nothing
            if (r < lo || hi < l) return;

            // Case 2: Full overlap — apply lazy mark, update max, return
            if (l <= lo && hi <= r) {
                tree[node] += delta;
                lazy[node] += delta;
                return;
            }

            // Case 3: Partial overlap — push down, recurse into children, pull up
            pushDown(node);
            int mid = (lo + hi) / 2;
            updateRange(2 * node,     lo,      mid, l, r, delta);
            updateRange(2 * node + 1, mid + 1, hi,  l, r, delta);
            tree[node] = Math.max(tree[2 * node], tree[2 * node + 1]);
        }

        // ── queryMax ──────────────────────────────────────────────────────────
        // Return maximum surge in zones [l, r].
        double queryMax(int node, int lo, int hi, int l, int r) {
            if (r < lo || hi < l) return Double.NEGATIVE_INFINITY;
            if (l <= lo && hi <= r) return tree[node];
            pushDown(node);
            int mid = (lo + hi) / 2;
            return Math.max(
                queryMax(2 * node,     lo,      mid, l, r),
                queryMax(2 * node + 1, mid + 1, hi,  l, r)
            );
        }

        // ── Public wrappers ───────────────────────────────────────────────────
        void update(int l, int r, double delta) {
            updateRange(1, 0, n - 1, l, r, delta);
        }

        double query(int l, int r) {
            return queryMax(1, 0, n - 1, l, r);
        }

        // Print current max values at all 16 leaf nodes
        void printLeaves() {
            System.out.print("  Zone values: [");
            for (int i = 0; i < n; i++) {
                double v = queryMax(1, 0, n - 1, i, i);
                System.out.printf("z%d=%.1f%s", i, v, i < n - 1 ? ", " : "");
            }
            System.out.println("]");
        }

        // Print internal node values (tree array, 1-indexed)
        void printInternalNodes() {
            System.out.println("  Internal node max values (tree[] indices 1-15):");
            for (int i = 1; i <= 15; i++) {
                if (tree[i] != 0 || lazy[i] != 0)
                    System.out.printf("    node[%2d]: max=%.1f  lazy=%.1f%n", i, tree[i], lazy[i]);
            }
        }
    }

    // ── Worst-case node count analysis ────────────────────────────────────────
    // For a range update or query, at most 4*log2(n) internal nodes are visited.
    // For n=16: log2(16) = 4, so at most 4*4 = 16 nodes per operation.
    static void analyseComplexity(int n) {
        int log2n = (int)(Math.log(n) / Math.log(2));
        System.out.printf("%nComplexity Analysis (n=%d):%n", n);
        System.out.printf("  log2(%d) = %d%n", n, log2n);
        System.out.printf("  Worst-case nodes visited per update/query: 4 * log2(n) = 4 * %d = %d%n",
            log2n, 4 * log2n);
        System.out.println("  Time complexity per operation: O(log n)");
    }

    // ── Fenwick tree argument ─────────────────────────────────────────────────
    static void fenwickArgument() {
        System.out.println("\n=== Part (c): Why Fenwick (BIT) is wrong for this workload ===");
        System.out.println();
        System.out.println("A Fenwick tree natively supports:");
        System.out.println("  • Point update       → O(log n)");
        System.out.println("  • Prefix sum query   → O(log n)");
        System.out.println();
        System.out.println("This workload needs:");
        System.out.println("  • Range-add update [l,r] → BIT can do this with difference-array");
        System.out.println("    trick (two BIT updates: +delta at l, -delta at r+1) → O(log n) ✓");
        System.out.println("  • Range-MAX query [l,r]  → CANNOT be answered in O(log n) with BIT ✗");
        System.out.println();
        System.out.println("Root cause: max is NOT invertible — you cannot undo a max the way you");
        System.out.println("can subtract a sum. BIT's prefix-query trick relies on inverting the");
        System.out.println("operation (prefix[r] - prefix[l-1]) which works for sums but not maxima.");
        System.out.println();
        System.out.println("To make Fenwick adequate, one of two changes is needed:");
        System.out.println("  1. Replace range-max queries with range-sum queries (change the");
        System.out.println("     workload) — then BIT handles both ops in O(log n).");
        System.out.println("  2. Restrict to point updates + range queries and precompute a sparse");
        System.out.println("     table for static max (no updates allowed).");
        System.out.println();
        System.out.println("Why sparse table is also wrong:");
        System.out.println("  A sparse table answers range-max in O(1) query time, but it is a");
        System.out.println("  STATIC structure — it does not support updates at all. Rebuilding");
        System.out.println("  it after each range-add costs O(n log n), far worse than O(log n).");
        System.out.println("  This workload has updates every few seconds, so sparse table is");
        System.out.println("  fundamentally unsuitable.");
    }

    // ── Main: trace all 5 operations ─────────────────────────────────────────
    public static void main(String[] args) {
        int n = 16;
        SegTreeLazy st = new SegTreeLazy(n);

        System.out.println("=== Uber Bengaluru Segment Tree Trace (n=16) ===");
        System.out.println("Initial state: all zones at 1.0\n");

        // Op 1: update [3,9] += 0.5
        System.out.println("Op 1: update [3,9] += 0.5  (M.G. Road event ends)");
        st.update(3, 9, 0.5);
        st.printLeaves();

        // Op 2: update [7,14] += 0.3
        System.out.println("\nOp 2: update [7,14] += 0.3  (Whitefield IT shift ends)");
        st.update(7, 14, 0.3);
        st.printLeaves();

        // Op 3: query max [0,15]
        System.out.println("\nOp 3: query max [0,15]");
        double q1 = st.query(0, 15);
        System.out.printf("  Result: max surge in [0,15] = %.1f%n", q1);
        System.out.println("  (zones 7-9 received both +0.5 and +0.3 → 1.0+0.5+0.3 = 1.8)");

        // Op 4: update [2,6] += 0.7
        System.out.println("\nOp 4: update [2,6] += 0.7  (cricket stadium emptying)");
        st.update(2, 6, 0.7);
        st.printLeaves();

        // Op 5: query max [4,10]
        System.out.println("\nOp 5: query max [4,10]");
        double q2 = st.query(4, 10);
        System.out.printf("  Result: max surge in [4,10] = %.1f%n", q2);
        System.out.println("  (zone 7: 1.0+0.5+0.3 = 1.8; zones 4-6: 1.0+0.5+0.7 = 2.2; answer = 2.2)");

        System.out.println("\nFinal zone values:");
        st.printLeaves();

        analyseComplexity(n);
        fenwickArgument();
    }
}
