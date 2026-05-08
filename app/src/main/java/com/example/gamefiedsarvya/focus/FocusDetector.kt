package com.example.gamefiedsarvya.focus

import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.*
import java.util.concurrent.Executors

/**
 * FocusDetector
 *
 * Uses the front camera + ML Kit Face Detection to measure:
 *   - Face presence (is the student looking at the screen?)
 *   - Eye openness (left + right eye open probability)
 *   - Head rotation (euler Y = left/right turn, euler X = up/down tilt)
 *   - Smile probability (engagement signal)
 *
 * Runs on a background executor. Results are delivered via [onResult] callback
 * at ~2 fps (throttled to avoid battery drain).
 *
 * Usage:
 *   val detector = FocusDetector(context)
 *   detector.start(lifecycleOwner) { result -> ... }
 *   detector.stop()
 */
class FocusDetector(private val context: Context) {

    private var cameraProvider: ProcessCameraProvider? = null
    private val analysisExecutor = Executors.newSingleThreadExecutor()
    private var lastAnalysisMs = 0L
    private val THROTTLE_MS = 500L   // analyse at most 2 fps

    private val detector: FaceDetector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)  // eyes + smile
            .setMinFaceSize(0.15f)   // ignore tiny faces
            .build()
        FaceDetection.getClient(options)
    }

    fun start(lifecycleOwner: LifecycleOwner, onResult: (FocusResult) -> Unit) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            try {
                cameraProvider = providerFuture.get()
                bindCamera(lifecycleOwner, onResult)
            } catch (e: Exception) {
                Log.w("FocusDetector", "Camera unavailable: ${e.message}")
                onResult(FocusResult.unavailable())
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun bindCamera(lifecycleOwner: LifecycleOwner, onResult: (FocusResult) -> Unit) {
        val provider = cameraProvider ?: return

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(analysisExecutor) { imageProxy ->
            val now = System.currentTimeMillis()
            if (now - lastAnalysisMs < THROTTLE_MS) {
                imageProxy.close()
                return@setAnalyzer
            }
            lastAnalysisMs = now
            analyseFrame(imageProxy, onResult)
        }

        try {
            provider.unbindAll()
            provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                imageAnalysis
            )
        } catch (e: Exception) {
            Log.w("FocusDetector", "Failed to bind camera: ${e.message}")
            onResult(FocusResult.unavailable())
        }
    }

    @androidx.camera.core.ExperimentalGetImage
    private fun analyseFrame(imageProxy: ImageProxy, onResult: (FocusResult) -> Unit) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) { imageProxy.close(); return }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        detector.process(image)
            .addOnSuccessListener { faces ->
                val result = buildResult(faces)
                onResult(result)
            }
            .addOnFailureListener { e ->
                Log.w("FocusDetector", "Detection failed: ${e.message}")
                onResult(FocusResult.unavailable())
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun buildResult(faces: List<Face>): FocusResult {
        if (faces.isEmpty()) return FocusResult(
            faceDetected    = false,
            focusScore      = 0.1f,
            engagementScore = 0.1f,
            alertState      = AlertState.NO_FACE,
            leftEyeOpen     = 0f,
            rightEyeOpen    = 0f,
            smileProbability = 0f,
            headTurnDeg     = 0f,
            headTiltDeg     = 0f
        )

        // Use the largest face (closest to camera)
        val face = faces.maxByOrNull { it.boundingBox.width() } ?: faces.first()

        val leftEye  = face.leftEyeOpenProbability  ?: 0.5f
        val rightEye = face.rightEyeOpenProbability ?: 0.5f
        val smile    = face.smilingProbability      ?: 0.0f
        val eulerY   = face.headEulerAngleY   // left/right turn (-ve = left)
        val eulerX   = face.headEulerAngleX   // up/down tilt

        // Focus score: eyes open + head facing forward
        val eyeScore    = ((leftEye + rightEye) / 2f).coerceIn(0f, 1f)
        val turnPenalty = (kotlin.math.abs(eulerY) / 45f).coerceIn(0f, 1f)
        val tiltPenalty = (kotlin.math.abs(eulerX) / 30f).coerceIn(0f, 1f)
        val focusScore  = (eyeScore * (1f - turnPenalty * 0.5f) * (1f - tiltPenalty * 0.3f))
            .coerceIn(0f, 1f)

        // Engagement score: smile + eyes open + face present
        val engagementScore = ((smile * 0.4f + eyeScore * 0.6f)).coerceIn(0f, 1f)

        val alertState = when {
            eyeScore < 0.3f                    -> AlertState.DROWSY
            kotlin.math.abs(eulerY) > 35f      -> AlertState.DISTRACTED
            kotlin.math.abs(eulerX) > 25f      -> AlertState.LOOKING_AWAY
            focusScore < 0.4f                  -> AlertState.LOW_FOCUS
            else                               -> AlertState.FOCUSED
        }

        return FocusResult(
            faceDetected     = true,
            focusScore       = focusScore,
            engagementScore  = engagementScore,
            alertState       = alertState,
            leftEyeOpen      = leftEye,
            rightEyeOpen     = rightEye,
            smileProbability = smile,
            headTurnDeg      = eulerY,
            headTiltDeg      = eulerX
        )
    }

    fun stop() {
        cameraProvider?.unbindAll()
        analysisExecutor.shutdown()
        detector.close()
    }
}

// ── Data models ───────────────────────────────────────────────────────────────

data class FocusResult(
    val faceDetected:     Boolean,
    val focusScore:       Float,      // 0..1  (1 = fully focused)
    val engagementScore:  Float,      // 0..1  (1 = highly engaged)
    val alertState:       AlertState,
    val leftEyeOpen:      Float,      // 0..1
    val rightEyeOpen:     Float,      // 0..1
    val smileProbability: Float,      // 0..1
    val headTurnDeg:      Float,      // euler Y
    val headTiltDeg:      Float       // euler X
) {
    companion object {
        fun unavailable() = FocusResult(
            faceDetected = false, focusScore = 0.5f, engagementScore = 0.5f,
            alertState = AlertState.UNAVAILABLE,
            leftEyeOpen = 0.5f, rightEyeOpen = 0.5f,
            smileProbability = 0f, headTurnDeg = 0f, headTiltDeg = 0f
        )
    }
}

enum class AlertState(val label: String, val emoji: String) {
    FOCUSED("Focused",       "🎯"),
    LOW_FOCUS("Low Focus",   "😐"),
    DROWSY("Drowsy",         "😴"),
    DISTRACTED("Distracted", "👀"),
    LOOKING_AWAY("Away",     "↩️"),
    NO_FACE("No Face",       "❓"),
    UNAVAILABLE("Camera Off","📷")
}
