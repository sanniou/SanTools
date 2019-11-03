package san.santools.utils

import java.util.ArrayList

/**
 * The original [ObservableArrayList] can't be inherited, so copy the code directly and extend the move change and other methods, which is more suitable for the animation display of RecyclerView.
 *
 */
class AdapterList<T> : ArrayList<T>(), ObservableList<T> {

    @Transient
    private var mListeners: ListChangeRegistry? = ListChangeRegistry()

    override fun addOnListChangedCallback(listener: ObservableList.OnListChangedCallback<out ObservableList<T>>) {
        if (mListeners == null) {
            mListeners = ListChangeRegistry()
        }
        mListeners!!.add(listener)
    }

    override fun removeOnListChangedCallback(listener: ObservableList.OnListChangedCallback<out ObservableList<T>>) {
        if (mListeners != null) {
            mListeners!!.remove(listener)
        }
    }

    override fun add(element: T): Boolean {
        super.add(element)
        notifyAdd(size - 1, 1)
        return true
    }

    override fun add(index: Int, element: T) {
        super.add(index, element)
        notifyAdd(index, 1)
    }

    override fun addAll(elements: Collection<T>): Boolean {
        val oldSize = size
        val added = super.addAll(elements)
        if (added) {
            notifyAdd(oldSize, size - oldSize)
        }
        return added
    }
    fun set(elements: Collection<T>){
        super.clear()
        super.addAll(elements)
        changed()
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        val added = super.addAll(index, elements)
        if (added) {
            notifyAdd(index, elements.size)
        }
        return added
    }

    override fun clear() {
        val oldSize = size
        super.clear()
        if (oldSize != 0) {
            notifyRemove(0, oldSize)
        }
    }

    fun changed() {
        if (mListeners != null) {
            mListeners!!.notifyChanged(this)
        }
    }

    override fun removeAt(index: Int): T {
        val `val` = super.removeAt(index)
        notifyRemove(index, 1)
        return `val`
    }

    override fun remove(element: T): Boolean {
        val index = indexOf(element)
        return if (index >= 0) {
            removeAt(index)
            true
        } else {
            false
        }
    }

    override fun set(index: Int, element: T): T {
        val `val` = super.set(index, element)
        if (mListeners != null) {
            mListeners!!.notifyChanged(this, index, 1)
        }
        return `val`
    }

    public override fun removeRange(fromIndex: Int, toIndex: Int) {
        super.removeRange(fromIndex, toIndex)
        notifyRemove(fromIndex, toIndex - fromIndex)
    }

    private fun notifyAdd(start: Int, count: Int) {
        if (mListeners != null) {
            mListeners!!.notifyInserted(this, start, count)
        }
    }

    private fun notifyRemove(start: Int, count: Int) {
        if (mListeners != null) {
            mListeners!!.notifyRemoved(this, start, count)
        }
    }

    /**
     * ----------------------------------------------------  ----------------------------------------------------
     */

    override fun removeAll(elements: Collection<T>): Boolean {
        throw RuntimeException("can not use this function now")
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        throw RuntimeException("can not use this function now")
    }

    fun swap(i: Int, j: Int) {
        super.set(i, super.set(j, super.get(i)))
    }

    fun change(i: Int) {
        if (mListeners != null) {
            mListeners!!.notifyChanged(this, i, 1)
        }
    }

    fun move(i: Int, j: Int) {
        val remove = super.removeAt(i)
        super.add(j, remove)
        if (mListeners != null) {
            mListeners!!.notifyMoved(this, i, j, 1)
        }
    }
}