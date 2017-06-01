package san.santools

import android.support.design.widget.Snackbar
import android.util.Log
import android.view.View

/**
 *author : jichang
 *time   : 2017/05/27
 *desc   :扩展工具
 *version: 1.0
 */

fun View.snackBar(str: String) = Snackbar.make(this, str, Snackbar.LENGTH_SHORT).show()

fun View.longSnackBar(str: String) = Snackbar.make(this, str, Snackbar.LENGTH_LONG).show()

inline fun <reified T> T.Logd(log: Any) {
    Log.d(T::class.simpleName, log.toString())
}

inline fun <reified T> T.Loge(str: Any) {
    val strBuffer = StringBuffer()
//    val stackTrace = Throwable().stackTrace
    strBuffer
//            .append("; class:").append(stackTrace[1].className).append("\n")
//            .append("; method:").append(stackTrace[1].methodName).append("\n")
//            .append("; number:").append(stackTrace[1].lineNumber).append("\n")
//            .append("; fileName:").append(stackTrace[1].fileName).append("\n")
            .append(str.toString())
    Log.e(T::class.simpleName, strBuffer.toString())
}

