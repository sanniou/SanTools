package san.santools.apps

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build.VERSION
import android.os.Bundle
import android.os.Process
import androidx.appcompat.app.AppCompatActivity
import san.santools.utils.toast

class ApkOneActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (VERSION.SDK_INT >= 21) {
            val window = window
            var i = 256
            if (VERSION.SDK_INT >= 23) {
                i = 8448
            }
            window.decorView.systemUiVisibility = i
            window.addFlags(Integer.MIN_VALUE)
            window.statusBarColor = Color.TRANSPARENT
        }
        val intent = intent
        val data = intent.data

        if (data != null) {
            if ("file" == intent.scheme) {
                val str = Manifest.permission.READ_EXTERNAL_STORAGE

                if (checkPermission(
                        str,
                        Process.myPid(),
                        Process.myUid()
                    ) != PackageManager.PERMISSION_GRANTED && VERSION.SDK_INT >= 23
                ) {
                    requestPermissions(arrayOf(str), 1)
                    return
                }
            }
        }
        val data2 = getIntent().data
        if (data2 != null) {
            InstallService.a(this, data2)
        }
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            if (grantResults.isEmpty() || grantResults[0] != 0) {
                toast("ssssssssssssssssss")
                finish()
            } else {
                val data = intent.data
                if (data != null) {
                    InstallService.a(this, data)
                }
                finish()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
