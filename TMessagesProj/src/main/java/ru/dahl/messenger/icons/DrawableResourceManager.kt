package ru.dahl.messenger.icons

import android.annotation.SuppressLint
import android.content.res.*
import android.graphics.drawable.Drawable
import ru.dahl.messenger.settings.DahlSettings

//import com.google.android.exoplayer2.util.Log

@Suppress("DEPRECATION")
class DrawableResourceManager(private val wrapped: Resources) : Resources(wrapped.assets, wrapped.displayMetrics, wrapped.configuration) {
    private var activeReplacement: BaseIconReplacement = DahlSettings.getCurrentIconPack()

    fun reloadReplacements() {
        activeReplacement = DahlSettings.getCurrentIconPack()
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("UseCompatLoadingForDrawables")
    @Throws(NotFoundException::class)
    override fun getDrawable(id: Int): Drawable? {
        return wrapped.getDrawable(activeReplacement.wrap(id))
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Throws(NotFoundException::class)
    override fun getDrawable(id: Int, theme: Theme?): Drawable? {
        return wrapped.getDrawable(activeReplacement.wrap(id), theme)
    }

    @Deprecated("Deprecated in Java")
    @Throws(NotFoundException::class)
    override fun getDrawableForDensity(id: Int, density: Int): Drawable? {
        return wrapped.getDrawableForDensity(activeReplacement.wrap(id), density)
    }

    override fun getDrawableForDensity(id: Int, density: Int, theme: Theme?): Drawable? {
        return wrapped.getDrawableForDensity(activeReplacement.wrap(id), density, theme)
    }
}