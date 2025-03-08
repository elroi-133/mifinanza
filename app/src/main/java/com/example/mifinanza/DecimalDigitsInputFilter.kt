package com.example.mifinanza

import android.text.InputFilter
import android.text.Spanned
import java.util.regex.Pattern

class DecimalDigitsInputFilter : InputFilter {

    // Permitir dígitos y coma decimal
    private val pattern: Pattern = Pattern.compile("[0-9]*((,|\\.)[0-9]{0,2})?")

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val result = dest.subSequence(0, dstart).toString() + source.toString() + dest.subSequence(dend, dest.length)
        return if (!pattern.matcher(result).matches()) {
            ""
        } else null
    }
}
