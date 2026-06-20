/**
 * Q2 - CO2: Flipkart Product Catalog — B+ Trees & Range Query Structures
 * Subject: DSA-2 (25SC1305E)
 *
 * Scenario:
 *   10^7 products indexed by (category, price) in a B+ tree.
 *   Each leaf holds 200 tuples; 8 KB pages; internal fanout = 200.
 *   95% queries are price-range scans; 87% return ~2800 products.
 *   Engineers debate adding a hash index for short-range queries.
 */

import java.util.*;

public class FlipkartBPlusTree {

    // ─────────────────────────────────────────────
    // B+ Tree Node
    // ─────────────────────────────────────────────
    static class BPlusNode {
        boolean isLeaf;
        int[]        keys;          // keys stored in this node
        String[]     categories;    // only used in leaves (parallel to keys)
        String[]     productIds;    // only used in leaves
        BPlusNode[]  children;      // only used in internal nodes
        BPlusNode    next;          // leaf-chain pointer (null on internal nodes)
        int          size;          // current number of keys

        BPlusNode(boolean isLeaf, int capacity) {
            this.isLeaf   = isLeaf;
            this.keys     = new int[capacity];
            this.size     = 0;
            if (isLeaf) {
                this.categories = new String[capacity];
                this.productIds = new String[capacity];
            } else {
                this.children = new BPlusNode[capacity + 1];
            }
        }
    }

    // ─────────────────────────────────────────────
    // B+ Tree  (order t => max 2t-1 keys per node)
    // ─────────────────────────────────────────────
    static class BPlusTree {
        static final int ORDER     = 3;   // small order for demo; in real life would be 200
        static final int MAX_KEYS  = 2 * ORDER - 1;
        BPlusNode root;

        BPlusTree() { root = new BPlusNode(true, MAX_KEYS); }

        // ── Find first leaf with key >= lo ──────
        BPlusNode findLeaf(BPlusNode node, int lo) {
            while (!node.isLeaf) {
                int i = 0;
                while (i < node.size && lo > node.keys[i]) i++;
                node = node.children[i];
            }
            return node;
        }

        // ── BUGGY range count (for demonstration) ──
        int rangeCountBuggy(BPlusNode root, int lo, int hi) {
            // BUG 1: Always descends to leftmost leaf (ignores lo)
            BPlusNode leaf = root;
            while (!leaf.isLeaf) {
                leaf = leaf.children[0]; // always go to leftmost child  ← BUG 1
            }
            int count = 0;
            while (leaf != null) {
                for (int k : Arrays.copyOf(leaf.keys, leaf.size)) {
                    if (lo <= k && k <= hi) count++;
                }
                leaf = leaf.next; // never break; walk to end            ← BUG 2
            }
            return count;
        }

        // ── FIXED range count ───────────────────
        int rangeCount(BPlusNode root, int lo, int hi) {
            // FIX 1: Descend to correct first leaf containing keys >= lo
            BPlusNode leaf = findLeaf(root, lo);

            int count = 0;
            while (leaf != null) {
                for (int i = 0; i < leaf.size; i++) {
                    int k = leaf.keys[i];
                    if (k > hi) return count; // FIX 2: early exit when past hi range
                    if (k >= lo) count++;
                }
                leaf = leaf.next;
            }
            return count;
        }

        // ── Range scan: returns matching (category, price, productId) ──
        List<String> rangeScan(BPlusNode root, String category, int lo, int hi) {
            BPlusNode leaf = findLeaf(root, lo);
            List<String> results = new ArrayList<>();
            while (leaf != null) {
                for (int i = 0; i < leaf.size; i++) {
                    int k = leaf.keys[i];
                    if (k > hi) return results; // early termination
                    if (k >= lo && leaf.categories[i].equals(category)) {
                        results.add(String.format("(%s, price=%d, id=%s)",
                                leaf.categories[i], k, leaf.productIds[i]));
                    }
                }
                leaf = leaf.next;
            }
            return results;
        }

        // ── Simple insert (for demo with small ORDER) ────────────────
        void insert(String category, int price, String productId) {
            BPlusNode r = root;
            if (r.size == MAX_KEYS) {
                BPlusNode newRoot = new BPlusNode(false, MAX_KEYS);
                newRoot.children[0] = r;
                splitChild(newRoot, 0, r);
                root = newRoot;
                insertNonFull(root, category, price, productId);
            } else {
                insertNonFull(r, category, price, productId);
            }
        }

        void insertNonFull(BPlusNode node, String category, int price, String productId) {
            int i = node.size - 1;
            if (node.isLeaf) {
                while (i >= 0 && price < node.keys[i]) {
                    node.keys[i + 1]       = node.keys[i];
                    node.categories[i + 1] = node.categories[i];
                    node.productIds[i + 1] = node.productIds[i];
                    i--;
                }
                node.keys[i + 1]       = price;
                node.categories[i + 1] = category;
                node.productIds[i + 1] = productId;
                node.size++;
            } else {
                while (i >= 0 && price < node.keys[i]) i--;
                i++;
                if (node.children[i].size == MAX_KEYS) {
                    splitChild(node, i, node.children[i]);
                    if (price > node.keys[i]) i++;
                }
                insertNonFull(node.children[i], category, price, productId);
            }
        }

        void splitChild(BPlusNode parent, int i, BPlusNode fullChild) {
            BPlusNode newChild = new BPlusNode(fullChild.isLeaf, MAX_KEYS);
            int mid = ORDER - 1;

            if (fullChild.isLeaf) {
                // Leaf split: copy right half into newChild, maintain leaf chain
                newChild.size = fullChild.size - mid;
                for (int j = 0; j < newChild.size; j++) {
                    newChild.keys[j]       = fullChild.keys[mid + j];
                    newChild.categories[j] = fullChild.categories[mid + j];
                    newChild.productIds[j] = fullChild.productIds[mid + j];
                }
                fullChild.size = mid;
                // Update leaf chain
                newChild.next = fullChild.next;
                fullChild.next = newChild;
                // Promote copy of first key of new leaf to parent
                for (int j = parent.size; j >= i + 1; j--) parent.keys[j] = parent.keys[j - 1];
                parent.keys[i] = newChild.keys[0];
            } else {
                // Internal split
                newChild.size = ORDER - 1;
                for (int j = 0; j < newChild.size; j++) {
                    newChild.keys[j]     = fullChild.keys[mid + 1 + j];
                }
                for (int j = 0; j <= newChild.size; j++) {
                    newChild.children[j] = fullChild.children[mid + 1 + j];
                }
                fullChild.size = mid;
                for (int j = parent.size; j >= i + 1; j--) parent.keys[j] = parent.keys[j - 1];
                parent.keys[i] = fullChild.keys[mid];
            }
            // Shift parent's child pointers
            for (int j = parent.size + 1; j >= i + 2; j--) parent.children[j] = parent.children[j - 1];
            parent.children[i + 1] = newChild;
            parent.size++;
        }

        // ── Print all leaves in order (leaf chain) ──
        void printLeaves() {
            BPlusNode node = root;
            while (!node.isLeaf) node = node.children[0];
            System.out.print("Leaves: ");
            while (node != null) {
                System.out.print("[");
                for (int i = 0; i < node.size; i++) {
                    System.out.print(node.categories[i] + ":" + node.keys[i]);
                    if (i < node.size - 1) System.out.print(", ");
                }
                System.out.print("] → ");
                node = node.next;
            }
            System.out.println("null");
        }

        int treeHeight() {
            int h = 0;
            BPlusNode node = root;
            while (!node.isLeaf) { node = node.children[0]; h++; }
            return h;
        }
    }

    // ─────────────────────────────────────────────
    // I/O Cost Model (matches scenario numbers)
    // ─────────────────────────────────────────────
    static void deriveBPlusTreeStats() {
        System.out.println("══════════════════════════════════════════════════════════");
        System.out.println(" SUB-QUESTION (a): B+ Tree Height & I/O Cost");
        System.out.println("══════════════════════════════════════════════════════════");

        long   N             = 10_000_000L;  // 10^7 products
        int    tuplesPerLeaf = 200;
        int    fanout        = 200;
        int    resultSize    = 2800;

        long   leafPages     = (long) Math.ceil((double) N / tuplesPerLeaf);
        int    level2        = (int)  Math.ceil((double) leafPages / fanout);
        int    level1        = (int)  Math.ceil((double) level2   / fanout);
        int    treeHeight    = (level1 == 1) ? 3 : 4; // root + internals + leaves

        int    descentCost   = treeHeight - 1;
        int    leafWalkCost  = (int) Math.ceil((double) resultSize / tuplesPerLeaf);
        int    totalIO       = descentCost + leafWalkCost;

        System.out.printf("  N (products)       = %,d%n", N);
        System.out.printf("  Tuples per leaf    = %d%n",  tuplesPerLeaf);
        System.out.printf("  Leaf pages         = ceil(%,d / %d) = %,d%n", N, tuplesPerLeaf, leafPages);
        System.out.printf("  Level above leaves = ceil(%,d / %d) = %d internal nodes%n", leafPages, fanout, level2);
        System.out.printf("  Root level         = ceil(%d / %d) = %d%n", level2, fanout, level1);
        System.out.printf("  Tree Height        = %d levels%n", treeHeight);
        System.out.println();
        System.out.printf("  Typical range query returns ~%d products:%n", resultSize);
        System.out.printf("    Descent cost     = height - 1 = %d page reads%n", descentCost);
        System.out.printf("    Leaf walk cost   = ceil(%d / %d) = %d page reads%n", resultSize, tuplesPerLeaf, leafWalkCost);
        System.out.printf("    TOTAL I/O        = %d + %d = %d page reads%n", descentCost, leafWalkCost, totalIO);
        System.out.printf("  Dominant term      = Leaf-chain walk (%d >> %d)%n", leafWalkCost, descentCost);
    }

    // ─────────────────────────────────────────────
    // Bug Analysis
    // ─────────────────────────────────────────────
    static void bugAnalysis() {
        System.out.println("\n══════════════════════════════════════════════════════════");
        System.out.println(" SUB-QUESTION (b): Bug Identification in rangeCount()");
        System.out.println("══════════════════════════════════════════════════════════");

        long totalLeaves = (long) Math.ceil(10_000_000.0 / 200);
        int  resultLeaves = (int) Math.ceil(2800.0 / 200);

        System.out.println("  BUG 1 — Wrong descent (always leftmost child):");
        System.out.println("    Code:  leaf = leaf.children[0]  // always go to leftmost child");
        System.out.println("    Problem: Ignores the lo bound; always starts at the very first leaf.");
        System.out.printf ("    I/O Impact: Reads ~%,d extra leaves before reaching lo.%n",
                totalLeaves / 2); // assume lo is at midpoint on average

        System.out.println("\n  BUG 2 — No early termination after hi:");
        System.out.println("    Code:  leaf = leaf.next;  // never break; walk to end");
        System.out.println("    Problem: Continues scanning ALL remaining leaves even after key > hi.");
        System.out.printf ("    I/O Impact: Reads ~%,d extra leaves beyond hi.%n",
                totalLeaves - resultLeaves - totalLeaves / 2);

        System.out.println("\n  OPTIMAL (corrected) I/O for ~2800 results:");
        System.out.printf ("    Descent=2 + Leaf-walk=%d = %d page reads total%n",
                resultLeaves, 2 + resultLeaves);
        System.out.printf ("    Buggy version: up to ~%,d page reads%n", totalLeaves);
    }

    // ─────────────────────────────────────────────
    // Hash Index Analysis
    // ─────────────────────────────────────────────
    static void hashIndexAnalysis() {
        System.out.println("\n══════════════════════════════════════════════════════════");
        System.out.println(" SUB-QUESTION (c): Hash Index Evaluation");
        System.out.println("══════════════════════════════════════════════════════════");

        int    tuplesPerLeaf = 200;
        int    descentCost   = 2;     // log_200(50000) levels
        double nsPerPageRead = 200.0; // nanoseconds

        // For result size r:
        //   B+ tree cost = descent + ceil(r / tuplesPerLeaf)
        //   Hash cost    = r individual probes (no clustering)
        // Break-even: descent + ceil(r/200) = r  =>  r - ceil(r/200) = 2

        System.out.println("  B+ tree cost(r)  = descent + ceil(r / 200)  page reads");
        System.out.println("  Hash index cost  = r individual hash probes  (no range benefit)");
        System.out.println();

        System.out.printf("  87%% of queries return ~2800 results:%n");
        int r = 2800;
        int bplusCost = descentCost + (int) Math.ceil((double) r / tuplesPerLeaf);
        System.out.printf("    B+ tree: %d + ceil(%d/200) = %d reads  (%.1f µs)%n",
                descentCost, r, bplusCost, bplusCost * nsPerPageRead / 1000);
        System.out.printf("    Hash:    %d reads  (%.1f µs)%n",
                r, r * nsPerPageRead / 1000);
        System.out.printf("    Winner:  B+ tree (%.0fx fewer reads)%n", (double) r / bplusCost);

        System.out.println("\n  Break-even analysis (find smallest r where hash beats B+):");
        for (int ri = 1; ri <= 20; ri++) {
            int bplus = descentCost + (int) Math.ceil((double) ri / tuplesPerLeaf);
            System.out.printf("    r=%2d  B+=%d  Hash=%2d  -> %s%n",
                    ri, bplus, ri, ri < bplus ? "Hash WINS" : "B+ wins");
        }

        System.out.println();
        System.out.println("  VERDICT: Hash index is a NET LOSS for this workload.");
        System.out.println("  Hash only beats B+ tree for r < 3 products per query.");
        System.out.println("  With 87% of queries returning ~2800 products, hash is 175x worse.");
        System.out.println("  Recommendation: Keep B+ tree only. No hash index needed.");
    }

    // ─────────────────────────────────────────────
    // Main
    // ─────────────────────────────────────────────
    public static void main(String[] args) {

        // ── Theoretical analysis (matches 10^7 scale) ─
        deriveBPlusTreeStats();
        bugAnalysis();
        hashIndexAnalysis();

        // ── Live B+ tree demo (small scale) ─────────
        System.out.println("\n══════════════════════════════════════════════════════════");
        System.out.println(" DEMO — B+ Tree with sample electronics products");
        System.out.println("══════════════════════════════════════════════════════════");

        BPlusTree tree = new BPlusTree();

        // Insert sample products (category, price, productId)
        int[][] prices = {
            {11800}, {12300}, {12900}, {13500}, {14100}, {14700}, {15400}, {16200},
            {9500},  {17000}, {8900},  {19500}
        };
        String[] pids = {"E001","E002","E003","E004","E005","E006","E007","E008",
                         "E009","E010","E011","E012"};

        for (int i = 0; i < prices.length; i++) {
            tree.insert("electronics", prices[i][0], pids[i]);
        }

        System.out.println("\nLeaf chain after all insertions:");
        tree.printLeaves();
        System.out.println("Tree height: " + tree.treeHeight() + " levels");

        // Range scan: electronics, price ∈ [12000, 14800]
        System.out.println("\nRange scan: electronics, price ∈ [12000, 14800]");
        List<String> results = tree.rangeScan(tree.root, "electronics", 12000, 14800);
        System.out.println("Results (" + results.size() + " products):");
        results.forEach(r -> System.out.println("  " + r));

        // Compare buggy vs fixed rangeCount
        System.out.println("\nrangeCount comparison [12000, 14800]:");
        System.out.println("  Buggy  count = " + tree.rangeCountBuggy(tree.root, 12000, 14800));
        System.out.println("  Fixed  count = " + tree.rangeCount(tree.root, 12000, 14800));
    }
}
