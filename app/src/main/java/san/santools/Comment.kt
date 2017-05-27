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

inline fun <reified T> T.debug(log: Any) {
    Log.d(T::class.simpleName, log.toString())
}

inline fun <reified T> T.error(log: Any) {
    Log.e(T::class.simpleName, log.toString())
}
