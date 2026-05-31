// Case Study 3 — X (Twitter): Bounded-Depth BFS for Retweet-Reach Prediction
// Graph: A→{B,C}, B→{D,E}, C→{E,F}, D→{G}, E→{G,H}, F→{H,I}
// Edge X→Y means "X is followed by Y" (tweet by X is visible to Y)
// BFS bounded to depth 3; visited-set prevents double-counting.

import java.util.*;

public class TwitterBFS {

    // ── Part (b): bfsBounded — complete implementation of the question boilerplate ──

    /**
     * adj    : {userId → list of users who follow userId}  (i.e., adjacency = followers)
     * source : the tweet author
     * maxDepth: how far to traverse (3 for the 10-minute reach model)
     *
     * Returns the set of all user_ids reachable from source within maxDepth hops,
     * EXCLUDING source itself.
     * Time complexity: O(V + E) over the visited subgraph.
     */
    static Set<String> bfsBounded(Map<String, List<String>> adj,
                                   String source, int maxDepth) {
        Set<String> visited = new HashSet<>();
        visited.add(source);

        // Each queue entry: Object[]{ user_id (String), depth (Integer) }
        Deque<Object[]> queue = new ArrayDeque<>();
        queue.offer(new Object[]{ source, 0 });

        Set<String> reached = new HashSet<>();

        while (!queue.isEmpty()) {
            Object[] cur   = queue.poll();
            String u       = (String) cur[0];
            int    depth   = (int)    cur[1];

            // TODO 1: do NOT expand neighbours if we are at the depth limit
            if (depth == maxDepth) continue;

            // TODO 2: enumerate neighbours; add unseen ones to visited + queue
            List<String> neighbours = adj.getOrDefault(u, Collections.emptyList());
            for (String v : neighbours) {
                if (!visited.contains(v)) {
                    visited.add(v);
                    queue.offer(new Object[]{ v, depth + 1 });

                    // TODO 3: accumulate every newly-visited non-source user into reached
                    reached.add(v);
                }
            }
        }
        return reached;
    }

    // ── Part (a): Trace BFS queue evolution ──────────────────────────────────
    static void traceBFS(Map<String, List<String>> adj, String source, int maxDepth) {
        System.out.println("=== Part (a): BFS Queue Trace ===");
        System.out.printf("%-12s %-6s %-25s %-40s%n",
            "Dequeued", "Depth", "Neighbours discovered", "Visited set");
        System.out.println("-".repeat(88));

        Set<String> visited = new LinkedHashSet<>();
        visited.add(source);
        Deque<Object[]> queue = new ArrayDeque<>();
        queue.offer(new Object[]{ source, 0 });
        Set<String> reached = new LinkedHashSet<>();

        while (!queue.isEmpty()) {
            Object[] cur = queue.poll();
            String u     = (String) cur[0];
            int    depth = (int)    cur[1];

            List<String> neighbours = adj.getOrDefault(u, Collections.emptyList());
            List<String> discovered = new ArrayList<>();

            if (depth < maxDepth) {
                for (String v : neighbours) {
                    if (!visited.contains(v)) {
                        visited.add(v);
                        queue.offer(new Object[]{ v, depth + 1 });
                        reached.add(v);
                        discovered.add(v);
                    }
                    // If already visited, skip — this is where double-counting is prevented
                }
            }

            String discovStr = discovered.isEmpty() ? "(none / depth limit)" : String.join(", ", discovered);
            System.out.printf("%-12s %-6d %-25s %-40s%n",
                u, depth, discovStr, visited.toString());
        }

        System.out.println();
        System.out.println("(i)  Final reached set (excl. A): " + new TreeSet<>(reached));
        System.out.println();
        System.out.println("(ii) Multi-in-edge users and why visited-set prevents double-counting:");
        System.out.println("  E: reachable via B (depth 2) and C (depth 2).");
        System.out.println("     When B is dequeued, E is discovered first and added to visited.");
        System.out.println("     When C is dequeued, E is already in visited → skipped. Count = 1.");
        System.out.println("  G: reachable via D (depth 3) and E (depth 3).");
        System.out.println("     D is dequeued before E; G discovered via D, added to visited.");
        System.out.println("     When E is dequeued at depth 3, depth==maxDepth → no expansion.");
        System.out.println("     (Even without depth limit, G would be in visited.) Count = 1.");
        System.out.println("  H: reachable via E (depth 3) and F (depth 3). Same argument — ");
        System.out.println("     first discovery wins; subsequent encounters skip. Count = 1.");
    }

    // ── Part (c): Capacity analysis ──────────────────────────────────────────
    static void capacityAnalysis() {
        System.out.println("\n=== Part (c): Capacity Analysis ===");

        // Parameters
        long d1       = 50_000L;    // depth-1 followers of the author
        long avgFoll  = 500L;       // average followers per user
        double overlap = 0.30;      // 30% of candidate edges go to already-visited users

        // 1. With visited-set (distinct users only)
        long d2_candidates = d1 * avgFoll;                     // 25,000,000
        long d2_distinct   = Math.round(d2_candidates * (1 - overlap)); // 17,500,000
        long d3_candidates = d2_distinct * avgFoll;            // 8,750,000,000
        long d3_distinct   = Math.round(d3_candidates * (1 - overlap)); // 6,125,000,000

        long total_visited = 1 + d1 + d2_distinct + d3_distinct;

        System.out.println("\n1. Worst-case distinct users reached (with visited-set, 30% overlap):");
        System.out.printf("   Depth 1 : %,d%n", d1);
        System.out.printf("   Depth 2 : %,d candidates → %,d distinct (×0.70)%n", d2_candidates, d2_distinct);
        System.out.printf("   Depth 3 : %,d candidates → %,d distinct (×0.70)%n", d3_candidates, d3_distinct);
        System.out.printf("   TOTAL distinct users ≤ depth 3: ~%,d%n", total_visited);

        // 2. Without visited-set (naive BFS — exponential re-enqueue)
        long d2_naive = d1 * avgFoll;
        long d3_naive = d2_naive * avgFoll;
        long total_naive = d1 + d2_naive + d3_naive;
        System.out.println("\n2. Worst-case work WITHOUT visited-set (naive BFS re-enqueues seen nodes):");
        System.out.printf("   Depth 1 : %,d%n", d1);
        System.out.printf("   Depth 2 : %,d (all candidates, no dedup)%n", d2_naive);
        System.out.printf("   Depth 3 : %,d%n", d3_naive);
        System.out.printf("   TOTAL enqueue ops : ~%,d  (≈ %.0f× worse than with visited-set)%n",
            total_naive, (double) total_naive / total_visited);

        // 3. Feasibility at 500 ms p99 (2 µs per neighbour visit)
        double visitTimeUs  = 2.0;         // microseconds per graph-neighbour visit
        double budgetUs     = 500_000.0;   // 500 ms = 500,000 µs

        // Effective work units = edges traversed ≈ total_visited (one visit per node in BFS)
        // For depth-2 in practice the dominant cost is visiting d2_distinct edges
        // Real visited-set work ≈ d1 + d2_distinct + d3_distinct (edges processed)
        double withVisited_us = total_visited * visitTimeUs;
        double withoutVisited_us = total_naive * visitTimeUs;

        System.out.println("\n3. Feasibility at 500 ms p99 (2 µs per neighbour visit):");
        System.out.printf("   With visited-set    : %,.0f µs ≈ %.1f s  → %s%n",
            withVisited_us, withVisited_us / 1_000_000,
            withVisited_us <= budgetUs ? "FEASIBLE ✓" : "NOT feasible ✗ (real graphs have far less depth-3 reach)");
        System.out.printf("   Without visited-set : %,.0f µs ≈ %.0f s  → NOT feasible ✗%n",
            withoutVisited_us, withoutVisited_us / 1_000_000);
        System.out.println();
        System.out.println("   Note: In practice, depth-3 reach is constrained by graph diameter");
        System.out.println("   and real follower-count distributions (power-law, not uniform 500).");
        System.out.println("   The visited-set is essential for correctness and performance; without");
        System.out.println("   it the system would blow the 500 ms budget by orders of magnitude.");
    }

    // ── Main ──────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        // Build adjacency list: X → list of users who follow X (tweet reaches them)
        // Alphabetical order within each list (required by the question)
        Map<String, List<String>> adj = new LinkedHashMap<>();
        adj.put("A", Arrays.asList("B", "C"));
        adj.put("B", Arrays.asList("D", "E"));
        adj.put("C", Arrays.asList("E", "F"));
        adj.put("D", Arrays.asList("G"));
        adj.put("E", Arrays.asList("G", "H"));
        adj.put("F", Arrays.asList("H", "I"));
        adj.put("G", Collections.emptyList());
        adj.put("H", Collections.emptyList());
        adj.put("I", Collections.emptyList());

        int maxDepth = 3;

        // Part (a): trace
        traceBFS(adj, "A", maxDepth);

        // Part (b): verify bfsBounded returns the same result
        System.out.println("\n=== Part (b): bfsBounded result verification ===");
        Set<String> result = bfsBounded(adj, "A", maxDepth);
        System.out.println("Reached (bfsBounded): " + new TreeSet<>(result));
        System.out.println("Expected            : [B, C, D, E, F, G, H, I]");
        System.out.println("Correct: " + new TreeSet<>(result).equals(
            new TreeSet<>(Arrays.asList("B","C","D","E","F","G","H","I"))));

        // Part (c): capacity analysis
        capacityAnalysis();
    }
}
