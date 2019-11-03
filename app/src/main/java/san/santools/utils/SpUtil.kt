package san.santools.utils

import android.content.Context.MODE_PRIVATE
import san.santools.SanApplication

const val SP_NAME = "SANTOOLS"

object SpUtil {
    val mSp = SanApplication.instance.getSharedPreferences(SP_NAME, MODE_PRIVATE)
    val mEditor = mSp.edit()

    fun put(key: String, any: Any) {
        when (any) {
            is String -> mEditor.putString(key, any)
            is Int -> mEditor.putInt(key, any)
            is Boolean -> mEditor.putBoolean(key, any)
            is Float -> mEditor.putFloat(key, any)
            is Long -> mEditor.putLong(key, any)
            else -> mEditor.putString(key, any.toString())
        }
        mEditor.apply()
    }

    operator fun <T> get(key: String, any: T): T? {
        return when (any) {
            is String -> mSp.getString(key, any) as T?
            is Int -> mSp.getInt(key, any) as T?
            is Boolean -> mSp.getBoolean(key, any) as T?
            is Float -> mSp.getFloat(key, any) as T?
            is Long -> mSp.getLong(key, any) as T?
            else -> null
        }
    }
}