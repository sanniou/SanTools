package san.santools

import android.support.design.widget.Snackbar
import android.util.Log
import android.view.View
import java.util.function.Predicate

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
    Log.e(T::class.simpleName, str.toString())
}


fun <T> MutableCollection<T>.removeFor(filter: (a: T) -> Boolean): Boolean {
    var removed = false
    val each = iterator()
    while (each.hasNext()) {
        if (filter.invoke(each.next())) {
            each.remove()
            removed = true
        }
    }
    return removed
}

fun <T> MutableCollection<T>.removeFirst(filter: (a: T) -> Boolean): Int {
    var removed = 0
    val each = iterator()
    while (each.hasNext()) {
        if (filter.invoke(each.next())) {
            each.remove()
            return removed
        }
        removed++
    }
    return -1
}
