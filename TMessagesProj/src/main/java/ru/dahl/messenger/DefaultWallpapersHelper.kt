package ru.dahl.messenger

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Shader
import android.util.Log
import androidx.annotation.RawRes
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.BuildVars
import org.telegram.messenger.FileLog
import org.telegram.messenger.R
import org.telegram.messenger.SvgHelper
import org.telegram.tgnet.TLRPC
import org.telegram.tgnet.TLRPC.TL_documentEmpty
import java.io.File
import java.io.FileOutputStream

object DefaultWallpapersHelper {

    private val cache = mutableSetOf<String>()

    @JvmStatic
    fun createWallpaperFiles() {
        for (wallpaper in DahlWallpaper.items) {
            var patternBitmap = SvgHelper.getBitmap(
                wallpaper.svg,
                AndroidUtilities.dp(360f),
                AndroidUtilities.dp(640f),
                Color.BLACK
            )
            patternBitmap = createTiledBitmap(patternBitmap, AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y)
            val stream= FileOutputStream(wallpaper.path)
            stream.use {
                try {
                    val bitmap: Bitmap = patternBitmap.copy(Bitmap.Config.ARGB_8888, true)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
                    bitmap.recycle()
                }catch (e: Exception){
                    FileLog.e(e)
                    Log.w("DefaultWallpapersHelper", "Create wallpaper ${wallpaper.slug} error.", e)
                }
            }
            patternBitmap.recycle()
        }
    }

    @JvmStatic
    fun getCroppedBitmapPath(path: String, width: Int, height: Int, centered: Boolean): String {
        val filename = File(path).name
        val cropPath = "${ApplicationLoader.getFilesDirFixed().path}/${width}_${height}_$filename"
        val cropFile = File(cropPath)
        if (cache.contains(cropPath) && cropFile.exists()) {
            return cropPath
        }

        val original = BitmapFactory.decodeFile(path) ?: return path
        if (width <= 0 || height <= 0) return path
        if (width > original.width || height > original.height) return path
        val x = if(centered) (original.width - width) / 2 else 0
        val y = if(centered) (original.height - height) / 2 else 0
        val cropped = Bitmap.createBitmap(original, x, y, width, height)
        original.recycle()

        val stream = FileOutputStream(cropPath)
        return stream.use {
            try {
                cropped.compress(Bitmap.CompressFormat.PNG, 90, stream)
                cropped.recycle()
                cache.add(cropPath)
                cropPath
            } catch (e: Exception) {
                if (BuildVars.LOGS_ENABLED) {
                    Log.w("DefaultWallpapersHelper", "Create crop error.", e)
                }
                path
            }
        }
    }

    @JvmStatic
    fun createTiledBitmap(tile: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap{
        if(tile.width < targetWidth || tile.height < targetHeight) {
            val shader = BitmapShader(tile, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
            val paint = Paint()
            paint.setShader(shader)

            val bitmap =
                Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)

            val canvas = Canvas(bitmap)
            canvas.drawRect(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat(), paint)
            tile.recycle()
            return bitmap
        }
        return tile
    }
}

sealed class DahlWallpaper(val slug: String, @RawRes val svg: Int, val colors: IntArray, val darkColors: IntArray) {

    data object Russia : DahlWallpaper(
        "dahl_russia",
        R.raw.dahl_wallpaper_russia,
        intArrayOf(
            0xFFEAA36E.toInt(),
            0xFFF0E486.toInt(),
            0xFFF29EBF.toInt(),
            0xFFE8C06E.toInt()
        ),
        intArrayOf(
            0xFF8ADBF2.toInt(),
            0xFF888DEC.toInt(),
            0xFFE39FEA.toInt(),
            0xFF679CED.toInt()
        )
    )

    data object Kazan : DahlWallpaper(
        "dahl_kazan",
        R.raw.dahl_wallpaper_kazan,
        intArrayOf(
            0xFF7FC289.toInt(),
            0xFFE4D573.toInt(),
            0xFFAFD677.toInt(),
            0xFFF0C07A.toInt()
        ),
        intArrayOf(
            0xFF7FA381.toInt(),
            0xFFFFF5C5.toInt(),
            0xFF336F55.toInt(),
            0xFFFBE37D.toInt()
        )
    )

    data object Piter : DahlWallpaper(
        "dahl_piter",
        R.raw.dahl_wallpaper_piter,
        intArrayOf(
            0xFFDAEACB.toInt(),
            0xFFECCBFF.toInt(),
            0xFFECCBFF.toInt(),
            0xFFB9E2FF.toInt()
        ),
        intArrayOf(
            0xFFFEC496.toInt(),
            0xFF962FBF.toInt(),
            0xFF4F5BD5.toInt(),
            0xFFFEC496.toInt()
        )

    )

    data object Moscow : DahlWallpaper(
        "dahl_moscow",
        R.raw.dahl_wallpaper_moscow,
        intArrayOf(
            0xFF7FC289.toInt(),
            0xFFECCBFF.toInt(),
            0xFFF29EBF.toInt(),
            0xFFF0C07A.toInt()
        ),

        intArrayOf(
            0xFF7FA381.toInt(),
            0xFF888DEC.toInt(),
            0xFFE39FEA.toInt(),
            0xFFFEC496.toInt()
        )
    )

    fun getColors(isDarkTheme: Boolean): IntArray = if (isDarkTheme) darkColors else colors

    val path: String = "${ApplicationLoader.getFilesDirFixed().path}/${slug}.png"

    fun toPattern(isDarkTheme: Boolean): TLRPC.TL_wallPaper {
        val wallpaper = TLRPC.TL_wallPaper()
        wallpaper.id = fileId()
        wallpaper.pattern = true
        wallpaper.isDefault = true
        wallpaper.dark = isDarkTheme
        wallpaper.slug = slug
        val document = TL_documentEmpty()
        document.localPath = path
        wallpaper.document = document
        return wallpaper
    }

    private fun fileId(): Long = when (this) {
        Kazan -> 231222124451
        Russia -> 241222124562
        Piter -> 251222124673
        Moscow -> 223442223441
    }

    companion object {

        // без lazy Russia принимает значение null. https://stackoverflow.com/a/53608393
        val items by lazy { arrayOf(Russia, Kazan, Piter, Moscow) }

        val slugs by lazy { items.map { it.slug }.toSet() }

        @JvmStatic
        fun getPatterns(isDarkTheme: Boolean): List<TLRPC.TL_wallPaper> =
            items.map { dw -> dw.toPattern(isDarkTheme)
        }

        fun getBySlug(slug: String): DahlWallpaper?{
            return items.firstOrNull { it.slug == slug }
        }
    }
}