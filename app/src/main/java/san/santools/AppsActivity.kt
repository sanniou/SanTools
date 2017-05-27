package san.santools

import android.content.pm.PackageManager.GET_UNINSTALLED_PACKAGES
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_apps.*
import kotlinx.android.synthetic.main.item_app.view.*

class AppsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)
        val s = mutableListOf<Any>()
        getAppList(s)
        recycler.run {
            layoutManager = LinearLayoutManager(this@AppsActivity)
            adapter = RecyclerAdapter().apply {
                register(AppItem::class.java, R.layout.item_app) {
                    holder, item ->
                    holder.itemView.run {
                        app_name.text = item.name
                        app_icon.setImageDrawable(item.icon)
                    }
                }
                setData(s)
            }
        }
    }

    private fun getAppList(list: MutableList<Any>) =
            packageManager.run {
                getInstalledApplications(GET_UNINSTALLED_PACKAGES)
                        //使用fold 因为可以返回其他类型
                        .fold(list) {
                            cl, b ->
                            cl.add(AppItem("" +
                                    "name : ${b.name}\n" +
                                    "packageName ： ${b.packageName}\n" +
                                    "labelRes ： ${b.labelRes}\n" +
                                    "nonLocalizedLabel ： ${b.nonLocalizedLabel}\n" +
                                    "icon ：${b.icon}\n" +
                                    "banner ：${b.banner}\n" +
                                    "logo ：${b.logo}\n" +
                                    "taskAffinity ：${b.taskAffinity}\n" +
                                    "permission ：${b.permission}\n" +
                                    "processName : ${b.processName}\n" +
                                    "className : ${b.className}\n" +
                                    "descriptionRes : ${b.descriptionRes}\n" +
                                    "theme : ${b.theme}\n" +
                                    "flags: ${b.flags}\n" +
                                    "requiresSmallestWidthDp: ${b.requiresSmallestWidthDp}\n" +
                                    "compatibleWidthLimitDp: ${b.compatibleWidthLimitDp}\n" +
                                    "largestWidthLimitDp: ${b.largestWidthLimitDp}\n" +
                                    "manageSpaceActivityName： ${b.manageSpaceActivityName}\n" +
                                    "backupAgentName ： ${b.backupAgentName}\n" +
                                    "sourceDir : ${b.sourceDir}\n" +
                                    "publicSourceDir : ${b.publicSourceDir}\n" +
                                    "splitSourceDirs: ${b.splitSourceDirs}\n" +
                                    "splitPublicSourceDirs : ${b.splitPublicSourceDirs}\n" +
                                    "sharedLibraryFiles : ${b.sharedLibraryFiles}\n" +
                                    "dataDir ： ${b.dataDir}\n" +
                                    "deviceProtectedDataDir : ${b.deviceProtectedDataDir}\n" +
                                    "nativeLibraryDir : ${b.nativeLibraryDir}\n" +
                                    "uid : ${b.uid}\n" +
                                    "uiOptions : ${b.uiOptions}\n" +
                                    "minSdkVersion : ${b.minSdkVersion}\n" +
                                    "targetSdkVersion : ${b.targetSdkVersion}\n" +
                                    "enabled : ${b.enabled}\n" +
                                    "", b.loadIcon(this)))
                            cl
                        }

            }
}
