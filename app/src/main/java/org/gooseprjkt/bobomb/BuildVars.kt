package org.gooseprjkt.bobomb

object BuildVars {
    const val TELEGRAM_URL = "https://t.me/androidsmsbobomb"
    const val SOURCECODE_URL = "https://github.com/gooseteam-hackers/Bobomb-Android.git"
    const val DONATE_URL = "https://smsbomber.page.link/donate"

    const val DATA_SOURCE = "https://gitlab.com/retrocat/bomber-static/-/raw/main/static75.json?ref_type=heads"

    val COUNTRY_CODES = arrayOf("7", "380", "375", "77", "90", "")

    val COUNTRY_FLAGS = intArrayOf(
        R.drawable.ic_ru,
        R.drawable.ic_uk,
        R.drawable.ic_by,
        R.drawable.ic_kz,
        R.drawable.ic_tr,
        R.drawable.ic_all
    )

    const val PHONE_ANY_LENGTH = 0

    val MAX_PHONE_LENGTH = intArrayOf(10, 9, 9, 9, 10, PHONE_ANY_LENGTH)

    const val SCHEDULED_ATTACKS_LIMIT = 12

    const val MAX_REPEATS_COUNT = 10
    const val REPEATS_MAX_LENGTH = MAX_REPEATS_COUNT.toString().length

    enum class AttackSpeed(val chunkSize: Int, val label: String) {
        SLOW(1, "Медленно"),
        DEFAULT(8, "Обычно"),
        FAST(17, "Быстро")
    }


    const val DRIP_MODE_DELAY_MS = 1200000L // 20 минут
    const val DRIP_MODE_ENABLED_BY_DEFAULT = false


    const val DEBUG_MODE_ENABLED_BY_DEFAULT = false
    const val UI_DEBUG_ENABLED_BY_DEFAULT = false
    const val BOMBER_SERVICE_DEBUG_ENABLED_BY_DEFAULT = false
    const val DRIP_MODE_DEBUG_ENABLED_BY_DEFAULT = false
    const val VERBOSE_LOGGING_ENABLED_BY_DEFAULT = false
    const val NETWORK_DEBUG_ENABLED_BY_DEFAULT = false
    const val SERVICE_TIMEOUT_INCREASED_BY_DEFAULT = false
    const val RANDOM_USER_AGENT_ENABLED_BY_DEFAULT = true
    const val RETRY_FAILED_REQUESTS_ENABLED_BY_DEFAULT = true
    const val SSL_SKIP_VERIFICATION_ENABLED_BY_DEFAULT = false
}
