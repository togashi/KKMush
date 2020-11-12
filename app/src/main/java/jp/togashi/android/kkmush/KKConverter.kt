package jp.togashi.android.kkmush

import java.util.regex.Pattern

object KKConverter {
    class ConvertResult(keys: List<String>) {
        var output: String? = null
        val counts: MutableMap<String, Int> = mutableMapOf()

        init {
            keys.forEach {
                counts[it] = 0
            }
        }

        fun add(element: Element) {
            val k = element.id
            counts[k] = (counts[k] ?: 0) + 1
        }
    }

    class Element(
            val level: Int,
            val id: String,
            pattern: String,
            private val replacement: String
    ) {
        private val compiled: Pattern = Pattern.compile(pattern)

        fun replaceAllAndCount(src: String, cr: ConvertResult): String {
            var outStr = src
            var tmp: String?
            val m = compiled.matcher(outStr)
            while (true) {
                tmp = m.replaceFirst(replacement)
                if (outStr == tmp) break
                outStr = tmp
                cr.add(this)
            }
            cr.output = outStr
            return outStr
        }
    }

    private val elements = listOf(
            Element(1, "HRNN", "ん", "ン"),
            Element(2, "XTSU", "っ", "ッ"),
            Element(2, "XYAA", "ゃあ", "ゃア"),
            Element(3, "GOBI", "([^a-zA-Z0-9 　。ッ]+)([ 　。!！\\?？])", "$1ッ$2"),
            Element(4, "BNMT", "(.*[^a-z0-9 　。ッ])$", "$1ッ")
    )

    fun convert(src: String, level: Int): ConvertResult {
        var outStr = src
        val convertResult = ConvertResult(listOf("SRCL", "DSTL") + elements.map { e -> e.id })
        elements.filter { level >= it.level }.forEach { element ->
            outStr = element.replaceAllAndCount(outStr, convertResult)
        }
        return convertResult
    }
}
