// Case Study 1 — Part (a): Plain BST
// MediFlow Hospital – Patient ID Indexing
// Insertion order: 20, 30, 35, 40, 45, 50, 60, 65, 70, 75, 80, 85, 90
// Nearly sorted → degenerates into a right-skewed chain

public class MediFlowBST {

    static class BSTNode {
        int key;
        BSTNode left, right;
        BSTNode(int key) { this.key = key; }
    }

    static BSTNode insert(BSTNode root, int key) {
        if (root == null) return new BSTNode(key);
        if (key < root.key) root.left  = insert(root.left,  key);
        else if (key > root.key) root.right = insert(root.right, key);
        return root;
    }

    // Height = number of edges on the longest root-to-leaf path
    static int height(BSTNode node) {
        if (node == null) return -1;          // -1 so a single-node tree has height 0
        return 1 + Math.max(height(node.left), height(node.right));
    }

    static void inorder(BSTNode node) {
        if (node == null) return;
        inorder(node.left);
        System.out.print(node.key + " ");
        inorder(node.right);
    }

    // In-order predecessor (largest in left subtree)
    static BSTNode maxNode(BSTNode node) {
        while (node.right != null) node = node.right;
        return node;
    }

    static BSTNode delete(BSTNode root, int key) {
        if (root == null) return null;
        if (key < root.key)      root.left  = delete(root.left,  key);
        else if (key > root.key) root.right = delete(root.right, key);
        else {
            // Node found
            if (root.left == null)  return root.right;
            if (root.right == null) return root.left;
            // Two children: replace with in-order predecessor
            BSTNode pred = maxNode(root.left);
            root.key  = pred.key;
            root.left = delete(root.left, pred.key);
        }
        return root;
    }

    public static void main(String[] args) {
        int[] ids = {20, 30, 35, 40, 45, 50, 60, 65, 70, 75, 80, 85, 90};
        BSTNode root = null;
        for (int id : ids) root = insert(root, id);

        int h = height(root);
        System.out.println("=== Plain BST ===");
        System.out.print("In-order traversal: ");
        inorder(root);
        System.out.println();
        System.out.println("Height (edges): " + h);

        // SLA check
        long hopTimeNs   = 200;           // nanoseconds per pointer dereference
        long slaBudgetNs = 5_000_000;     // 5 ms in nanoseconds
        long worstCaseNs = (long)(h + 1) * hopTimeNs; // h+1 nodes on worst path
        System.out.printf("%nSLA Analysis:%n");
        System.out.printf("  Worst-case hops     : %d  (tree height + 1 comparisons)%n", h + 1);
        System.out.printf("  Time per hop        : %d ns%n", hopTimeNs);
        System.out.printf("  Worst-case lookup   : %d ns%n", worstCaseNs);
        System.out.printf("  SLA budget          : %,d ns (5 ms)%n", slaBudgetNs);
        System.out.printf("  Budget consumed     : %.1f%%%n", 100.0 * worstCaseNs / slaBudgetNs);
        System.out.println("  Result              : " + (worstCaseNs < slaBudgetNs ? "MEETS SLA ✓" : "VIOLATES SLA ✗"));
        System.out.println();
        System.out.println("Structural reason: all 13 keys arrive in strictly increasing order.");
        System.out.println("Every new key is larger than the current root, so it is always");
        System.out.println("inserted as the rightmost node. The BST degenerates into a right-");
        System.out.println("skewed linked list with height n-1 = 12, giving O(n) lookup.");

        // Noon deletions: 30, 70, 50
        System.out.println("\n=== Plain BST after noon deletions (30, 70, 50) ===");
        root = delete(root, 30);
        root = delete(root, 70);
        root = delete(root, 50);
        int hPost = height(root);
        System.out.print("In-order traversal: ");
        inorder(root);
        System.out.println();
        System.out.printf("Height after deletions : %d%n", hPost);
        long postDeleteNs = (long)(hPost + 1) * hopTimeNs;
        System.out.printf("Worst-case lookup      : %d ns%n", postDeleteNs);
        System.out.printf("Budget consumed        : %.1f%%%n", 100.0 * postDeleteNs / slaBudgetNs);
    }
}
