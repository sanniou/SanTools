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

typealias ViewBinder<T> = (RecyclerViewHolder, T) -> Unit

class RecyclerAdapter : RecyclerView.Adapter<RecyclerViewHolder>() {
    //默认的getCount实现
    private var mOnItemCount: (() -> Int) = { mData.size }

    //默认的getViewType实现
    private var mGetItemViewType: ((position: Int) -> Int) = { mClazzs.indexOf(mData[it].javaClass) }

    private val mData = mutableListOf<Any>()

    private val mRes = mutableListOf<Int>()

    private val mBinders = mutableListOf<ViewBinder<*>>()

    private val mClazzs = mutableListOf<Class<out Any>>()

    fun <T : Any> register(clazz: Class<T>, res: Int, binder: ViewBinder<T>) {
        mRes.add(res)
        mClazzs.add(clazz)
        mBinders.add(binder)
    }

    fun setData(list: List<Any>) {
        mData.clear()
        mData.addAll(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            RecyclerViewHolder(LayoutInflater.from(parent.context).inflate(mRes[viewType], parent, false))

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        mBinders[holder.itemViewType](holder, mData[position])
    }

    override fun getItemCount() = mOnItemCount()

    override fun getItemViewType(position: Int) = mGetItemViewType(position)
}

class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
