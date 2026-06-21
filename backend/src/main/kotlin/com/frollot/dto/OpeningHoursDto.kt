package com.frollot.dto

data class TimeRange(
    val open: String,
    val close: String
)

data class UpdateOpeningHoursRequest(
    val openingHours: Map<String, List<TimeRange>?>?,
    val timezone: String? = null
) {
    companion object {
        private val VALID_DAYS = setOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")
        private val TIME_PATTERN = Regex("""^([01]\d|2[0-3]):[0-5]\d$""")
        private val VALID_TIMEZONES = java.time.ZoneId.getAvailableZoneIds()

        fun validate(request: UpdateOpeningHoursRequest) {
            // Validate timezone if provided
            if (request.timezone != null) {
                require(request.timezone in VALID_TIMEZONES) {
                    "Fuseau horaire invalide : '${request.timezone}'"
                }
            }

            val hours = request.openingHours ?: return

            // Validate day names
            for (day in hours.keys) {
                require(day in VALID_DAYS) {
                    "Jour invalide : '$day'. Jours acceptes : ${VALID_DAYS.joinToString()}"
                }
            }

            // Validate each day's ranges
            for ((day, ranges) in hours) {
                if (ranges == null || ranges.isEmpty()) continue // closed day

                for ((i, range) in ranges.withIndex()) {
                    require(TIME_PATTERN.matches(range.open)) {
                        "$day plage ${i + 1} : heure d'ouverture invalide '${range.open}' (format HH:mm attendu)"
                    }
                    require(TIME_PATTERN.matches(range.close)) {
                        "$day plage ${i + 1} : heure de fermeture invalide '${range.close}' (format HH:mm attendu)"
                    }
                    require(range.close > range.open) {
                        "$day plage ${i + 1} : l'heure de fermeture (${range.close}) doit etre strictement apres l'ouverture (${range.open})"
                    }
                }

                // Check no overlap between ranges of the same day (sorted by open)
                if (ranges.size > 1) {
                    val sorted = ranges.sortedBy { it.open }
                    for (i in 0 until sorted.size - 1) {
                        require(sorted[i].close <= sorted[i + 1].open) {
                            "$day : les plages ${sorted[i].open}-${sorted[i].close} et ${sorted[i + 1].open}-${sorted[i + 1].close} se chevauchent"
                        }
                    }
                }
            }
        }
    }
}

data class OpeningHoursResponse(
    val openingHours: Map<String, List<TimeRange>?>?,
    val timezone: String
)
