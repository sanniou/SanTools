package san.santools.utils

import san.santools.apps.AppItem
import java.text.Collator
import java.util.Locale

interface AppSortComparator : Comparator<AppItem> {
    var invert: Boolean

    override fun compare(o1: AppItem, o2: AppItem): Int {
        var a = o1
        var b = o2
        if (invert) {
            a = b.also { b = a }
        }
        return compareField(a, b)
    }

    fun compareField(o1: AppItem, o2: AppItem): Int
}

class UpdateTimeSort(override var invert: Boolean = false) : AppSortComparator {

    override fun compareField(o1: AppItem, o2: AppItem) = o2.lastTime.compareTo(o1.lastTime)
}

class FirstTimeSort(override var invert: Boolean = false) : AppSortComparator {
    override fun compareField(o1: AppItem, o2: AppItem) = o2.firstTime.compareTo(o1.firstTime)
}

class SizeSort(override var invert: Boolean = false) : AppSortComparator {
    override fun compareField(o1: AppItem, o2: AppItem) = o2.size.compareTo(o1.size)
}

class NameSort(override var invert: Boolean = false) : AppSortComparator {
    override fun compareField(o1: AppItem, o2: AppItem) =
        Collator.getInstance(Locale.CHINA).compare(o1.name, o2.name)
}
