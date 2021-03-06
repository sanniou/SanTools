package san.santools.apps

import android.content.Intent
import android.graphics.drawable.Drawable

/**
 *author : jichang
 *time   : 2017/05/26
 *desc   :
 *version: 1.0
 */
data class AppItem(val name: String, val icon: Drawable, val isSystemApp: Boolean,
                   val firstTime: Long, val lastTime: Long,
                   val versionCode: Long, val versionName: String,
                   val packageName: String,
                   val intent: Intent?, val size: Long, val allInfo: String)

