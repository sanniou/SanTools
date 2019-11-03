package san.santools.apps

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION
import android.os.ParcelFileDescriptor
import san.santools.utils.toast
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T

class InstallService : IntentService(InstallService::class.simpleName) {

    companion object {
        fun a(context: Context, fileDescriptor: ParcelFileDescriptor?, file: String): File? {

            if (fileDescriptor != null) {
                val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
                val apkFile = File(context.externalCacheDir, file)
                val outputStream = FileOutputStream(apkFile)
                val outputChannel = outputStream.channel
                val inputChannel = inputStream.channel
                inputChannel.transferTo(0, Long.MAX_VALUE, outputChannel)
                inputStream.close()
                outputChannel.close()
                return apkFile
            }
            return null
        }

        fun a(context: Context, uri: Uri) {
            val intent = Intent(context, InstallService::class.java)
            intent.data = uri
            if (VERSION.SDK_INT >= 26) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val sb = StringBuilder()
            sb.append(intent.getIntExtra("EXTRA_START_ID", 0))
            sb.append(".apk")
            val sb2 = sb.toString()

            // fixme
            if (true) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.setDataAndType(
                    intent.data,
                    "application/vnd.android.package-archive"
                )
                startActivity(intent)

                return
            }
            if (intent.data != null) {
                try {
                    val a2 = a(
                        this, contentResolver.openFileDescriptor(intent.data!!, "r"), sb2
                    )
                    if (a2 != null) {
                        a(this, a2)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    toast(e.message ?: "")
                }
            }
        }
    }

    fun a(context: Context, file: File) {
/*        val uri: Uri;
        val z = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            "compat_mode",
            false
        );
        val intent = Intent("android.intent.action.VIEW");
        if (VERSION.SDK_INT < 24 || z) {
            uri = Uri.fromFile(file)
        } else {
            val bVar = FileProvider.getUriForFile(context, "com.SanTools.fileProvider")
            try {
                val canonicalPath = file.canonicalPath;
                var entry = null;
                for (Entry entry2 : FileProvider.f10b.entrySet()) {
                    val path = ((File) entry2 . getValue ()).getPath();
                    if (canonicalPath.startsWith(path) && (entry == null || path.length() > ((File) entry . getValue ()).getPath().length())) {
                        entry = entry2;
                    }
                }
                if (entry != null) {
                    var path2 = ((File) entry . getValue ()).getPath();
                    var str = "/";
                    var substring =
                        canonicalPath.substring(path2.endsWith(str) ? path2 . length () : path2.length()+1);
                    StringBuilder sb = StringBuilder ();
                    sb.append(Uri.encode((String) entry . getKey ()));
                    sb.append('/');
                    sb.append(Uri.encode(substring, str));
                    uri =
                        Builder().scheme("content").authority(bVar.f9a).encodedPath(sb.toString())
                            .build();
                    intent.addFlags(1);
                } else {
                    var sb2 = StringBuilder();
                    sb2.append("Failed to find configured root that contains ");
                    sb2.append(canonicalPath);
                    throw new IllegalArgumentException (sb2.toString());
                }
            } catch (unused: IOException) {
                var sb3 = StringBuilder();
                sb3.append("Failed to resolve canonical path for ");
                sb3.append(file);
                throw  IllegalArgumentException(sb3.toString());
            }
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        intent.addFlags(268435456);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }*/
    }
}

