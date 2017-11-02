package san.santools

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 *author : jichang
 *time   : 2017/05/26
 *desc   :
 *version: 1.0
 */


class RecyclerAdapter : RecyclerView.Adapter<RecyclerViewHolder>() {
    //默认的getCount实现
    private var mOnItemCount: (() -> Int) = { mData.size }

    //默认的getViewType实现
    private var mGetItemViewType: ((position: Int) -> Int) = { mClazzs.indexOf(mData[it]::class.java) }

    private lateinit var mData: List<Any>

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


    fun setData(list: List<Any>) {
        mData = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {

        val holder = RecyclerViewHolder(LayoutInflater.from(parent.context).inflate(mRes[viewType], parent, false), mListener)
        mBinders[viewType].onCreteView(holder)
        return holder
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val item = mData[position]
        holder.put("Item", item)
        mBinders[holder.itemViewType].onBindView(holder)
    }

    override fun onViewRecycled(holder: RecyclerViewHolder) {
        super.onViewRecycled(holder)
        mBinders[holder.itemViewType].onRecyclView(holder)
    }

    override fun getItemCount() = mOnItemCount()

    override fun getItemViewType(position: Int) = mGetItemViewType(position)
}

class RecyclerViewHolder(itemView: View, private var listener: OnItemClickListener?) : RecyclerView.ViewHolder(itemView) {

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

    open fun onRecyclView(holder: RecyclerViewHolder) {}
}
