package com.meshmap.app.classifier

object UrgencyClassifier {
    private val criticalKeywords = setOf(
        "sos", "help", "emergency", "trapped", "bleeding",
        "dying", "fire", "flood", "earthquake", "rescue",
        "ambulance", "injury", "injured", "unconscious",
        "heart attack", "stroke", "drowning", "collapsed",
        "shooter", "gunshot", "bomb", "explosion"
    )
    private val urgentKeywords = setOf(
        "urgent", "need help", "lost", "stuck", "broken",
        "pain", "medical", "danger", "warning", "alert",
        "accident", "crash", "fallen", "stranded"
    )

    fun classify(text: String): Int {
        val lower = text.lowercase()
        return when {
            criticalKeywords.any { lower.contains(it) } -> 2  // CRITICAL
            urgentKeywords.any { lower.contains(it) } -> 1    // URGENT
            else -> 0                                          // NORMAL
        }
    }
}
