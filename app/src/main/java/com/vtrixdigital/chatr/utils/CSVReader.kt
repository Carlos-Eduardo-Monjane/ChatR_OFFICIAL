package com.vtrixdigital.chatr.utils


import java.io.BufferedReader
import java.io.IOException
import java.io.Reader
import java.util.ArrayList

class CSVReader
@JvmOverloads constructor(
    reader: Reader,
    private val separator: Char = DEFAULT_SEPARATOR,
    private val quoteChar: Char = DEFAULT_QUOTE_CHARACTER,
    private val skipLines: Int = DEFAULT_SKIP_LINES
) {
    private val br: BufferedReader = BufferedReader(reader)
    private var hasNext = true
    private var linesSkipped: Boolean = false
    private val nextLine: String?
        @Throws(IOException::class)
        get() {
            if (!this.linesSkipped) {
                for (i in 0 until skipLines) {
                    br.readLine()
                }
                this.linesSkipped = true
            }
            val nextLine = br.readLine()
            if (nextLine == null) {
                hasNext = false
            }
            return if (hasNext) nextLine else null
        }

    @Throws(IOException::class)
    fun readNext(): Array<String>? {
        val nextLine = nextLine
        return if (hasNext) parseLine(nextLine) else null
    }

    @Throws(IOException::class)
    private fun parseLine(nextLine1: String?): Array<String>? {
        val nextLine = nextLine1 ?: return null
        val tokensOnThisLine = ArrayList<String>()
        var sb = StringBuffer()
        var inQuotes = false
        do {
            if (inQuotes) {
                sb.append("\n")
            }
            var i = 0
            while (i < nextLine.length) {
                val c = nextLine[i]
                if (c == quoteChar) {
                    if (inQuotes && nextLine.length > i + 1  && nextLine[i + 1] == quoteChar
                    ) {
                        sb.append(nextLine[i + 1])
                        i++
                    } else {
                        inQuotes = !inQuotes
                        if (i > 2 && nextLine[i - 1] != this.separator && nextLine.length > i + 1 &&
                            nextLine[i + 1] != this.separator
                        ) {
                            sb.append(c)
                        }
                    }
                } else if (c == separator && !inQuotes) {
                    tokensOnThisLine.add(sb.toString())
                    sb = StringBuffer() // start work on next token
                } else {
                    sb.append(c)
                }
                i++
            }
        } while (inQuotes)
        tokensOnThisLine.add(sb.toString())
        return tokensOnThisLine.toTypedArray()

    }
    companion object {
        const val DEFAULT_SEPARATOR = ','
        const val DEFAULT_QUOTE_CHARACTER = '"'
        const val DEFAULT_SKIP_LINES = 0
    }
}