package com.example.gamefiedsarvya.data.repository

import com.example.gamefiedsarvya.data.models.*

/**
 * Offline-first Learning Hub.
 * Professional tier includes GFG/LeetCode-style DSA + STEM content.
 * All content embedded — no external redirects.
 */
object LearningHubRepository {

    private val allMaterials: List<StudyMaterial> = buildList {

        // ════════════════════════════════════════════════════════════════════
        //  FOUNDATION TIER
        // ════════════════════════════════════════════════════════════════════

        add(StudyMaterial("lh_f_tech1", "Technology", LearningTier.FOUNDATION,
            "What is a Computer?",
            """
A computer processes information using four steps:
• **Input** – Keyboard, mouse, microphone
• **Processing** – CPU (the brain)
• **Storage** – Hard drive, RAM
• **Output** – Screen, speakers, printer

The CPU executes billions of instructions per second.
RAM is temporary memory — it clears when you turn off the computer.
            """.trimIndent(),
            listOf("CPU = Central Processing Unit", "RAM = temporary memory", "Input → Process → Output"),
            listOf("Typing = Input", "Seeing result on screen = Output"),
            estimatedMinutes = 3, xpReward = 15))

        add(StudyMaterial("lh_f_sci1", "Science", LearningTier.FOUNDATION,
            "Our Solar System",
            """
8 planets orbit our Sun:
1. **Mercury** – closest, smallest, no atmosphere
2. **Venus** – hottest (greenhouse effect)
3. **Earth** – our home, liquid water
4. **Mars** – red planet, thin atmosphere
5. **Jupiter** – largest, Great Red Spot storm
6. **Saturn** – beautiful rings of ice and rock
7. **Uranus** – rotates on its side
8. **Neptune** – farthest, strongest winds

Memory trick: **M**y **V**ery **E**ducated **M**other **J**ust **S**erved **U**s **N**achos
            """.trimIndent(),
            listOf("Mercury is closest to Sun", "Earth is 3rd planet", "Jupiter is largest"),
            listOf("My Very Educated Mother Just Served Us Nachos"),
            estimatedMinutes = 4, xpReward = 15))

        add(StudyMaterial("lh_f_math1", "Math", LearningTier.FOUNDATION,
            "Multiplication Tables",
            """
Multiplication is repeated addition!

**Key facts:**
• 7 × 8 = 56  (7 groups of 8)
• 9 × 6 = 54
• 8 × 8 = 64
• 12 × 12 = 144

**Tricks:**
• 5 × anything ends in 0 or 5
• 10 × anything just adds a zero
• 9 × n = (n-1) followed by (10-n)  e.g. 9×7 = 63
            """.trimIndent(),
            listOf("7×8=56", "9×6=54", "5× ends in 0 or 5", "10× adds a zero"),
            estimatedMinutes = 5, xpReward = 20))

        add(StudyMaterial("lh_f_geo1", "Geography", LearningTier.FOUNDATION,
            "World Continents",
            """
There are **7 continents**:
1. **Asia** – largest, most populated
2. **Africa** – second largest, Sahara Desert
3. **North America** – includes USA, Canada, Mexico
4. **South America** – Amazon rainforest
5. **Antarctica** – coldest, no permanent residents
6. **Europe** – many countries, small area
7. **Australia** – smallest continent, unique wildlife

The **Pacific Ocean** is the largest ocean.
            """.trimIndent(),
            listOf("7 continents", "Asia is largest", "Antarctica is coldest", "Pacific is largest ocean"),
            estimatedMinutes = 4, xpReward = 15))

        // ════════════════════════════════════════════════════════════════════
        //  ADVANCED TIER
        // ════════════════════════════════════════════════════════════════════

        add(StudyMaterial("lh_a_tech1", "Technology", LearningTier.ADVANCED,
            "Data Structures: Stack & Queue",
            """
**Stack** – Last In, First Out (LIFO)
Operations: push(), pop(), peek() — all O(1)
Use cases: undo/redo, browser history, function call stack, DFS

**Queue** – First In, First Out (FIFO)
Operations: enqueue(), dequeue(), front() — all O(1)
Use cases: print queue, BFS traversal, task scheduling, CPU scheduling

**Implementation:**
Stack with array: push = arr[++top], pop = arr[top--]
Queue with circular array avoids O(n) dequeue

**Interview tip:** Stack is used in balanced parentheses, next greater element, and monotonic stack problems.
            """.trimIndent(),
            listOf("Stack = LIFO", "Queue = FIFO", "Both O(1) operations", "Stack → DFS, Queue → BFS"),
            listOf("Stack: browser back button", "Queue: printer jobs"),
            estimatedMinutes = 8, xpReward = 30))

        add(StudyMaterial("lh_a_sci1", "Science", LearningTier.ADVANCED,
            "Newton's Laws of Motion",
            """
**First Law (Inertia):** Objects resist change in motion unless acted on by a net force.

**Second Law:** F = ma
• F in Newtons (N), m in kg, a in m/s²
• Example: 10 kg × 2 m/s² = 20 N

**Third Law:** Every action has an equal and opposite reaction.
• Rocket: exhaust pushes down → rocket goes up
• Swimming: push water back → move forward

**Derived equations:**
• v = u + at
• s = ut + ½at²
• v² = u² + 2as
            """.trimIndent(),
            listOf("F = ma", "v = u + at", "v² = u² + 2as", "Action = Reaction"),
            listOf("F=ma: 10kg × 2m/s² = 20N", "Rocket propulsion uses 3rd law"),
            estimatedMinutes = 10, xpReward = 35))

        add(StudyMaterial("lh_a_math1", "Math", LearningTier.ADVANCED,
            "Calculus: Derivatives",
            """
**Power Rule:** d/dx(xⁿ) = nxⁿ⁻¹

**Common derivatives:**
• d/dx(x²) = 2x
• d/dx(sin x) = cos x
• d/dx(cos x) = −sin x
• d/dx(eˣ) = eˣ
• d/dx(ln x) = 1/x

**Chain Rule:** d/dx[f(g(x))] = f'(g(x)) · g'(x)
Example: d/dx(sin(2x)) = cos(2x) · 2 = 2cos(2x)

**Product Rule:** d/dx(uv) = u'v + uv'
**Quotient Rule:** d/dx(u/v) = (u'v − uv') / v²
            """.trimIndent(),
            listOf("Power rule: nxⁿ⁻¹", "d/dx(sin x) = cos x", "Chain rule", "Product rule"),
            listOf("d/dx(x⁴) = 4x³", "d/dx(sin(2x)) = 2cos(2x)"),
            estimatedMinutes = 12, xpReward = 40))

        add(StudyMaterial("lh_a_chem1", "Chemistry", LearningTier.ADVANCED,
            "Chemical Bonding",
            """
**Ionic Bonding:** Transfer of electrons between metals and non-metals.
• NaCl: Na⁺ + Cl⁻ → strong electrostatic attraction
• High melting point, conducts electricity when dissolved

**Covalent Bonding:** Sharing of electrons between non-metals.
• H₂O: 2 shared pairs (polar covalent)
• CO₂: double bonds (non-polar)

**Electronegativity:** Determines bond polarity.
• Difference > 1.7 → ionic
• 0.4–1.7 → polar covalent
• < 0.4 → non-polar covalent

**VSEPR Theory:** Electron pairs repel → determines molecular shape.
• H₂O: bent (2 lone pairs)
• NH₃: trigonal pyramidal
• CH₄: tetrahedral
            """.trimIndent(),
            listOf("Ionic = electron transfer", "Covalent = electron sharing", "VSEPR determines shape"),
            listOf("NaCl is ionic", "H₂O is polar covalent"),
            estimatedMinutes = 12, xpReward = 40))

        // ════════════════════════════════════════════════════════════════════
        //  PROFESSIONAL TIER – GFG / LeetCode / STEM style
        // ════════════════════════════════════════════════════════════════════

        add(StudyMaterial("lh_p_dsa1", "Technology", LearningTier.PROFESSIONAL,
            "DSA: Arrays & Two Pointers (LeetCode Pattern)",
            """
**Two Pointer Technique** — O(n) time, O(1) space

**Pattern 1: Opposite ends**
```
left = 0, right = n-1
while left < right:
    if condition: left++
    else: right--
```
Problems: Two Sum (sorted), Container With Most Water, Trapping Rain Water

**Pattern 2: Same direction (sliding window)**
```
left = 0
for right in range(n):
    window.add(arr[right])
    while window invalid:
        window.remove(arr[left])
        left++
```
Problems: Longest Substring Without Repeating, Minimum Window Substring

**Key insight:** Two pointers eliminate the need for nested loops, reducing O(n²) → O(n).

**LeetCode problems to practice:**
• #1 Two Sum, #11 Container With Most Water
• #15 3Sum, #42 Trapping Rain Water
• #3 Longest Substring Without Repeating Characters
            """.trimIndent(),
            listOf("Two pointers: O(n) time O(1) space", "Opposite ends for sorted arrays", "Sliding window for subarray problems"),
            listOf("Two Sum sorted: left+right pointers", "Sliding window: expand right, shrink left"),
            estimatedMinutes = 15, xpReward = 50))

        add(StudyMaterial("lh_p_dsa2", "Technology", LearningTier.PROFESSIONAL,
            "DSA: Dynamic Programming Fundamentals (GFG Pattern)",
            """
**DP = Recursion + Memoization (or Tabulation)**

**Steps to solve any DP problem:**
1. Identify overlapping subproblems
2. Define state: dp[i] = ?
3. Write recurrence relation
4. Handle base cases
5. Determine order of computation

**Classic patterns:**

**1D DP:**
• Fibonacci: dp[i] = dp[i-1] + dp[i-2]
• Climbing Stairs: dp[i] = dp[i-1] + dp[i-2]
• House Robber: dp[i] = max(dp[i-1], dp[i-2] + nums[i])

**2D DP:**
• Longest Common Subsequence:
  dp[i][j] = dp[i-1][j-1]+1 if match, else max(dp[i-1][j], dp[i][j-1])
• 0/1 Knapsack:
  dp[i][w] = max(dp[i-1][w], dp[i-1][w-wt[i]] + val[i])

**Space optimisation:** Many 2D DP can be reduced to 1D by using rolling array.

**LeetCode problems:**
• #70 Climbing Stairs, #198 House Robber
• #300 Longest Increasing Subsequence
• #1143 Longest Common Subsequence
• #416 Partition Equal Subset Sum
            """.trimIndent(),
            listOf("DP = recursion + memoization", "Define state clearly", "LCS recurrence", "Knapsack pattern"),
            listOf("Fibonacci: dp[i] = dp[i-1]+dp[i-2]", "LCS: match → +1, else max of neighbours"),
            estimatedMinutes = 20, xpReward = 60))

        add(StudyMaterial("lh_p_dsa3", "Technology", LearningTier.PROFESSIONAL,
            "DSA: Graph Algorithms (BFS, DFS, Dijkstra)",
            """
**BFS (Breadth-First Search)** — O(V+E)
Uses queue. Finds shortest path in unweighted graphs.
```
queue = [start]
visited = {start}
while queue:
    node = queue.popleft()
    for neighbour in graph[node]:
        if neighbour not in visited:
            visited.add(neighbour)
            queue.append(neighbour)
```

**DFS (Depth-First Search)** — O(V+E)
Uses stack/recursion. Detects cycles, topological sort.

**Dijkstra's Algorithm** — O((V+E) log V) with min-heap
Shortest path in weighted graphs (non-negative weights).
```
dist = {node: inf for all nodes}
dist[start] = 0
heap = [(0, start)]
while heap:
    d, u = heappop(heap)
    for v, w in graph[u]:
        if dist[u] + w < dist[v]:
            dist[v] = dist[u] + w
            heappush(heap, (dist[v], v))
```

**When to use:**
• BFS → shortest path (unweighted), level-order traversal
• DFS → cycle detection, topological sort, connected components
• Dijkstra → shortest path (weighted, non-negative)
• Bellman-Ford → negative weights

**LeetCode:** #200 Number of Islands, #207 Course Schedule, #743 Network Delay Time
            """.trimIndent(),
            listOf("BFS uses queue, DFS uses stack", "Dijkstra: O((V+E)logV)", "BFS = shortest unweighted path"),
            listOf("BFS for shortest path", "Dijkstra for weighted graphs"),
            estimatedMinutes = 20, xpReward = 65))

        add(StudyMaterial("lh_p_algo1", "Technology", LearningTier.PROFESSIONAL,
            "Algorithm Complexity & Big-O Mastery",
            """
**Complexity hierarchy (best → worst):**
O(1) < O(log n) < O(√n) < O(n) < O(n log n) < O(n²) < O(n³) < O(2ⁿ) < O(n!)

**Sorting algorithms:**
| Algorithm    | Best      | Average   | Worst     | Space  | Stable |
|-------------|-----------|-----------|-----------|--------|--------|
| Merge Sort  | O(n log n)| O(n log n)| O(n log n)| O(n)   | Yes    |
| Quick Sort  | O(n log n)| O(n log n)| O(n²)    | O(log n)| No    |
| Heap Sort   | O(n log n)| O(n log n)| O(n log n)| O(1)   | No     |
| Tim Sort    | O(n)      | O(n log n)| O(n log n)| O(n)   | Yes    |

**Amortised analysis:**
Dynamic array append: O(1) amortised (occasional O(n) resize)
Hash table insert: O(1) amortised

**Master Theorem:** T(n) = aT(n/b) + f(n)
• If f(n) = O(n^(log_b(a) - ε)) → T(n) = Θ(n^log_b(a))
• If f(n) = Θ(n^log_b(a)) → T(n) = Θ(n^log_b(a) · log n)
            """.trimIndent(),
            listOf("O(1)<O(log n)<O(n)<O(n log n)<O(n²)", "Merge sort: O(n log n) stable", "Quick sort: O(n²) worst case"),
            listOf("Binary search on 1M items: ~20 steps", "Bubble sort on 1M: ~1 trillion steps"),
            estimatedMinutes = 18, xpReward = 55))

        add(StudyMaterial("lh_p_sys1", "Technology", LearningTier.PROFESSIONAL,
            "System Design: Scalability Fundamentals",
            """
**Horizontal vs Vertical Scaling:**
• Vertical: bigger machine (limited, expensive)
• Horizontal: more machines (preferred, scalable)

**Load Balancing:**
• Round Robin, Least Connections, IP Hash
• Tools: Nginx, HAProxy, AWS ALB

**Caching:**
• Cache-aside: app checks cache first, then DB
• Write-through: write to cache and DB simultaneously
• LRU eviction policy
• Tools: Redis, Memcached

**Database Scaling:**
• Read replicas for read-heavy workloads
• Sharding: partition data across multiple DBs
• CAP Theorem: Consistency, Availability, Partition tolerance — pick 2

**Message Queues:**
• Decouple services, handle traffic spikes
• Tools: Kafka, RabbitMQ, AWS SQS

**CDN:** Serve static assets from edge locations near users.

**Key numbers to remember:**
• L1 cache: ~1 ns, RAM: ~100 ns, SSD: ~100 μs, Network: ~10 ms
            """.trimIndent(),
            listOf("Horizontal > vertical scaling", "CAP theorem: pick 2", "Cache-aside pattern", "Sharding for DB scale"),
            listOf("Redis for caching", "Kafka for message queues"),
            estimatedMinutes = 20, xpReward = 60))

        add(StudyMaterial("lh_p_ml1", "AI/ML", LearningTier.PROFESSIONAL,
            "Machine Learning: Core Concepts",
            """
**Supervised Learning:**
• Classification: predict category (SVM, Decision Tree, Neural Net)
• Regression: predict value (Linear Regression, Random Forest)

**Unsupervised Learning:**
• Clustering: K-Means, DBSCAN, Hierarchical
• Dimensionality Reduction: PCA, t-SNE, UMAP

**Key metrics:**
• Accuracy = (TP+TN)/(TP+TN+FP+FN)
• Precision = TP/(TP+FP) — how many predicted positives are correct
• Recall = TP/(TP+FN) — how many actual positives were found
• F1 = 2·(Precision·Recall)/(Precision+Recall)

**Bias-Variance Tradeoff:**
• High bias → underfitting (model too simple)
• High variance → overfitting (model too complex)
• Solution: regularisation (L1/L2), dropout, cross-validation

**Gradient Descent:**
θ = θ − α · ∇J(θ)
• α = learning rate
• Variants: SGD, Mini-batch, Adam, RMSProp

**Neural Networks:**
• Forward pass: compute predictions
• Backpropagation: compute gradients via chain rule
• Activation functions: ReLU, Sigmoid, Softmax
            """.trimIndent(),
            listOf("Precision vs Recall tradeoff", "Bias-variance tradeoff", "Gradient descent: θ = θ − α∇J", "ReLU activation"),
            listOf("High bias = underfitting", "High variance = overfitting"),
            estimatedMinutes = 22, xpReward = 65))

        add(StudyMaterial("lh_p_os1", "Technology", LearningTier.PROFESSIONAL,
            "Operating Systems: Process & Memory",
            """
**Process vs Thread:**
• Process: independent memory space, heavier
• Thread: shared memory, lighter, faster context switch

**CPU Scheduling:**
• FCFS: simple, convoy effect
• SJF: optimal average wait, needs burst time prediction
• Round Robin: fair, time quantum based
• Priority: starvation possible → use aging

**Deadlock conditions (all 4 must hold):**
1. Mutual Exclusion
2. Hold and Wait
3. No Preemption
4. Circular Wait

**Prevention:** Break any one condition.
**Detection:** Resource Allocation Graph, Banker's Algorithm

**Memory Management:**
• Paging: fixed-size frames, no external fragmentation
• Segmentation: variable-size, logical division
• Virtual Memory: allows processes larger than RAM
• Page Replacement: FIFO, LRU, Optimal

**Thrashing:** Too many page faults → CPU spends more time paging than executing.
Solution: Reduce degree of multiprogramming.
            """.trimIndent(),
            listOf("4 deadlock conditions", "Round Robin = fair scheduling", "Paging = no external fragmentation", "Thrashing = too many page faults"),
            listOf("Deadlock: all 4 conditions must hold", "LRU page replacement is near-optimal"),
            estimatedMinutes = 18, xpReward = 55))

        add(StudyMaterial("lh_p_db1", "Technology", LearningTier.PROFESSIONAL,
            "Databases: SQL, Indexing & Normalisation",
            """
**ACID Properties:**
• Atomicity: all or nothing
• Consistency: valid state before and after
• Isolation: concurrent transactions don't interfere
• Durability: committed data persists

**Normalisation:**
• 1NF: atomic values, no repeating groups
• 2NF: 1NF + no partial dependencies
• 3NF: 2NF + no transitive dependencies
• BCNF: every determinant is a candidate key

**Indexing:**
• B-Tree index: O(log n) search, good for range queries
• Hash index: O(1) lookup, no range queries
• Composite index: column order matters (leftmost prefix rule)

**Query optimisation:**
• EXPLAIN/EXPLAIN ANALYZE to see query plan
• Avoid SELECT *, use specific columns
• Index on WHERE, JOIN, ORDER BY columns
• Avoid functions on indexed columns in WHERE

**SQL Window Functions:**
ROW_NUMBER(), RANK(), DENSE_RANK(), LAG(), LEAD(), SUM() OVER()

**Joins:**
INNER, LEFT, RIGHT, FULL OUTER, CROSS, SELF
            """.trimIndent(),
            listOf("ACID: Atomicity, Consistency, Isolation, Durability", "3NF removes transitive dependencies", "B-Tree: O(log n) range queries"),
            listOf("EXPLAIN shows query plan", "Composite index: leftmost prefix rule"),
            estimatedMinutes = 18, xpReward = 55))

        add(StudyMaterial("lh_p_math1", "Math", LearningTier.PROFESSIONAL,
            "Linear Algebra for ML & CS",
            """
**Matrix Operations:**
• Multiplication: (m×n)(n×p) = (m×p), O(mnp)
• Transpose: (AB)ᵀ = BᵀAᵀ
• Inverse: AA⁻¹ = I, exists iff det(A) ≠ 0

**Determinant:**
• 2×2: |A| = ad − bc
• Properties: det(AB) = det(A)·det(B), det(Aᵀ) = det(A)

**Eigenvalues & Eigenvectors:**
Av = λv → det(A − λI) = 0 (characteristic equation)
• PCA uses eigenvectors of covariance matrix
• PageRank uses dominant eigenvector

**Vector Spaces:**
• Span, basis, dimension
• Rank = number of linearly independent rows/columns
• Null space: Ax = 0

**Applications in ML:**
• Linear regression: θ = (XᵀX)⁻¹Xᵀy
• PCA: eigenvectors of covariance matrix
• SVD: A = UΣVᵀ (used in recommendation systems)
            """.trimIndent(),
            listOf("Matrix multiply: (m×n)(n×p)=(m×p)", "Eigenvalue: det(A-λI)=0", "PCA uses eigenvectors", "SVD: A=UΣVᵀ"),
            listOf("Linear regression: θ=(XᵀX)⁻¹Xᵀy", "PCA reduces dimensions"),
            estimatedMinutes = 20, xpReward = 60))

        add(StudyMaterial("lh_p_phys1", "Physics", LearningTier.PROFESSIONAL,
            "Quantum Mechanics Essentials",
            """
**Wave-Particle Duality:**
de Broglie: λ = h/mv (matter has wave properties)
Double-slit experiment: interference pattern even with single particles

**Heisenberg Uncertainty Principle:**
Δx · Δp ≥ ℏ/2
Δt · ΔE ≥ ℏ/2
Cannot simultaneously know exact position AND momentum.

**Schrödinger Equation (time-dependent):**
iℏ ∂ψ/∂t = Ĥψ
ψ = wave function, |ψ|² = probability density

**Quantum Numbers:**
• n: principal (energy level, n = 1,2,3...)
• l: angular momentum (0 to n-1)
• mₗ: magnetic (-l to +l)
• mₛ: spin (±½)

**Pauli Exclusion Principle:** No two electrons can have identical quantum numbers.

**Quantum Tunnelling:** Particles can pass through potential barriers — basis of tunnel diodes, STM, nuclear fusion in stars.

**Applications:** Transistors, lasers, MRI, quantum computing.
            """.trimIndent(),
            listOf("λ = h/mv (de Broglie)", "Δx·Δp ≥ ℏ/2", "4 quantum numbers: n,l,mₗ,mₛ", "Pauli exclusion principle"),
            listOf("Tunnelling enables transistors", "Schrödinger equation governs quantum states"),
            estimatedMinutes = 20, xpReward = 60))
    }

    fun getMaterialsForTier(tier: LearningTier): List<StudyMaterial> =
        allMaterials.filter { it.tier == tier }

    fun getMaterialsForTopic(topic: String, tier: LearningTier): List<StudyMaterial> =
        allMaterials.filter { it.topic == topic && it.tier == tier }

    fun getMaterialById(id: String): StudyMaterial? = allMaterials.find { it.id == id }

    fun getAllTopicsForTier(tier: LearningTier): List<String> =
        allMaterials.filter { it.tier == tier }.map { it.topic }.distinct().sorted()

    fun getRecommendedMaterials(
        tier: LearningTier,
        weakTopics: List<String>,
        studiedIds: Set<String>
    ): List<StudyMaterial> {
        val unstudied = allMaterials.filter { it.tier == tier && it.id !in studiedIds }
        val weak      = unstudied.filter { it.topic in weakTopics }
        return (weak + unstudied).distinctBy { it.id }.take(5)
    }

    fun getTotalCount(): Int = allMaterials.size
    fun getCountForTier(tier: LearningTier): Int = allMaterials.count { it.tier == tier }
}
