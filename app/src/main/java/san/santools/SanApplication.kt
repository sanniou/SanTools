package san.santools

import android.app.Application
import android.content.Context

class SanApplication : Application() {

    companion object {
        lateinit var instance: Context
    }

    override fun onCreate() {
        super.onCreate()
        instance = applicationContext
    }
}