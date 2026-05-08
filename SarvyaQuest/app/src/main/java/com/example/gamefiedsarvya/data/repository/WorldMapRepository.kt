package com.example.gamefiedsarvya.data.repository

import com.example.gamefiedsarvya.data.models.*

/**
 * Provides the three tier world maps.
 * All content is offline-first.
 */
object WorldMapRepository {

    // ═══════════════════════════════════════════════════════════════════════════
    //  FOUNDATION MAP  – colorful, forest/city/learning zones
    // ═══════════════════════════════════════════════════════════════════════════

    private val foundationMap = TierWorldMap(
        tier  = LearningTier.FOUNDATION,
        style = MapStyle.COLORFUL,
        title = "Learning Kingdom",
        nodes = listOf(
            MapNode("fn_start",  "Start",           MapNodeType.LESSON,    "Basics",      10,  0.50f, 0.90f, emptyList(),          true,  false, "Your journey begins!",    "S"),
            MapNode("fn_abc",    "Alphabet",         MapNodeType.LESSON,    "Language",    15,  0.25f, 0.78f, listOf("fn_start"),    true,  false, "Learn letters and words", "A"),
            MapNode("fn_num",    "Numbers",          MapNodeType.LESSON,    "Math",        15,  0.75f, 0.78f, listOf("fn_start"),    true,  false, "Count and calculate",     "N"),
            MapNode("fn_sci",    "Science",          MapNodeType.LESSON,    "Science",     20,  0.50f, 0.65f, listOf("fn_abc","fn_num"), false, false, "Explore nature",       "Sc"),
            MapNode("fn_quiz1",  "Quiz 1",           MapNodeType.QUIZ,      "Mixed",       25,  0.30f, 0.52f, listOf("fn_sci"),      false, false, "Test your basics",        "Q"),
            MapNode("fn_geo",    "Geography",        MapNodeType.LESSON,    "Geography",   20,  0.70f, 0.52f, listOf("fn_sci"),      false, false, "Explore the world",       "G"),
            MapNode("fn_hub",    "Study Hub",        MapNodeType.HUB,       "Mixed",       30,  0.50f, 0.40f, listOf("fn_quiz1","fn_geo"), false, false, "Study and prepare",  "H"),
            MapNode("fn_tech",   "Technology",       MapNodeType.LESSON,    "Technology",  25,  0.25f, 0.28f, listOf("fn_hub"),      false, false, "Computers and gadgets",   "T"),
            MapNode("fn_art",    "Creative",         MapNodeType.LESSON,    "Creative",    20,  0.75f, 0.28f, listOf("fn_hub"),      false, false, "Create and express",      "C"),
            MapNode("fn_boss",   "Final Boss",       MapNodeType.BOSS,      "Mixed",       80,  0.50f, 0.12f, listOf("fn_tech","fn_art"), false, false, "Prove your mastery!", "B")
        ),
        connections = listOf(
            "fn_start" to "fn_abc", "fn_start" to "fn_num",
            "fn_abc" to "fn_sci",   "fn_num" to "fn_sci",
            "fn_sci" to "fn_quiz1", "fn_sci" to "fn_geo",
            "fn_quiz1" to "fn_hub", "fn_geo" to "fn_hub",
            "fn_hub" to "fn_tech",  "fn_hub" to "fn_art",
            "fn_tech" to "fn_boss", "fn_art" to "fn_boss"
        )
    )

    // ═══════════════════════════════════════════════════════════════════════════
    //  ADVANCED MAP  – structured, chapters/modules/exam paths
    // ═══════════════════════════════════════════════════════════════════════════

    private val advancedMap = TierWorldMap(
        tier  = LearningTier.ADVANCED,
        style = MapStyle.STRUCTURED,
        title = "Exam Pathway",
        nodes = listOf(
            MapNode("av_ch1",    "Ch.1 Foundations", MapNodeType.LESSON, "Math",       20,  0.50f, 0.90f, emptyList(),           true,  false, "Core mathematical concepts", "1"),
            MapNode("av_ch2m",   "Ch.2 Algebra",     MapNodeType.LESSON, "Math",       25,  0.25f, 0.75f, listOf("av_ch1"),       false, false, "Equations and functions",    "2M"),
            MapNode("av_ch2p",   "Ch.2 Physics",     MapNodeType.LESSON, "Science",    25,  0.75f, 0.75f, listOf("av_ch1"),       false, false, "Mechanics and waves",        "2P"),
            MapNode("av_quiz1",  "Module Test 1",    MapNodeType.QUIZ,   "Math",       35,  0.50f, 0.60f, listOf("av_ch2m","av_ch2p"), false, false, "Timed module assessment", "Q1"),
            MapNode("av_ch3",    "Ch.3 Calculus",    MapNodeType.LESSON, "Math",       30,  0.25f, 0.45f, listOf("av_quiz1"),     false, false, "Derivatives and integrals",  "3C"),
            MapNode("av_ch3c",   "Ch.3 Chemistry",   MapNodeType.LESSON, "Science",    30,  0.75f, 0.45f, listOf("av_quiz1"),     false, false, "Reactions and bonding",      "3X"),
            MapNode("av_hub",    "Revision Hub",     MapNodeType.HUB,    "Mixed",      40,  0.50f, 0.32f, listOf("av_ch3","av_ch3c"), false, false, "Revise before the boss",  "H"),
            MapNode("av_mock",   "Mock Exam",        MapNodeType.QUIZ,   "Mixed",      50,  0.30f, 0.18f, listOf("av_hub"),       false, false, "Full timed mock test",       "M"),
            MapNode("av_comp",   "Competitive Prep", MapNodeType.LESSON, "Mixed",      40,  0.70f, 0.18f, listOf("av_hub"),       false, false, "JEE / NEET patterns",        "CP"),
            MapNode("av_boss",   "Final Exam",       MapNodeType.BOSS,   "Mixed",      100, 0.50f, 0.05f, listOf("av_mock","av_comp"), false, false, "Ultimate mastery test",  "B")
        ),
        connections = listOf(
            "av_ch1" to "av_ch2m",  "av_ch1" to "av_ch2p",
            "av_ch2m" to "av_quiz1","av_ch2p" to "av_quiz1",
            "av_quiz1" to "av_ch3", "av_quiz1" to "av_ch3c",
            "av_ch3" to "av_hub",   "av_ch3c" to "av_hub",
            "av_hub" to "av_mock",  "av_hub" to "av_comp",
            "av_mock" to "av_boss", "av_comp" to "av_boss"
        )
    )

    // ═══════════════════════════════════════════════════════════════════════════
    //  PROFESSIONAL MAP  – network-style, skills/domains/career paths
    // ═══════════════════════════════════════════════════════════════════════════

    private val professionalMap = TierWorldMap(
        tier  = LearningTier.PROFESSIONAL,
        style = MapStyle.NETWORK,
        title = "Skill Network",
        nodes = listOf(
            MapNode("pr_core",   "Core",          MapNodeType.LESSON, "CS",         30,  0.50f, 0.90f, emptyList(),            true,  false, "Data structures and algorithms", "C"),
            MapNode("pr_algo",   "Algorithms",    MapNodeType.LESSON, "CS",         40,  0.20f, 0.72f, listOf("pr_core"),       false, false, "Sorting, searching, graphs",     "Al"),
            MapNode("pr_sys",    "Systems",       MapNodeType.LESSON, "Engineering",40,  0.80f, 0.72f, listOf("pr_core"),       false, false, "Architecture and scalability",   "Sy"),
            MapNode("pr_ml",     "ML",            MapNodeType.LESSON, "AI/ML",      50,  0.35f, 0.55f, listOf("pr_algo"),       false, false, "Models and training",            "ML"),
            MapNode("pr_db",     "Databases",     MapNodeType.LESSON, "Engineering",40,  0.65f, 0.55f, listOf("pr_sys"),        false, false, "SQL, NoSQL, indexing",           "DB"),
            MapNode("pr_hub",    "Research Hub",  MapNodeType.HUB,    "Mixed",      50,  0.50f, 0.40f, listOf("pr_ml","pr_db"), false, false, "Deep dive and research",         "H"),
            MapNode("pr_cloud",  "Cloud/DevOps",  MapNodeType.LESSON, "Engineering",45,  0.20f, 0.25f, listOf("pr_hub"),        false, false, "AWS, CI/CD, containers",         "Cl"),
            MapNode("pr_sec",    "Security",      MapNodeType.LESSON, "CS",         45,  0.50f, 0.22f, listOf("pr_hub"),        false, false, "Cryptography and threats",       "Se"),
            MapNode("pr_ai",     "Advanced AI",   MapNodeType.LESSON, "AI/ML",      55,  0.80f, 0.25f, listOf("pr_hub"),        false, false, "Deep learning and LLMs",         "AI"),
            MapNode("pr_boss",   "Capstone",      MapNodeType.BOSS,   "Mixed",      150, 0.50f, 0.06f, listOf("pr_cloud","pr_sec","pr_ai"), false, false, "Real-world scenario test", "B")
        ),
        connections = listOf(
            "pr_core" to "pr_algo",  "pr_core" to "pr_sys",
            "pr_algo" to "pr_ml",    "pr_sys" to "pr_db",
            "pr_ml" to "pr_hub",     "pr_db" to "pr_hub",
            "pr_hub" to "pr_cloud",  "pr_hub" to "pr_sec",  "pr_hub" to "pr_ai",
            "pr_cloud" to "pr_boss", "pr_sec" to "pr_boss", "pr_ai" to "pr_boss"
        )
    )

    fun getMapForTier(tier: LearningTier): TierWorldMap = when (tier) {
        LearningTier.FOUNDATION   -> foundationMap
        LearningTier.ADVANCED     -> advancedMap
        LearningTier.PROFESSIONAL -> professionalMap
    }

    fun getNode(tier: LearningTier, nodeId: String): MapNode? =
        getMapForTier(tier).nodes.find { it.id == nodeId }

    fun getUnlockedNodes(tier: LearningTier, completedIds: Set<String>): List<MapNode> {
        val map = getMapForTier(tier)
        return map.nodes.map { node ->
            val prereqsMet = node.prerequisites.all { it in completedIds }
            node.copy(
                isUnlocked  = prereqsMet || node.prerequisites.isEmpty(),
                isCompleted = node.id in completedIds
            )
        }
    }
}
