// Case Study 1 — Parts (b) & (c): AVL Tree
// MediFlow Hospital — Patient ID Indexing
// Answers the TODOs from the question boilerplate exactly.

public class MediFlowAVL {

    // ── Node ──────────────────────────────────────────────────────────────────
    static class AVLNode {
        int key;
        AVLNode left, right;
        int height = 1;
        AVLNode(int key) { this.key = key; }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    static int height(AVLNode n)  { return n == null ? 0 : n.height; }
    static int balance(AVLNode n) { return n == null ? 0 : height(n.left) - height(n.right); }
    static void updateHeight(AVLNode n) {
        if (n != null) n.height = 1 + Math.max(height(n.left), height(n.right));
    }

    // ── TODO 1: Right rotation (fixes LL imbalance) ───────────────────────────
    // Before:        y            After:     x
    //               / \                     / \
    //              x   T3                 T1   y
    //             / \                         / \
    //            T1  T2                      T2  T3
    static AVLNode rotateRight(AVLNode y) {
        AVLNode x  = y.left;
        AVLNode T2 = x.right;

        x.right = y;
        y.left  = T2;

        updateHeight(y);   // update lower node first
        updateHeight(x);
        return x;          // x is the new subtree root
    }

    // ── TODO 2: Left rotation (fixes RR imbalance) ────────────────────────────
    // Before:    x               After:       y
    //           / \                          / \
    //          T1   y                       x  T3
    //              / \                     / \
    //             T2  T3                  T1  T2
    static AVLNode rotateLeft(AVLNode x) {
        AVLNode y  = x.right;
        AVLNode T2 = y.left;

        y.left  = x;
        x.right = T2;

        updateHeight(x);   // update lower node first
        updateHeight(y);
        return y;          // y is the new subtree root
    }

    // ── TODO 3: Insert with rebalancing ───────────────────────────────────────
    static int rotationCount = 0;   // for tracing

    static AVLNode insert(AVLNode node, int key) {
        // 1. Standard BST insert
        if (node == null) return new AVLNode(key);
        if (key < node.key) node.left  = insert(node.left,  key);
        else if (key > node.key) node.right = insert(node.right, key);
        else return node;  // duplicate keys not inserted

        // 2. Update height of current ancestor
        updateHeight(node);

        // 3. Get balance factor to check for imbalance
        int bf = balance(node);

        // ── Four AVL cases ────────────────────────────────────────────────────

        // LL Case: right rotation
        if (bf > 1 && key < node.left.key) {
            rotationCount++;
            System.out.printf("  [RR after insert %2d] Pivot=%d  bf(pivot)=%d  bf(pivot.right)=%d%n",
                key, node.key, balance(node), balance(node.right));
            return rotateRight(node);
        }

        // RR Case: left rotation
        if (bf < -1 && key > node.right.key) {
            rotationCount++;
            System.out.printf("  [LL after insert %2d] Pivot=%d  bf(pivot)=%d  bf(pivot.left)=%d%n",
                key, node.key, balance(node), balance(node.left));
            return rotateLeft(node);
        }

        // LR Case: left-rotate left child, then right-rotate node
        if (bf > 1 && key > node.left.key) {
            rotationCount++;
            System.out.printf("  [LR after insert %2d] Pivot=%d%n", key, node.key);
            node.left = rotateLeft(node.left);
            return rotateRight(node);
        }

        // RL Case: right-rotate right child, then left-rotate node
        if (bf < -1 && key < node.right.key) {
            rotationCount++;
            System.out.printf("  [RL after insert %2d] Pivot=%d%n", key, node.key);
            node.right = rotateRight(node.right);
            return rotateLeft(node);
        }

        return node;
    }

    // ── Delete ────────────────────────────────────────────────────────────────
    static AVLNode minNode(AVLNode n) {
        while (n.left != null) n = n.left;
        return n;
    }

    static AVLNode delete(AVLNode root, int key) {
        if (root == null) return null;

        if (key < root.key)      root.left  = delete(root.left,  key);
        else if (key > root.key) root.right = delete(root.right, key);
        else {
            // Node to delete found
            if (root.left == null)  return root.right;
            if (root.right == null) return root.left;
            // Two children: replace with in-order successor
            AVLNode succ = minNode(root.right);
            root.key   = succ.key;
            root.right = delete(root.right, succ.key);
        }

        updateHeight(root);
        int bf = balance(root);

        // LL
        if (bf > 1 && balance(root.left) >= 0) {
            rotationCount++;
            System.out.printf("  [Delete rebalance RR] Pivot=%d  bf=%d%n", root.key, bf);
            return rotateRight(root);
        }
        // LR
        if (bf > 1 && balance(root.left) < 0) {
            rotationCount++;
            System.out.printf("  [Delete rebalance LR] Pivot=%d  bf=%d%n", root.key, bf);
            root.left = rotateLeft(root.left);
            return rotateRight(root);
        }
        // RR
        if (bf < -1 && balance(root.right) <= 0) {
            rotationCount++;
            System.out.printf("  [Delete rebalance LL] Pivot=%d  bf=%d%n", root.key, bf);
            return rotateLeft(root);
        }
        // RL
        if (bf < -1 && balance(root.right) > 0) {
            rotationCount++;
            System.out.printf("  [Delete rebalance RL] Pivot=%d  bf=%d%n", root.key, bf);
            root.right = rotateRight(root.right);
            return rotateLeft(root);
        }
        return root;
    }

    // ── Pretty-print the tree ─────────────────────────────────────────────────
    static void printTree(AVLNode node, String prefix, boolean isLeft) {
        if (node == null) return;
        System.out.println(prefix + (isLeft ? "├── " : "└── ")
            + node.key + " (h=" + node.height + ", bf=" + balance(node) + ")");
        printTree(node.left,  prefix + (isLeft ? "│   " : "    "), true);
        printTree(node.right, prefix + (isLeft ? "│   " : "    "), false);
    }

    static void inorder(AVLNode node) {
        if (node == null) return;
        inorder(node.left);
        System.out.print(node.key + " ");
        inorder(node.right);
    }

    // ── Main ──────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        int[] ids = {20, 30, 35, 40, 45, 50, 60, 65, 70, 75, 80, 85, 90};

        System.out.println("=== AVL Tree — Insertions ===");
        AVLNode root = null;
        for (int id : ids) {
            root = insert(root, id);
        }

        System.out.println("\nFinal AVL tree structure:");
        printTree(root, "", false);
        System.out.print("\nIn-order (should be sorted): ");
        inorder(root);
        System.out.println();
        System.out.println("Height: " + (root.height - 1) + " edges");
        System.out.println("Total rotations during insertion: " + rotationCount);

        // SLA check
        long hopTimeNs   = 200;
        long slaBudgetNs = 5_000_000;
        long worstCaseNs = (long) root.height * hopTimeNs;
        System.out.printf("%nSLA Analysis (post-insert):%n");
        System.out.printf("  Worst-case hops : %d%n", root.height);
        System.out.printf("  Lookup time     : %d ns%n", worstCaseNs);
        System.out.printf("  Budget consumed : %.1f%%%n", 100.0 * worstCaseNs / slaBudgetNs);
        System.out.println("  Result          : " + (worstCaseNs < slaBudgetNs ? "MEETS SLA ✓" : "VIOLATES SLA ✗"));

        // ── Part (c): Noon deletions ─────────────────────────────────────────
        rotationCount = 0;
        System.out.println("\n=== Part (c): Noon Deletions — 30, 70, 50 ===");
        System.out.println("Deleting 30...");
        root = delete(root, 30);
        System.out.println("Deleting 70...");
        root = delete(root, 70);
        System.out.println("Deleting 50...");
        root = delete(root, 50);

        System.out.println("\nFinal AVL tree after deletions:");
        printTree(root, "", false);
        System.out.print("In-order: ");
        inorder(root);
        System.out.println();
        System.out.println("Height after deletions: " + (root.height - 1) + " edges");
        System.out.println("Total rotations during deletion: " + rotationCount);

        long postNs = (long) root.height * hopTimeNs;
        System.out.printf("%nSLA Analysis (post-delete AVL):%n");
        System.out.printf("  Worst-case lookup : %d ns  (%.1f%% of 5 ms budget)%n",
            postNs, 100.0 * postNs / slaBudgetNs);

        // Compare with plain BST worst case after deletion
        // Plain BST remains a near-chain of 10 nodes (deleting 30,50,70 doesn't rebalance)
        int bstHeight = 9; // measured from plain BST simulation
        long bstNs    = (long)(bstHeight + 1) * hopTimeNs;
        System.out.printf("  Plain BST equiv   : %d ns  (%.1f%% of 5 ms budget)%n",
            bstNs, 100.0 * bstNs / slaBudgetNs);
    }
}
