/**
 * Q3 - CO3: Bangalore Metro Phase-3 Expansion
 * Case Study: Graph Traversal & Minimum Spanning Trees
 * Subject: DSA-2 (25SC1305E)
 *
 * Scenario:
 *   7 stations: M(Majestic), K(Kadugodi), W(Whitefield),
 *               S(Sarjapur), E(Electronic City Ext),
 *               Y(Yeshwanthpur West), H(Hebbal North)
 *   12 candidate edges with construction cost (Rs-crore).
 *   Constraints:
 *     (i)  Minimise total construction cost (MST)
 *     (ii) Network must stay connected
 *     (iii) Redundancy mandate: >= 2 edge-disjoint paths between M and W
 */

import java.util.*;

public class BangaloreMetroMST {

    // ─────────────────────────────────────────────
    // Station enum for readability
    // ─────────────────────────────────────────────
    static final String[] STATION = {"M", "K", "W", "S", "E", "Y", "H"};
    // Index:                           0    1    2    3    4    5    6

    static int idx(String s) {
        for (int i = 0; i < STATION.length; i++)
            if (STATION[i].equals(s)) return i;
        return -1;
    }

    // ─────────────────────────────────────────────
    // Edge
    // ─────────────────────────────────────────────
    static class Edge implements Comparable<Edge> {
        int u, v, weight;
        String label;

        Edge(String u, String v, int weight) {
            this.u      = idx(u);
            this.v      = idx(v);
            this.weight = weight;
            this.label  = u + "-" + v + "(" + weight + ")";
        }

        @Override public int compareTo(Edge o) { return this.weight - o.weight; }

        @Override public String toString() { return label; }
    }

    // ─────────────────────────────────────────────
    // NAIVE Union-Find (no rank, no path compression)
    // ─────────────────────────────────────────────
    static class UnionFindNaive {
        int[] parent;
        int   findHops = 0; // tracks total pointer hops for analysis

        UnionFindNaive(int n) {
            parent = new int[n];
            for (int i = 0; i < n; i++) parent[i] = i;
        }

        int find(int x) { // NO path compression
            while (parent[x] != x) {
                x = parent[x];
                findHops++;
            }
            return x;
        }

        boolean union(int x, int y) { // NO union-by-rank
            int rx = find(x), ry = find(y);
            if (rx == ry) return false;
            parent[rx] = ry; // always attach root of x under root of y
            return true;
        }

        String parentState() {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < parent.length; i++) {
                sb.append(STATION[i]).append("->").append(STATION[parent[i]]);
                if (i < parent.length - 1) sb.append(", ");
            }
            sb.append("]");
            return sb.toString();
        }
    }

    // ─────────────────────────────────────────────
    // OPTIMISED Union-Find (union-by-rank + path compression)
    // ─────────────────────────────────────────────
    static class UnionFindOptimised {
        int[] parent, rank;

        UnionFindOptimised(int n) {
            parent = new int[n];
            rank   = new int[n];
            for (int i = 0; i < n; i++) parent[i] = i;
        }

        int find(int x) { // WITH path compression
            if (parent[x] != x) parent[x] = find(parent[x]);
            return parent[x];
        }

        boolean union(int x, int y) { // WITH union-by-rank
            int rx = find(x), ry = find(y);
            if (rx == ry) return false;
            if (rank[rx] < rank[ry])      parent[rx] = ry;
            else if (rank[rx] > rank[ry]) parent[ry] = rx;
            else { parent[ry] = rx; rank[rx]++; }
            return true;
        }
    }

    // ─────────────────────────────────────────────
    // Kruskal's Algorithm (Naive)
    // ─────────────────────────────────────────────
    static List<Edge> kruskalNaive(int n, List<Edge> edges) {
        Collections.sort(edges);
        UnionFindNaive uf  = new UnionFindNaive(n);
        List<Edge>     mst = new ArrayList<>();

        System.out.println("\nKruskal's step-by-step (union-find evolution):");
        System.out.printf("  %-22s %-8s %-45s%n", "Edge", "Decision", "Parent state after");
        System.out.println("  " + "─".repeat(80));

        for (Edge e : edges) {
            int ru = uf.find(e.u), rv = uf.find(e.v);
            if (ru != rv) {
                uf.union(e.u, e.v);
                mst.add(e);
                System.out.printf("  %-22s %-8s %s%n", e.label, "ACCEPT", uf.parentState());
            } else {
                System.out.printf("  %-22s %-8s %s%n", e.label, "REJECT", uf.parentState());
            }
            if (mst.size() == n - 1) break; // MST complete
        }
        // Continue printing remaining rejected edges
        boolean started = mst.size() == n - 1;
        if (started) {
            for (Edge e : edges) {
                if (mst.contains(e)) continue;
                int ru = uf.find(e.u), rv = uf.find(e.v);
                if (ru == rv)
                    System.out.printf("  %-22s %-8s %s%n", e.label, "REJECT", uf.parentState());
            }
        }
        System.out.printf("%n  Total find() pointer hops (naive): %d%n", uf.findHops);
        return mst;
    }

    // ─────────────────────────────────────────────
    // Kruskal's Algorithm (Optimised)
    // ─────────────────────────────────────────────
    static List<Edge> kruskalOptimised(int n, List<Edge> edges) {
        Collections.sort(edges);
        UnionFindOptimised uf  = new UnionFindOptimised(n);
        List<Edge>         mst = new ArrayList<>();
        for (Edge e : edges) {
            if (uf.union(e.u, e.v)) {
                mst.add(e);
                if (mst.size() == n - 1) break;
            }
        }
        return mst;
    }

    // ─────────────────────────────────────────────
    // Redundancy: BFS to find all paths M -> W
    // (used to verify edge-disjoint paths)
    // ─────────────────────────────────────────────
    static List<List<Integer>> findAllPaths(List<Edge> edgeSet, int src, int dst, int n) {
        // Build adjacency list
        List<List<int[]>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        for (Edge e : edgeSet) {
            adj.get(e.u).add(new int[]{e.v, e.weight});
            adj.get(e.v).add(new int[]{e.u, e.weight});
        }

        List<List<Integer>> allPaths = new ArrayList<>();
        Deque<List<Integer>> queue   = new ArrayDeque<>();
        queue.add(new ArrayList<>(Collections.singletonList(src)));

        while (!queue.isEmpty()) {
            List<Integer> path = queue.poll();
            int last = path.get(path.size() - 1);
            if (last == dst) {
                allPaths.add(path);
                continue;
            }
            for (int[] nb : adj.get(last)) {
                if (!path.contains(nb[0])) {
                    List<Integer> newPath = new ArrayList<>(path);
                    newPath.add(nb[0]);
                    queue.add(newPath);
                }
            }
        }
        return allPaths;
    }

    static boolean areEdgeDisjoint(List<Integer> p1, List<Integer> p2, List<Edge> edgeSet) {
        Set<String> edgesP1 = new HashSet<>();
        for (int i = 0; i < p1.size() - 1; i++) {
            int a = p1.get(i), b = p1.get(i + 1);
            edgesP1.add(Math.min(a, b) + "-" + Math.max(a, b));
        }
        for (int i = 0; i < p2.size() - 1; i++) {
            int a = p2.get(i), b = p2.get(i + 1);
            String key = Math.min(a, b) + "-" + Math.max(a, b);
            if (edgesP1.contains(key)) return false;
        }
        return true;
    }

    static String pathToString(List<Integer> path) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            sb.append(STATION[path.get(i)]);
            if (i < path.size() - 1) sb.append(" → ");
        }
        return sb.toString();
    }

    // ─────────────────────────────────────────────
    // Complexity Analysis
    // ─────────────────────────────────────────────
    static void complexityAnalysis(int n, int m, int naiveHops) {
        System.out.println("\n══════════════════════════════════════════════════════════");
        System.out.println(" SUB-QUESTION (b): Union-Find Complexity Analysis");
        System.out.println("══════════════════════════════════════════════════════════");

        System.out.printf("  Graph: n=%d stations, m=%d candidate edges%n%n", n, m);

        System.out.println("  NAIVE (no union-by-rank, no path compression):");
        System.out.println("    find(x): O(n) per call in worst case (chain of n nodes)");
        System.out.println("    union(x,y): 2 * find() = O(n)");
        System.out.println("    Kruskal total: O(m log m) sort + O(m * n) union-find");
        System.out.printf ("    For n=%d, m=%d: sort=%d ops + union-find=%d ops%n",
                n, m, m * (int)(Math.log(m) / Math.log(2)), m * n);
        System.out.printf ("    Actual find() pointer hops measured: %d%n%n", naiveHops);

        System.out.println("  OPTIMISED (union-by-rank + path compression):");
        System.out.println("    find(x): O(α(n)) amortised ≈ O(1) practically");
        System.out.println("    union(x,y): O(α(n))");
        System.out.println("    Kruskal total: O(m log m) — dominated by sort");
        System.out.printf ("    For n=%d, m=%d: ~%d ops total%n",
                n, m, m * (int)(Math.log(m) / Math.log(2)));

        System.out.println("\n  Worst-case naive find() hops for n=7:");
        System.out.println("    A chain 0→1→2→3→4→5→6: find(0) = 6 hops.");
        System.out.printf ("    Called 2*m=%d times => max = %d * %d = %d hops.%n",
                2*m, 2*m, n-1, 2*m*(n-1));
    }

    // ─────────────────────────────────────────────
    // Main
    // ─────────────────────────────────────────────
    public static void main(String[] args) {

        // ── Define all 12 candidate edges ──────
        // (from the diagram: M=0,K=1,W=2,S=3,E=4,Y=5,H=6)
        List<Edge> allEdges = new ArrayList<>(Arrays.asList(
            new Edge("E", "S",  4),
            new Edge("W", "K",  5),
            new Edge("W", "S",  6),
            new Edge("M", "E",  7),
            new Edge("M", "K",  8),
            new Edge("H", "Y",  8),
            new Edge("M", "Y",  9),
            new Edge("Y", "S",  9),
            new Edge("M", "S", 10),
            new Edge("H", "M", 11),
            new Edge("M", "W", 12),
            new Edge("H", "K", 14)
        ));

        int n = STATION.length; // 7

        // ── Part (a): Kruskal's Algorithm ───────
        System.out.println("══════════════════════════════════════════════════════════");
        System.out.println(" SUB-QUESTION (a): Kruskal's Algorithm — MST Construction");
        System.out.println("══════════════════════════════════════════════════════════");
        System.out.println("\nAll edges sorted by weight:");
        List<Edge> sortedEdges = new ArrayList<>(allEdges);
        Collections.sort(sortedEdges);
        sortedEdges.forEach(e -> System.out.println("  " + e));

        List<Edge> mst = kruskalNaive(n, new ArrayList<>(allEdges));

        System.out.println("\nMST edges:");
        int totalCost = 0;
        for (Edge e : mst) {
            System.out.printf("  %s%n", e);
            totalCost += e.weight;
        }
        System.out.printf("Total MST cost = %d crore%n", totalCost);

        // Save naive hops for analysis
        List<Edge> tempEdges = new ArrayList<>(allEdges);
        Collections.sort(tempEdges);
        UnionFindNaive ufTemp = new UnionFindNaive(n);
        for (Edge e : tempEdges) if (ufTemp.union(e.u, e.v)) if(kruskalOptimised(n, new ArrayList<>(allEdges)).size() == n-1) break;
        // Re-run to count hops properly
        ufTemp = new UnionFindNaive(n);
        for (Edge e : tempEdges) ufTemp.union(e.u, e.v);
        int naiveHops = ufTemp.findHops;

        // ── Part (b): Complexity ─────────────────
        complexityAnalysis(n, allEdges.size(), naiveHops);

        // ── Part (c): Redundancy Mandate ─────────
        System.out.println("\n══════════════════════════════════════════════════════════");
        System.out.println(" SUB-QUESTION (c): Redundancy Mandate Verification");
        System.out.println("══════════════════════════════════════════════════════════");

        int M = idx("M"), W = idx("W");
        List<List<Integer>> mstPaths = findAllPaths(mst, M, W, n);

        System.out.println("\nPaths M → W in MST:");
        mstPaths.forEach(p -> System.out.println("  " + pathToString(p)));
        System.out.printf("Number of M-W paths in MST = %d%n", mstPaths.size());
        System.out.println("A spanning tree has exactly ONE path between any two nodes.");
        System.out.println("=> MST ALONE does NOT satisfy the 2-edge-disjoint-path mandate.");

        // Find minimum-cost augmentation
        System.out.println("\nSearching for minimum-cost augmentation:");
        System.out.println("(Cheapest non-MST edge that creates a 2nd edge-disjoint M-W path)");

        List<Edge> nonMstEdges = new ArrayList<>(allEdges);
        nonMstEdges.removeAll(mst);
        Collections.sort(nonMstEdges);

        System.out.printf("%-20s %-10s %-10s%n", "Candidate edge", "Cost(cr)", "Creates 2nd path?");
        System.out.println("─".repeat(45));

        Edge bestAugmentation = null;
        for (Edge aug : nonMstEdges) {
            List<Edge> augmented = new ArrayList<>(mst);
            augmented.add(aug);
            List<List<Integer>> augPaths = findAllPaths(augmented, M, W, n);
            boolean hasTwoDisjoint = false;
            for (int i = 0; i < augPaths.size() && !hasTwoDisjoint; i++)
                for (int j = i + 1; j < augPaths.size() && !hasTwoDisjoint; j++)
                    if (areEdgeDisjoint(augPaths.get(i), augPaths.get(j), augmented))
                        hasTwoDisjoint = true;

            System.out.printf("%-20s %-10d %-10s%n",
                    aug.label, aug.weight, hasTwoDisjoint ? "YES ✓" : "No");
            if (hasTwoDisjoint && bestAugmentation == null)
                bestAugmentation = aug;
        }

        System.out.println("\n── Minimum-Cost Augmentation ──");
        System.out.printf("Add edge: %s  (cost = %d crore)%n",
                bestAugmentation.label, bestAugmentation.weight);
        System.out.printf("Augmented network cost = MST(%d) + aug(%d) = %d crore%n",
                totalCost, bestAugmentation.weight, totalCost + bestAugmentation.weight);

        // Show the two edge-disjoint paths
        List<Edge> finalNetwork = new ArrayList<>(mst);
        finalNetwork.add(bestAugmentation);
        List<List<Integer>> finalPaths = findAllPaths(finalNetwork, M, W, n);
        System.out.println("\nTwo edge-disjoint M ↔ W paths in augmented network:");
        boolean printed = false;
        for (int i = 0; i < finalPaths.size() && !printed; i++) {
            for (int j = i + 1; j < finalPaths.size() && !printed; j++) {
                if (areEdgeDisjoint(finalPaths.get(i), finalPaths.get(j), finalNetwork)) {
                    System.out.println("  Path 1: " + pathToString(finalPaths.get(i)));
                    System.out.println("  Path 2: " + pathToString(finalPaths.get(j)));
                    System.out.println("  => Paths share NO edges. Mandate SATISFIED.");
                    printed = true;
                }
            }
        }

        System.out.println("\n══════════════════════════════════════════════════════════");
        System.out.println(" SUMMARY");
        System.out.println("══════════════════════════════════════════════════════════");
        System.out.printf("  MST edges : %s%n", mst);
        System.out.printf("  MST cost  : %d crore%n", totalCost);
        System.out.printf("  Aug. edge : %s%n", bestAugmentation);
        System.out.printf("  Final cost: %d crore (MST + redundancy)%n",
                totalCost + bestAugmentation.weight);
    }
}
