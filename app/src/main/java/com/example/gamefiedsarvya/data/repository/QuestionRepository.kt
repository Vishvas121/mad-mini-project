package com.example.gamefiedsarvya.data.repository

import com.example.gamefiedsarvya.data.models.Difficulty
import com.example.gamefiedsarvya.data.models.Question

/**
 * Offline-first question bank.
 * In a production build this would fetch from a backend; here it provides
 * a rich preloaded set so the demo works without any network.
 */
object QuestionRepository {

    private val allQuestions: List<Question> = buildList {

        // ── FOREST (Easy) ─────────────────────────────────────────────────────
        add(Question("f1", "What does CPU stand for?",
            listOf("Central Processing Unit", "Computer Personal Unit",
                "Core Power Unit", "Central Program Utility"),
            0, "Think about what 'processes' instructions in a computer.",
            "CPU = Central Processing Unit – the brain of the computer.",
            "Technology", Difficulty.EASY, 20))

        add(Question("f2", "Which planet is closest to the Sun?",
            listOf("Venus", "Earth", "Mercury", "Mars"),
            2, "It's the smallest planet in our solar system.",
            "Mercury is the first planet from the Sun.",
            "Science", Difficulty.EASY, 20))

        add(Question("f3", "What is 7 × 8?",
            listOf("54", "56", "58", "64"),
            1, "7 × 8 is close to 7 × 10 = 70, subtract 14.",
            "7 × 8 = 56.",
            "Math", Difficulty.EASY, 15))

        add(Question("f4", "Which language is used to style web pages?",
            listOf("HTML", "CSS", "JavaScript", "Python"),
            1, "It stands for Cascading Style Sheets.",
            "CSS controls the visual presentation of HTML elements.",
            "Technology", Difficulty.EASY, 20))

        add(Question("f5", "What is the chemical symbol for water?",
            listOf("WA", "H2O", "HO2", "OHH"),
            1, "Water has 2 hydrogen atoms and 1 oxygen atom.",
            "H₂O – two hydrogen, one oxygen.",
            "Science", Difficulty.EASY, 15))

        add(Question("f6", "What is the capital of France?",
            listOf("Berlin", "Madrid", "Paris", "Rome"),
            2, "It's home to the Eiffel Tower.",
            "Paris is the capital and largest city of France.",
            "Geography", Difficulty.EASY, 15))

        add(Question("f7", "How many sides does a hexagon have?",
            listOf("5", "6", "7", "8"),
            1, "'Hex' is Greek for six.",
            "A hexagon has exactly 6 sides.",
            "Math", Difficulty.EASY, 15))

        add(Question("f8", "Which gas do plants absorb from the air?",
            listOf("Oxygen", "Nitrogen", "Carbon Dioxide", "Hydrogen"),
            2, "Plants use it during photosynthesis.",
            "Plants absorb CO₂ and release O₂ during photosynthesis.",
            "Science", Difficulty.EASY, 20))

        // ── RUINS (Medium) ────────────────────────────────────────────────────
        add(Question("r1", "What does RAM stand for?",
            listOf("Random Access Memory", "Read Access Module",
                "Rapid Array Memory", "Runtime Access Mode"),
            0, "It's the short-term memory your computer uses while running programs.",
            "RAM = Random Access Memory – volatile, fast storage for active processes.",
            "Technology", Difficulty.MEDIUM, 18))

        add(Question("r2", "What is the powerhouse of the cell?",
            listOf("Nucleus", "Ribosome", "Mitochondria", "Golgi Apparatus"),
            2, "It produces ATP energy for the cell.",
            "Mitochondria generate most of the cell's ATP through cellular respiration.",
            "Science", Difficulty.MEDIUM, 18))

        add(Question("r3", "Solve: 3x + 9 = 24. What is x?",
            listOf("3", "5", "7", "9"),
            1, "Subtract 9 from both sides first.",
            "3x = 15 → x = 5.",
            "Math", Difficulty.MEDIUM, 20))

        add(Question("r4", "Which data structure uses LIFO order?",
            listOf("Queue", "Stack", "Linked List", "Tree"),
            1, "Think of a stack of plates – last in, first out.",
            "A Stack follows Last-In-First-Out (LIFO) order.",
            "Technology", Difficulty.MEDIUM, 18))

        add(Question("r5", "What is Newton's Second Law of Motion?",
            listOf("F = mv", "F = ma", "E = mc²", "p = mv"),
            1, "Force equals mass times something.",
            "F = ma: Force equals mass multiplied by acceleration.",
            "Science", Difficulty.MEDIUM, 20))

        add(Question("r6", "What is the time complexity of binary search?",
            listOf("O(n)", "O(n²)", "O(log n)", "O(1)"),
            2, "Each step halves the search space.",
            "Binary search is O(log n) because it halves the problem each iteration.",
            "Technology", Difficulty.MEDIUM, 18))

        add(Question("r7", "Which continent is the Sahara Desert on?",
            listOf("Asia", "Australia", "South America", "Africa"),
            3, "It's the world's largest hot desert.",
            "The Sahara Desert spans across northern Africa.",
            "Geography", Difficulty.MEDIUM, 18))

        add(Question("r8", "What is the derivative of x²?",
            listOf("x", "2x", "x²", "2"),
            1, "Use the power rule: bring down the exponent.",
            "d/dx(x²) = 2x by the power rule.",
            "Math", Difficulty.MEDIUM, 20))

        // ── FORTRESS (Hard) ───────────────────────────────────────────────────
        add(Question("h1", "What is the time complexity of quicksort (average case)?",
            listOf("O(n)", "O(n log n)", "O(n²)", "O(log n)"),
            1, "It divides and conquers, but not perfectly balanced.",
            "Quicksort averages O(n log n) but degrades to O(n²) in worst case.",
            "Technology", Difficulty.HARD, 15))

        add(Question("h2", "What is the Heisenberg Uncertainty Principle?",
            listOf(
                "Energy cannot be created or destroyed",
                "Position and momentum cannot both be precisely known simultaneously",
                "Every action has an equal and opposite reaction",
                "Matter and energy are equivalent"
            ),
            1, "It's a fundamental limit in quantum mechanics.",
            "Δx·Δp ≥ ℏ/2 – the more precisely you know position, the less you know momentum.",
            "Science", Difficulty.HARD, 20))

        add(Question("h3", "In Big-O notation, which is the fastest growth rate?",
            listOf("O(n!)", "O(2ⁿ)", "O(n³)", "O(n log n)"),
            3, "Slower growth = faster algorithm.",
            "O(n log n) grows slowest among these, making it the most efficient.",
            "Technology", Difficulty.HARD, 15))

        add(Question("h4", "What does SOLID stand for in software design?",
            listOf(
                "Single, Open, Liskov, Interface, Dependency",
                "Scalable, Object, Linked, Integrated, Dynamic",
                "Simple, Optimized, Layered, Isolated, Distributed",
                "Structured, Ordered, Linked, Indexed, Defined"
            ),
            0, "These are five object-oriented design principles.",
            "SOLID: Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, Dependency Inversion.",
            "Technology", Difficulty.HARD, 20))

        add(Question("h5", "What is the integral of sin(x)?",
            listOf("cos(x) + C", "-cos(x) + C", "sin(x) + C", "-sin(x) + C"),
            1, "The derivative of -cos(x) is sin(x).",
            "∫sin(x)dx = -cos(x) + C.",
            "Math", Difficulty.HARD, 18))

        add(Question("h6", "Which sorting algorithm is stable and has O(n log n) worst case?",
            listOf("Quicksort", "Heapsort", "Merge Sort", "Shell Sort"),
            2, "It splits arrays in half recursively.",
            "Merge Sort is stable and guarantees O(n log n) in all cases.",
            "Technology", Difficulty.HARD, 15))

        add(Question("h7", "What is the Pythagorean theorem?",
            listOf("a² + b² = c²", "a + b = c", "a² - b² = c²", "2a + 2b = c"),
            0, "It relates the sides of a right triangle.",
            "a² + b² = c² where c is the hypotenuse.",
            "Math", Difficulty.HARD, 15))

        add(Question("h8", "What does DNA stand for?",
            listOf(
                "Deoxyribonucleic Acid",
                "Dinitrogen Acid",
                "Dynamic Nucleic Array",
                "Deoxyribose Nitrogen Acid"
            ),
            0, "It's the molecule that carries genetic information.",
            "DNA = Deoxyribonucleic Acid – the blueprint of life.",
            "Science", Difficulty.HARD, 18))

        // ── STEM / HIGH SCHOOL HARD ───────────────────────────────────────────

        add(Question("s1",
            "A ball is thrown vertically upward at 20 m/s. How high does it reach? (g = 10 m/s²)",
            listOf("10 m", "20 m", "40 m", "5 m"),
            1, "Use v² = u² − 2gh, set v = 0 at max height.",
            "v² = u² − 2gh → 0 = 400 − 20h → h = 20 m.",
            "Physics", Difficulty.HARD, 18))

        add(Question("s2",
            "What is the pH of a solution with [H⁺] = 1×10⁻³ mol/L?",
            listOf("3", "7", "11", "−3"),
            0, "pH = −log[H⁺].",
            "pH = −log(10⁻³) = 3. Acidic solution.",
            "Chemistry", Difficulty.HARD, 15))

        add(Question("s3",
            "Evaluate: lim(x→0) [sin(x)/x]",
            listOf("0", "∞", "1", "undefined"),
            2, "This is a standard limit — memorise it.",
            "lim(x→0) sin(x)/x = 1. Fundamental trigonometric limit.",
            "Math", Difficulty.HARD, 15))

        add(Question("s4",
            "Which data structure gives O(1) average-case lookup?",
            listOf("Binary Search Tree", "Hash Table", "Sorted Array", "Linked List"),
            1, "Think about direct key-to-index mapping.",
            "Hash Table uses a hash function for O(1) average lookup.",
            "Technology", Difficulty.HARD, 15))

        add(Question("s5",
            "In a p-n junction diode, current flows when:",
            listOf(
                "Reverse biased",
                "Forward biased",
                "No bias applied",
                "Temperature is 0 K"
            ),
            1, "The barrier potential must be overcome.",
            "Forward bias reduces the depletion layer, allowing current to flow.",
            "Physics", Difficulty.HARD, 15))

        add(Question("s6",
            "What is the oxidation state of Cr in K₂Cr₂O₇?",
            listOf("+3", "+6", "+7", "+4"),
            1, "K is +1, O is −2. Solve for Cr.",
            "2(+1) + 2x + 7(−2) = 0 → 2x = 12 → x = +6.",
            "Chemistry", Difficulty.HARD, 18))

        add(Question("s7",
            "The time complexity of Dijkstra's algorithm with a min-heap is:",
            listOf("O(V²)", "O(E log V)", "O(V log E)", "O(E + V)"),
            1, "Each edge relaxation costs O(log V) with a heap.",
            "With a binary min-heap: O((V + E) log V) ≈ O(E log V) for dense graphs.",
            "Technology", Difficulty.HARD, 15))

        add(Question("s8",
            "If f(x) = x³ − 3x, find the local minimum value.",
            listOf("−2", "0", "2", "−3"),
            0, "Find f'(x) = 0, then check f''(x) > 0.",
            "f'(x) = 3x² − 3 = 0 → x = ±1. f(1) = 1 − 3 = −2 (local min).",
            "Math", Difficulty.HARD, 18))

        add(Question("s9",
            "Which gate produces output 1 only when all inputs are 0?",
            listOf("AND", "OR", "NOR", "NAND"),
            2, "It's the complement of OR.",
            "NOR gate: output is 1 only when all inputs are 0.",
            "Technology", Difficulty.HARD, 15))

        add(Question("s10",
            "The de Broglie wavelength of an electron moving at velocity v is:",
            listOf("λ = mv/h", "λ = h/mv", "λ = hv/m", "λ = m/hv"),
            1, "de Broglie: wavelength = Planck's constant / momentum.",
            "λ = h/p = h/(mv). Wave-particle duality for matter.",
            "Physics", Difficulty.HARD, 15))

        add(Question("s11",
            "What is the rank of the matrix [[1,2],[2,4]]?",
            listOf("0", "1", "2", "3"),
            1, "Check if rows are linearly independent.",
            "Row 2 = 2 × Row 1, so they're dependent. Rank = 1.",
            "Math", Difficulty.HARD, 18))

        add(Question("s12",
            "In thermodynamics, which process has ΔU = 0?",
            listOf("Adiabatic", "Isothermal", "Isobaric", "Isochoric"),
            1, "Internal energy depends only on temperature.",
            "Isothermal: constant temperature → ΔT = 0 → ΔU = 0 for ideal gas.",
            "Physics", Difficulty.HARD, 15))

        add(Question("s13",
            "What is the output of: print(type(lambda x: x).__name__) in Python?",
            listOf("function", "lambda", "method", "callable"),
            0, "Lambda creates an anonymous function object.",
            "Lambda expressions are of type 'function' in Python.",
            "Technology", Difficulty.HARD, 15))

        add(Question("s14",
            "The number of moles in 44g of CO₂ is: (C=12, O=16)",
            listOf("0.5", "1", "2", "4"),
            1, "Molar mass of CO₂ = 12 + 32 = 44 g/mol.",
            "44g ÷ 44 g/mol = 1 mole of CO₂.",
            "Chemistry", Difficulty.HARD, 15))

        add(Question("s15",
            "Which normal form eliminates transitive dependencies?",
            listOf("1NF", "2NF", "3NF", "BCNF"),
            2, "Transitive dependency: A→B→C where A→C transitively.",
            "3NF removes transitive dependencies. Every non-key attribute depends only on the key.",
            "Technology", Difficulty.HARD, 15))

        add(Question("s16",
            "Solve: ∫₀¹ x² dx",
            listOf("1/2", "1/3", "1/4", "1"),
            1, "Use the power rule for integration.",
            "∫x² dx = x³/3. Evaluated from 0 to 1: 1/3 − 0 = 1/3.",
            "Math", Difficulty.HARD, 15))

        add(Question("s17",
            "In a BJT transistor, the current gain β = IC/IB. If IB = 50μA and β = 100, find IC.",
            listOf("0.5 mA", "5 mA", "50 mA", "500 μA"),
            1, "IC = β × IB.",
            "IC = 100 × 50μA = 5000μA = 5 mA.",
            "Physics", Difficulty.HARD, 15))

        add(Question("s18",
            "Which chemical is used as a reducing agent in the thermite reaction?",
            listOf("Iron oxide", "Aluminium", "Carbon", "Hydrogen"),
            1, "It donates electrons to reduce iron oxide.",
            "Aluminium reduces Fe₂O₃: 2Al + Fe₂O₃ → Al₂O₃ + 2Fe. Al is oxidised.",
            "Chemistry", Difficulty.HARD, 15))

        add(Question("s19",
            "What is the space complexity of merge sort?",
            listOf("O(1)", "O(log n)", "O(n)", "O(n log n)"),
            2, "It needs auxiliary space for merging.",
            "Merge sort requires O(n) extra space for the temporary arrays during merge.",
            "Technology", Difficulty.HARD, 15))

        add(Question("s20",
            "The eigenvalues of matrix [[2,1],[0,3]] are:",
            listOf("1 and 2", "2 and 3", "0 and 3", "1 and 3"),
            1, "Upper triangular matrix: eigenvalues are the diagonal entries.",
            "For upper triangular matrices, eigenvalues = diagonal = {2, 3}.",
            "Math", Difficulty.HARD, 15))
    }

    fun getQuestionsForZone(zoneId: String, difficulty: Difficulty): List<Question> {
        val targetDifficulty = when (zoneId) {
            "zone_forest"   -> Difficulty.EASY
            "zone_ruins"    -> Difficulty.MEDIUM
            "zone_fortress" -> Difficulty.HARD
            else            -> difficulty
        }
        return allQuestions.filter { it.difficulty == targetDifficulty }.shuffled()
    }

    fun getAdaptiveQuestions(
        topic: String,
        difficulty: Difficulty,
        count: Int = 3
    ): List<Question> {
        val filtered = allQuestions.filter {
            it.difficulty == difficulty && (topic.isEmpty() || it.topic == topic)
        }
        return if (filtered.size >= count) filtered.shuffled().take(count)
        else allQuestions.filter { it.difficulty == difficulty }.shuffled().take(count)
    }

    fun getQuestionById(id: String): Question? = allQuestions.find { it.id == id }

    fun getAllTopics(): List<String> =
        allQuestions.map { it.topic }.distinct().sorted()
}
