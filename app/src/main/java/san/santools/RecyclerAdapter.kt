package san.santools

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import san.santools.utils.AdapterItemsChangedCallback
import san.santools.utils.AdapterList

/**
 *author : jichang
 *time   : 2017/05/26
 *desc   :
 *version: 1.0
 */

class RecyclerAdapter<T : Any>(list: AdapterList<T>) : RecyclerView.Adapter<RecyclerViewHolder>() {
    //默认的getCount实现
    private var mOnItemCount: (() -> Int) = { data.size }

    //默认的getViewType实现
    private var mGetItemViewType: ((position: Int) -> Int) =
        { mClazzs.indexOf(data[it]::class.java) }

    private var data = list
        set(value) {
            if (field != value) {
                field.removeOnListChangedCallback(callback)
                field = value
                value.addOnListChangedCallback(callback)
                notifyDataSetChanged()
            }
        }

    private val callback = AdapterItemsChangedCallback(this).apply {
        data.addOnListChangedCallback(this)
    }

    private val mRes = mutableListOf<Int>()

    private val mBinders = mutableListOf<ViewBinder>()

    private val mClazzs = mutableListOf<Class<out Any>>()

    private var mListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        mListener = listener
    }

    fun <T : Any> registBinder(clazz: Class<T>, res: Int, binder: ViewBinder) {
        mRes.add(res)
        mClazzs.add(clazz)
        mBinders.add(binder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {

        val holder = RecyclerViewHolder(
            LayoutInflater.from(parent.context).inflate(
                mRes[viewType],
                parent,
                false
            ), mListener
        )
        mBinders[viewType].onCreteView(holder)
        return holder
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val item = data[position]
        holder.put("Item", item)
        mBinders[holder.itemViewType].onBindView(holder)
    }

    override fun onViewRecycled(holder: RecyclerViewHolder) {
        super.onViewRecycled(holder)
        mBinders[holder.itemViewType].onRecycled(holder)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerViewHolder) {
        super.onViewDetachedFromWindow(holder)
        mBinders[holder.itemViewType].onDetach(holder)
    }

    override fun getItemCount() = mOnItemCount()

    override fun getItemViewType(position: Int) = mGetItemViewType(position)
}

class RecyclerViewHolder(itemView: View, private var listener: OnItemClickListener?) :
    RecyclerView.ViewHolder(itemView) {

    val mMap = mutableMapOf<String, Any>()

    init {
        itemView.setOnClickListener {
            listener?.onItemClick(this)
        }
    }

    fun put(key: String, item: Any) = mMap.put(key, item)

    fun <T> get(key: String) = mMap[key] as T

    fun <T> getItem() = get<T>("Item")
}

interface OnItemClickListener {
    fun onItemClick(holder: RecyclerViewHolder)
}

abstract class ViewBinder() {

    abstract fun onBindView(holder: RecyclerViewHolder)

    open fun onCreteView(holder: RecyclerViewHolder) {}

    open fun onRecycled(holder: RecyclerViewHolder) {}

    open fun onDetach(holder: RecyclerViewHolder) {}
}
