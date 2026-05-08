package com.example.gamefiedsarvya.network

import com.example.gamefiedsarvya.data.models.*

/**
 * High-level Groq service.
 * Hard mode generates university/competitive-exam level questions.
 * Falls back gracefully when API unavailable.
 */
object GroqService {

    // ── Adaptive question generation ──────────────────────────────────────────

    suspend fun generateAdaptiveQuestion(
        topic: String,
        difficulty: Difficulty,
        tier: LearningTier,
        playerName: String,
        recentAccuracy: Float,
        usedQuestionTexts: Set<String> = emptySet()   // prevent repeats
    ): GeneratedQuestion? {
        if (!GroqClient.isConfigured) return null
        return try {
            val systemPrompt = buildSystemPrompt(tier, difficulty)
            val avoidClause  = if (usedQuestionTexts.isNotEmpty())
                "\nDo NOT repeat any of these questions:\n${usedQuestionTexts.take(10).joinToString("\n") { "- $it" }}"
            else ""

            val hardExtra = if (difficulty == Difficulty.HARD) """
This is HARD MODE. Requirements:
- University / competitive-exam level (JEE, NEET, GATE, GRE, STEM degree)
- Requires multi-step reasoning or deep conceptual understanding
- No trivial or definition-only questions
- Include numerical calculation or proof-based reasoning where possible
""" else ""

            val userPrompt = """
Generate ONE multiple-choice question for $playerName.
Topic: $topic
Difficulty: ${difficulty.name}
Tier: ${tier.displayName} (${tier.ageRange})
Recent accuracy: ${(recentAccuracy * 100).toInt()}%
$hardExtra$avoidClause

Respond in EXACTLY this format (no extra text, no markdown):
QUESTION: <question text>
A: <option A>
B: <option B>
C: <option C>
D: <option D>
ANSWER: <A|B|C|D>
HINT: <one-sentence hint, no answer>
EXPLANATION: <one-sentence explanation with the answer>
            """.trimIndent()

            val response = GroqClient.api.chat(
                bearer  = GroqClient.authHeader,
                request = GroqRequest(
                    model      = if (difficulty == Difficulty.HARD) "llama3-70b-8192" else "llama3-8b-8192",
                    messages   = listOf(
                        GroqMessage("system", systemPrompt),
                        GroqMessage("user", userPrompt)
                    ),
                    maxTokens   = if (difficulty == Difficulty.HARD) 500 else 300,
                    temperature = adaptiveTemperature(recentAccuracy, difficulty)
                )
            )
            parseGeneratedQuestion(response.text, topic, difficulty)
        } catch (e: Exception) {
            null
        }
    }

    // ── AI Tutor hint ─────────────────────────────────────────────────────────

    suspend fun getTutorHint(
        questionText: String,
        topic: String,
        tier: LearningTier,
        playerName: String
    ): String? {
        if (!GroqClient.isConfigured) return null
        return try {
            val response = GroqClient.api.chat(
                bearer  = GroqClient.authHeader,
                request = GroqRequest(
                    messages = listOf(
                        GroqMessage("system", buildSystemPrompt(tier, Difficulty.EASY)),
                        GroqMessage("user",
                            "Give $playerName a SHORT hint (max 20 words) without revealing the answer:\n$questionText\nTopic: $topic"
                        )
                    ),
                    maxTokens = 60, temperature = 0.5f
                )
            )
            response.text.takeIf { it.isNotBlank() }
        } catch (e: Exception) { null }
    }

    // ── Personalised feedback ─────────────────────────────────────────────────

    suspend fun getPersonalisedFeedback(
        playerName: String,
        wasCorrect: Boolean,
        topic: String,
        explanation: String,
        tier: LearningTier,
        streakCount: Int
    ): String? {
        if (!GroqClient.isConfigured) return null
        return try {
            val context = if (wasCorrect) "Correct! Streak: $streakCount." else "Incorrect. $explanation"
            val response = GroqClient.api.chat(
                bearer  = GroqClient.authHeader,
                request = GroqRequest(
                    messages = listOf(
                        GroqMessage("system", buildSystemPrompt(tier, Difficulty.EASY)),
                        GroqMessage("user",
                            "Give $playerName a SHORT (max 25 words) ${if (wasCorrect) "celebratory" else "encouraging"} message about $topic. $context"
                        )
                    ),
                    maxTokens = 80, temperature = 0.8f
                )
            )
            response.text.takeIf { it.isNotBlank() }
        } catch (e: Exception) { null }
    }

    // ── Topic summary ─────────────────────────────────────────────────────────

    suspend fun summariseTopic(
        topic: String,
        tier: LearningTier,
        playerName: String
    ): String? {
        if (!GroqClient.isConfigured) return null
        return try {
            val response = GroqClient.api.chat(
                bearer  = GroqClient.authHeader,
                request = GroqRequest(
                    messages = listOf(
                        GroqMessage("system", buildSystemPrompt(tier, Difficulty.MEDIUM)),
                        GroqMessage("user",
                            "Write a concise study summary of '$topic' for $playerName at ${tier.displayName} level. " +
                            "Use bullet points. Max 150 words. Make it engaging."
                        )
                    ),
                    maxTokens = 250, temperature = 0.6f
                )
            )
            response.text.takeIf { it.isNotBlank() }
        } catch (e: Exception) { null }
    }

    // ── Personalised greeting ─────────────────────────────────────────────────

    suspend fun getPersonalisedGreeting(
        playerName: String,
        tier: LearningTier,
        level: Int,
        accuracy: Float
    ): String? {
        if (!GroqClient.isConfigured) return null
        return try {
            val response = GroqClient.api.chat(
                bearer  = GroqClient.authHeader,
                request = GroqRequest(
                    messages = listOf(
                        GroqMessage("system", "You are a motivating learning companion. Be warm, brief, personal."),
                        GroqMessage("user",
                            "ONE-sentence welcome back for $playerName. Level $level, ${tier.displayName}, ${(accuracy * 100).toInt()}% accuracy. Max 20 words."
                        )
                    ),
                    maxTokens = 60, temperature = 0.9f
                )
            )
            response.text.takeIf { it.isNotBlank() }
        } catch (e: Exception) { null }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun buildSystemPrompt(tier: LearningTier, difficulty: Difficulty): String {
        val base = when (tier) {
            LearningTier.FOUNDATION -> "You are a friendly tutor for students aged 6-15. Use simple language, short sentences, positive reinforcement."
            LearningTier.ADVANCED   -> "You are a structured academic tutor for Class 11-12 and competitive exam students (JEE/NEET/SAT). Be precise and exam-oriented."
            LearningTier.PROFESSIONAL -> "You are a professional STEM mentor for college students. Be concise, technical, and practical. Focus on real-world applications."
        }
        val hardExtra = if (difficulty == Difficulty.HARD)
            " Generate university-level, multi-step reasoning questions. No trivial questions."
        else ""
        return base + hardExtra
    }

    private fun adaptiveTemperature(accuracy: Float, difficulty: Difficulty): Float = when {
        difficulty == Difficulty.HARD -> 0.7f   // consistent hard questions
        accuracy > 0.8f -> 0.8f
        accuracy < 0.4f -> 0.4f
        else            -> 0.6f
    }

    private fun parseGeneratedQuestion(
        raw: String,
        topic: String,
        difficulty: Difficulty
    ): GeneratedQuestion? {
        return try {
            val lines = raw.lines()
            val map = mutableMapOf<String, String>()
            lines.forEach { line ->
                val idx = line.indexOf(':')
                if (idx > 0) {
                    val key = line.substring(0, idx).trim().uppercase()
                    val value = line.substring(idx + 1).trim()
                    map[key] = value
                }
            }
            val question    = map["QUESTION"] ?: return null
            val optA        = map["A"]        ?: return null
            val optB        = map["B"]        ?: return null
            val optC        = map["C"]        ?: return null
            val optD        = map["D"]        ?: return null
            val answerKey   = map["ANSWER"]   ?: return null
            val hint        = map["HINT"]     ?: ""
            val explanation = map["EXPLANATION"] ?: ""

            val correctIndex = when (answerKey.uppercase().trim().firstOrNull()) {
                'A' -> 0; 'B' -> 1; 'C' -> 2; 'D' -> 3; else -> return null
            }

            GeneratedQuestion(
                text         = question,
                options      = listOf(optA, optB, optC, optD),
                correctIndex = correctIndex,
                hint         = hint,
                explanation  = explanation,
                topic        = topic,
                difficulty   = difficulty
            )
        } catch (e: Exception) { null }
    }

    // ── Concept explanation for Learning Mode (NEW) ───────────────────────────

    suspend fun getConceptExplanation(
        question: String,
        correctAnswer: String,
        topic: String,
        tier: LearningTier,
        playerName: String,
        simplified: Boolean = false
    ): String? {
        if (!GroqClient.isConfigured) return null
        return try {
            val style = if (simplified)
                "Use the simplest possible language. Short sentences. Analogies. No jargon."
            else
                "Be clear and educational. Use examples. Max 120 words."

            val prompt = if (simplified) """
$playerName got this wrong again. Explain it in the simplest way possible.

Question: $question
Correct answer: $correctAnswer
Topic: $topic

$style

Format:
EXPLANATION: <2-3 simple sentences explaining WHY the answer is correct>
EXAMPLE: <one real-world example>
REMEMBER: <one key takeaway in 10 words or less>
            """.trimIndent() else """
$playerName answered incorrectly. Help them understand.

Question: $question
Correct answer: $correctAnswer
Topic: $topic

$style

Format:
EXPLANATION: <clear explanation of why the answer is correct>
EXAMPLE: <one concrete example>
REMEMBER: <key takeaway in 15 words or less>
            """.trimIndent()

            val response = GroqClient.api.chat(
                bearer  = GroqClient.authHeader,
                request = GroqRequest(
                    model    = "llama3-8b-8192",
                    messages = listOf(
                        GroqMessage("system", buildSystemPrompt(tier, Difficulty.EASY)),
                        GroqMessage("user", prompt)
                    ),
                    maxTokens   = 200,
                    temperature = 0.5f
                )
            )
            response.text.takeIf { it.isNotBlank() }
        } catch (e: Exception) { null }
    }
}

data class GeneratedQuestion(
    val text: String,
    val options: List<String>,
    val correctIndex: Int,
    val hint: String,
    val explanation: String,
    val topic: String,
    val difficulty: Difficulty
)
