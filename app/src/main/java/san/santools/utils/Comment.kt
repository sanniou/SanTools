package san.santools.utils

import com.google.android.material.snackbar.Snackbar
import android.util.Log
import android.view.View
import android.widget.Toast
import san.santools.SanApplication

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

fun toast(str: String) {
    Toast.makeText(SanApplication.instance, str, Toast.LENGTH_SHORT).show()
}

fun <T> MutableCollection<T>.removeFor(filter: (a: T) -> Boolean): MutableList<T> {
    val list = mutableListOf<T>()
    val each = iterator()
    while (each.hasNext()) {
        val next = each.next()
        if (filter(next)) {
            each.remove()
            list.add(next)
        }
    }
    return list
}

fun <T> MutableCollection<T>.removeFirst(filter: (a: T) -> Boolean): Int {
    var removed = 0
    val each = iterator()
    while (each.hasNext()) {
        if (filter(each.next())) {
            each.remove()
            return removed
        }
        removed++
    }
    return -1
}
