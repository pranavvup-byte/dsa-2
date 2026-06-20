// Case Study 4 — Chennai Metro Fare Optimisation
// Multi-criteria Dijkstra: minimise fare, tiebreak on time
// 7 stations: ALD, AVN, CMR, GTM, MGR, TNB, WMN
// 10 edges, weights = (fare ₹, travel time min)

import java.util.*;

public class ChennaiMetroDijkstra {

    // ── Graph model ──────────────────────────────────────────────────────────
    static class MetroEdge {
        int to;
        int fare;          // primary weight
        int time;          // secondary weight (tiebreaker)
        MetroEdge(int to, int f, int t) { this.to = to; this.fare = f; this.time = t; }
    }

    static class NodeDist {
        int node;
        int fare;
        int time;
        NodeDist(int n, int f, int t) { node = n; fare = f; time = t; }
    }

    // Station name <-> index mapping
    static final String[] NAMES = {"ALD", "AVN", "CMR", "GTM", "MGR", "TNB", "WMN"};
    static Map<String, Integer> idx = new HashMap<>();
    static {
        for (int i = 0; i < NAMES.length; i++) idx.put(NAMES[i], i);
    }

    static List<List<MetroEdge>> buildGraph() {
        int n = NAMES.length;
        List<List<MetroEdge>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());

        // Undirected metro lines: (station, station, fare, time)
        int[][] raw = {
            // u,        v,        fare, time
            {idx.get("ALD"), idx.get("AVN"), 10, 5},
            {idx.get("ALD"), idx.get("CMR"),  8, 7},
            {idx.get("AVN"), idx.get("CMR"), 12, 4},
            {idx.get("AVN"), idx.get("GTM"),  5, 3},
            {idx.get("CMR"), idx.get("GTM"),  6, 2},
            {idx.get("CMR"), idx.get("MGR"), 15, 6},
            {idx.get("GTM"), idx.get("MGR"), 10, 5},
            {idx.get("GTM"), idx.get("TNB"), 12, 4},
            {idx.get("MGR"), idx.get("WMN"),  8, 3},
            {idx.get("TNB"), idx.get("WMN"), 25, 9},
        };

        for (int[] e : raw) {
            adj.get(e[0]).add(new MetroEdge(e[1], e[2], e[3]));
            adj.get(e[1]).add(new MetroEdge(e[0], e[2], e[3])); // undirected
        }
        return adj;
    }

    // ── Part (a): Plain Dijkstra on fare only (priority queue trace) ────────
    static void fareOnlyDijkstra(List<List<MetroEdge>> adj, int source) {
        int n = adj.size();
        int[] dist = new int[n];
        int[] prev = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(prev, -1);
        dist[source] = 0;

        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[1] - b[1]); // {node, fare}
        pq.offer(new int[]{source, 0});
        boolean[] finalised = new boolean[n];

        System.out.println("=== Part (a): Dijkstra on fare alone — extraction trace ===\n");
        System.out.printf("%-10s %-10s %-50s%n", "Extracted", "Fare", "Relaxations performed");
        System.out.println("-".repeat(85));

        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            int u = cur[0], f = cur[1];
            if (finalised[u]) continue;
            if (f > dist[u]) continue;
            finalised[u] = true;

            StringBuilder relax = new StringBuilder();
            for (MetroEdge e : adj.get(u)) {
                int nf = dist[u] + e.fare;
                if (nf < dist[e.to]) {
                    dist[e.to] = nf;
                    prev[e.to] = u;
                    pq.offer(new int[]{e.to, nf});
                    relax.append(NAMES[e.to]).append("(->").append(nf).append(") ");
                } else {
                    relax.append(NAMES[e.to]).append("(no change) ");
                }
            }
            System.out.printf("%-10s %-10d %-50s%n", NAMES[u], dist[u], relax.toString());
        }

        int wmn = idx.get("WMN");
        System.out.printf("%nMinimum fare ALD -> WMN: ₹%d%n", dist[wmn]);

        List<String> path = new ArrayList<>();
        for (int at = wmn; at != -1; at = prev[at]) path.add(NAMES[at]);
        Collections.reverse(path);
        System.out.println("Path: " + String.join(" -> ", path));

        // Verify cumulative fare
        System.out.println("\nCumulative fare verification along path:");
        int running = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            int u = idx.get(path.get(i)), v = idx.get(path.get(i + 1));
            int edgeFare = -1;
            for (MetroEdge e : adj.get(u)) if (e.to == v) edgeFare = e.fare;
            running += edgeFare;
            System.out.printf("  %s -> %s : +₹%d  (running total ₹%d)%n",
                path.get(i), path.get(i + 1), edgeFare, running);
        }
        System.out.println("Total fare matches Dijkstra result: " + (running == dist[wmn]));
    }

    // ── Part (b) & (c): Multi-criteria Dijkstra — lexicographic (fare, time) ─

    /** Returns array where dist[v] = {fare, time} of cheapest-then-fastest path from source. */
    static int[][] dijkstraMultiCriteria(int n, List<List<MetroEdge>> adj, int source) {
        int[][] dist = new int[n][2];
        for (int i = 0; i < n; i++) {
            dist[i][0] = Integer.MAX_VALUE;
            dist[i][1] = Integer.MAX_VALUE;
        }
        dist[source][0] = 0;
        dist[source][1] = 0;

        // Min-heap ordered by (fare ASC, time ASC) — lex compare.
        PriorityQueue<NodeDist> pq = new PriorityQueue<>((a, b) -> {
            // TODO 1: compare a.fare vs b.fare; if equal, compare a.time vs b.time
            if (a.fare != b.fare) return Integer.compare(a.fare, b.fare);
            return Integer.compare(a.time, b.time);
        });
        pq.offer(new NodeDist(source, 0, 0));
        int[] prev = new int[n];
        Arrays.fill(prev, -1);

        while (!pq.isEmpty()) {
            NodeDist top = pq.poll();
            int u = top.node;

            // Stale skip
            if (top.fare > dist[u][0] || (top.fare == dist[u][0] && top.time > dist[u][1])) continue;

            for (MetroEdge e : adj.get(u)) {
                int newFare = dist[u][0] + e.fare;
                int newTime = dist[u][1] + e.time;

                // TODO 2: lex compare (newFare, newTime) with (dist[e.to][0], dist[e.to][1])
                //         if strictly better, relax both and offer to PQ
                boolean better =
                    (newFare < dist[e.to][0]) ||
                    (newFare == dist[e.to][0] && newTime < dist[e.to][1]);

                if (better) {
                    dist[e.to][0] = newFare;
                    dist[e.to][1] = newTime;
                    prev[e.to] = u;
                    pq.offer(new NodeDist(e.to, newFare, newTime));
                }
            }
        }

        lastPrev = prev; // stash for path reconstruction in demo driver
        return dist;
    }

    static int[] lastPrev; // helper for printing path after dijkstraMultiCriteria

    static void multiCriteriaDemo(List<List<MetroEdge>> adj, int source) {
        System.out.println("\n=== Part (b)/(c): Multi-criteria Dijkstra — lexicographic (fare, time) ===\n");
        int n = adj.size();
        int[][] dist = dijkstraMultiCriteria(n, adj, source);

        System.out.printf("%-10s %-10s %-10s%n", "Station", "Fare (₹)", "Time (min)");
        System.out.println("-".repeat(32));
        for (int i = 0; i < n; i++) {
            System.out.printf("%-10s %-10d %-10d%n", NAMES[i], dist[i][0], dist[i][1]);
        }

        int wmn = idx.get("WMN");
        List<String> path = new ArrayList<>();
        for (int at = wmn; at != -1; at = lastPrev[at]) path.add(NAMES[at]);
        Collections.reverse(path);

        System.out.printf("%nFinal route ALD -> WMN: %s%n", String.join(" -> ", path));
        System.out.printf("Fare = ₹%d   Time = %d min%n", dist[wmn][0], dist[wmn][1]);

        System.out.println("\nComparison with Part (a) (fare-only Dijkstra):");
        System.out.println("  Same path chosen: ALD -> CMR -> MGR -> WMN");
        System.out.println("  No fare-tie occurs anywhere on the shortest-path tree in this");
        System.out.println("  network (e.g. fare-to-MGR via CMR = ₹23 strictly beats fare-to-MGR");
        System.out.println("  via GTM = ₹24), so the lexicographic tiebreaker never actually");
        System.out.println("  fires here — but the comparator is correctly wired for networks");
        System.out.println("  where a genuine fare-tie does occur.");
    }

    // ── Complexity & correctness discussion ──────────────────────────────────
    static void discussComplexity() {
        System.out.println("\n=== Complexity & Correctness Discussion ===\n");
        System.out.println("Time complexity of dijkstraMultiCriteria:");
        System.out.println("  Same asymptotic complexity as standard Dijkstra:");
        System.out.println("  O((V + E) log V) using a binary-heap PriorityQueue,");
        System.out.println("  since the comparator does O(1) extra work per comparison");
        System.out.println("  (one or two int comparisons) — this does not change the");
        System.out.println("  asymptotic bound, only the constant factor.");
        System.out.println();
        System.out.println("Why NOT 'run Dijkstra twice' (once on fare, once on time):");
        System.out.println("  Running Dijkstra independently on fare and then independently");
        System.out.println("  on time produces two DIFFERENT shortest-path trees in general.");
        System.out.println("  The fare-optimal path is not guaranteed to be the same path as");
        System.out.println("  the time-optimal path, so you cannot simply pick the fare-tree's");
        System.out.println("  distances and the time-tree's distances and combine them — the");
        System.out.println("  resulting (fare, time) pair would not correspond to ANY single");
        System.out.println("  real route in the network.");
        System.out.println();
        System.out.println("  Example: the cheapest path might be ALD->CMR->MGR->WMN (fare 31,");
        System.out.println("  time 16), while the fastest path independently might route through");
        System.out.println("  completely different edges. 'Two separate Dijkstra runs' gives you");
        System.out.println("  the best fare achievable BY SOME path and the best time achievable");
        System.out.println("  BY SOME (possibly different) path — not a single coherent journey.");
        System.out.println();
        System.out.println("  The lexicographic-comparator approach instead finds the single path");
        System.out.println("  that is truly fare-optimal first, and among all fare-optimal paths,");
        System.out.println("  time-optimal — this is the only approach that guarantees a path that");
        System.out.println("  actually exists in the graph and matches the passenger's true");
        System.out.println("  preference ordering.");
    }

    // ── Part (d): Pareto-optimal multi-criteria routing (MOSP) ───────────────
    static void paretoDiscussion() {
        System.out.println("\n=== Part (d): Pareto-Optimal Multi-Criteria Routing (MOSP) ===\n");
        System.out.println("(i) A Pareto-optimal path is a route for which no other path exists");
        System.out.println("    that is at least as good on every criterion (fare, time) and");
        System.out.println("    strictly better on at least one. Equivalently, you cannot reduce");
        System.out.println("    the fare without increasing the time, or vice versa.");
        System.out.println();
        System.out.println("(ii) The number of Pareto-optimal paths can grow exponentially with");
        System.out.println("     graph size because every distinct combination of edge-subset");
        System.out.println("     trade-offs along different routes can produce a new non-dominated");
        System.out.println("     (fare, time) pair; in dense graphs with many alternate routes,");
        System.out.println("     the Pareto frontier itself can have exponentially many points");
        System.out.println("     in the worst case (a well-known result for multi-objective");
        System.out.println("     shortest-path problems, which are NP-hard for 3+ objectives).");
        System.out.println();
        System.out.println("(iii) The Multi-Objective Shortest Path (MOSP) algorithm extends");
        System.out.println("      Dijkstra by storing, at each node, a Pareto-frontier SET of");
        System.out.println("      non-dominated (fare, time) labels instead of a single best");
        System.out.println("      distance. When relaxing an edge, a new label is only kept if it");
        System.out.println("      is not dominated by any existing label at that node; dominated");
        System.out.println("      labels (both worse fare and worse time than another label) are");
        System.out.println("      pruned. The priority queue holds labels rather than nodes, and");
        System.out.println("      the algorithm terminates when no more non-dominated labels can");
        System.out.println("      be generated.");
        System.out.println();
        System.out.println("(iv) Typical use case: Google Maps' 'Avoid tolls' / route-options");
        System.out.println("     feature, which presents the user with multiple non-dominated");
        System.out.println("     routes (e.g. fastest-but-tolled vs slower-but-toll-free) rather");
        System.out.println("     than collapsing everything into a single 'best' route, letting");
        System.out.println("     the user pick based on their own fare/time trade-off preference.");
    }

    // ── Main ──────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        List<List<MetroEdge>> adj = buildGraph();
        int source = idx.get("ALD");

        fareOnlyDijkstra(adj, source);
        multiCriteriaDemo(adj, source);
        discussComplexity();
        paretoDiscussion();
    }
}
