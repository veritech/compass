package compass.ui.components.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.createBitmap
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

object NumberedMarkerIcon {
    fun createBitmap(context: Context, label: String, color: Color): Bitmap {
        val size = (48 * context.resources.displayMetrics.density).toInt()
        val bitmap = createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.color = color.toArgb()
        val rect = RectF(4f, 4f, size - 4f, size - 4f)
        canvas.drawRoundRect(rect, size / 2f, size / 2f, paint)

        paint.color = android.graphics.Color.WHITE
        paint.textSize = size * 0.4f
        paint.textAlign = Paint.Align.CENTER
        val textY = size / 2f - (paint.descent() + paint.ascent()) / 2f
        canvas.drawText(label, size / 2f, textY, paint)

        return bitmap
    }

    fun addNumberedMarker(
        mapView: MapView,
        context: Context,
        index: Int,
        latitude: Double,
        longitude: Double,
        color: Color,
    ) {
        val marker = Marker(mapView)
        marker.position = GeoPoint(latitude, longitude)
        marker.icon = BitmapDrawable(
            context.resources,
            createBitmap(context, index.toString(), color),
        )
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        mapView.overlays.add(marker)
    }
}
