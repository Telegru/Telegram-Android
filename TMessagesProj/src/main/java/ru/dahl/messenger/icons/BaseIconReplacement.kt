package ru.dahl.messenger.icons

import android.util.SparseIntArray
import androidx.core.util.containsKey

abstract class BaseIconReplacement {
    abstract val replacements: SparseIntArray

    fun wrap(id: Int): Int {
        if (replacements.containsKey(id)) return replacements[id]
        return id
    }
}

fun newSparseInt(vararg intPairs: Pair<Int, Int>) = SparseIntArray().apply {
    intPairs.forEach {
        this.put(it.first, it.second)
    }
}
