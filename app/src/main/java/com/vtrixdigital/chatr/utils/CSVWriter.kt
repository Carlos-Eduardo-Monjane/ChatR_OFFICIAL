package com.vtrixdigital.chatr.utils


/**
Copyright 2005 Bytecode Pty Ltd.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

/*
 * The code copied from http://opencsv.sourceforge.net/
 *
 * While incorporating into secrets, the following changes were made:
 *
 * - removed the following methods to keep the bytecode smaller:
 *   writeAll(), all methods related to sql
 */

import java.io.IOException
import java.io.PrintWriter
import java.io.Writer

class CSVWriter

@JvmOverloads constructor(
    writer: Writer,
    private val separator: Char = DEFAULT_SEPARATOR,
    private val quoteChar: Char = DEFAULT_QUOTE_CHARACTER,
    private val escapeChar: Char = DEFAULT_ESCAPE_CHARACTER,
    private val lineEnd: String = DEFAULT_LINE_END
) {
    private val pw: PrintWriter = PrintWriter(writer)
    fun writeNext(nextLine: Array<String?>?) {

        if (nextLine == null)
            return

        val sb = StringBuffer()
        for (i in nextLine.indices) {

            if (i != 0) {
                sb.append(separator)
            }

            val nextElement = nextLine[i] ?: continue
            if (quoteChar != NO_QUOTE_CHARACTER)
                sb.append(quoteChar)
            for (element in nextElement) {
                if (escapeChar != NO_ESCAPE_CHARACTER && element == quoteChar) {
                    sb.append(escapeChar).append(element)
                } else if (escapeChar != NO_ESCAPE_CHARACTER && element == escapeChar) {
                    sb.append(escapeChar).append(element)
                } else {
                    sb.append(element)
                }
            }
            if (quoteChar != NO_QUOTE_CHARACTER)
                sb.append(quoteChar)
        }

        sb.append(lineEnd)
        pw.write(sb.toString())

    }

    @Throws(IOException::class)
    fun close() {
        pw.flush()
        pw.close()
    }

    companion object {

        /** The character used for escaping quotes.  */
        const val DEFAULT_ESCAPE_CHARACTER = '"'

        /** The default separator to use if none is supplied to the constructor.  */
        const val DEFAULT_SEPARATOR = ','

        /**
         * The default quote character to use if none is supplied to the
         * constructor.
         */
        const val DEFAULT_QUOTE_CHARACTER = '"'

        /** The quote constant to use when you wish to suppress all quoting.  */
        const val NO_QUOTE_CHARACTER = '\u0000'

        /** The escape constant to use when you wish to suppress all escaping.  */
        const val NO_ESCAPE_CHARACTER = '\u0000'

        /** Default line terminator uses platform encoding.  */
        const val DEFAULT_LINE_END = "\n"
    }

}