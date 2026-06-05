package compass.data

import android.content.Context
import org.osmdroid.config.Configuration

object OsmdroidConfig {
    fun initialize(context: Context) {
        val preferences = context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        Configuration.getInstance().load(context, preferences)
        Configuration.getInstance().userAgentValue = context.packageName
    }
}
