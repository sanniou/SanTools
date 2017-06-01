package san.santools.reader

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager

/**
 * 获取屏幕的宽度px

 * @param context 上下文
 * *
 * @return 屏幕宽px
 */
fun getScreenWidth(context: Context) =
        DisplayMetrics().run {
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
                    .defaultDisplay
                    .getMetrics(this)
            widthPixels
        }

/**
 * 获取屏幕的高度px
 * @param context 上下文
 * *
 * @return 屏幕高px
 */
fun getScreenHeight(context: Context) =
        DisplayMetrics().run {
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
                    .defaultDisplay
                    .getMetrics(this)
            heightPixels
        }