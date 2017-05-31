package san.santools

import android.app.AlertDialog
import android.content.pm.ApplicationInfo
import android.content.pm.ApplicationInfo.FLAG_SYSTEM
import android.content.pm.PackageInfo
import android.content.pm.PackageManager.*
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_apps.*
import kotlinx.android.synthetic.main.item_app.view.*
import kotlinx.android.synthetic.main.item_switch.view.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 简单功能，就不做mvp了
 */
class AppsActivity : AppCompatActivity() {

    val mList = mutableListOf<Any>()
    val mAppList = mutableListOf<AppItem>()
    val b: String = "应用"
    var mIsAll = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)
        setSupportActionBar(toolbar)
        getAppList(mAppList)
        recycler.run {
            addItemDecoration(DividerItemDecoration(this@AppsActivity, DividerItemDecoration.VERTICAL))
            layoutManager = LinearLayoutManager(this@AppsActivity)
            adapter = RecyclerAdapter().apply {
                register(AppItem::class.java, R.layout.item_app) {
                    holder, item ->
                    holder.itemView.run {
                        //FIXME 此处在onBindViewHolder(),不合理的监听器设置写法
                        app_name.text = "${item.name} \n ${item.size} b\n ${item.versionCode} \n ${item.versionName}"
                        app_name.setOnClickListener {
                            AlertDialog.Builder(this@AppsActivity).setMessage(item.allInfo).show()
                        }
                        app_icon.setImageDrawable(item.icon)
                        app_icon.setOnClickListener {
                            item.intent
                                    ?.run { startActivity(item.intent) }
                                    ?: run { recycler.snackBar("启动个p") }
                        }
                    }
                }
                register(String::class.java, R.layout.item_switch) {
                    holder, _ ->
                    //FIXME 此处在onBindViewHolder(),不合理的监听器设置写法
                    holder.itemView
                            .app_switch.apply {
                        setOnCheckedChangeListener(null)
                        isChecked.takeIf { it != mIsAll }?.run { isChecked = mIsAll }
                    }
                            .setOnCheckedChangeListener {
                                _, isChecked ->
                                mIsAll = isChecked
                                if (isChecked) {
                                    updateData(mAppList, recycler.adapter as RecyclerAdapter)
                                } else {
                                    mAppList.filter { !it.isSystemApp }
                                            .run {
                                                updateData(this, recycler.adapter as RecyclerAdapter)
                                            }
                                }
                            }
                }
                updateData(mAppList, this)

            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_act_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        mIsAll = true
        val adapter = recycler.adapter as RecyclerAdapter
        mAppList.sortWith(Comparator { o1, o2 ->
            when (item?.itemId) {
                R.id.update_time_sort -> (o1.lastTime - o2.lastTime).toInt()
                R.id.first_time_sort -> (o1.firstTime - o2.firstTime).toInt()
                R.id.size_sort -> (o1.size - o2.size).toInt()
                R.id.name_sort -> o1.name[0] - o2.name[0]
                else -> 0
            }
        })

        updateData(mAppList, adapter)
        recycler.snackBar(item?.title.toString())
        return super.onOptionsItemSelected(item)
    }

    private fun updateData(sortedWith: List<AppItem>, adapter: RecyclerAdapter) {
        mList.clear()
        mList.add(b)
        mList.addAll(sortedWith)
        adapter.setData(mList)
        adapter.notifyDataSetChanged()
        supportActionBar?.title = "应用：${mList.size - 1
        }"
    }

    private fun getAppList(list: MutableList<AppItem>) =
            packageManager.run {
                //狂立flag
                getInstalledPackages(GET_ACTIVITIES
                        or GET_SIGNATURES
                        or GET_SERVICES
                        or GET_PROVIDERS
                        or GET_INTENT_FILTERS
                        or GET_RECEIVERS
                        or GET_URI_PERMISSION_PATTERNS
                        or GET_PERMISSIONS)
                        //使用fold 因为可以遍历同时返回其他类型
                        .fold(list) {
                            cl, p ->
                            val b = p.applicationInfo
                            cl.add(AppItem(
                                    getApplicationLabel(b).toString(),
                                    //allInfo(p, b),
                                    b.loadIcon(this),
                                    b.flags and FLAG_SYSTEM != 0,
                                    p.firstInstallTime,
                                    p.lastUpdateTime,
                                    p.versionCode,
                                    p.versionName ?: "",
                                    getLaunchIntentForPackage(p.packageName),
                                    File(b.publicSourceDir).length(),
                                    allInfo(p, b)
                            )
                            )
                            cl
                        }

            }
}

val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

fun allInfo(p: PackageInfo, b: ApplicationInfo) =
        "firstInstallTime : ${p.firstInstallTime}\n ${dateFormat.format(Date(p.firstInstallTime))} \n" +
                "lastUpdateTime : ${p.lastUpdateTime}\n ${dateFormat.format(Date(p.lastUpdateTime))} \n" +
                "activities : ${p.activities
                        ?.fold(StringBuilder()) {
                            str, item ->
                            str.append(item.name).append("\n")
                        }}\n" +
                "requestedPermission : ${p.requestedPermissions
                        ?.fold(StringBuffer()) {
                            str, item ->
                            str.append(item).append("\n")
                        }
                }\n" +
                "permission : ${p.permissions
                        ?.fold(StringBuffer()) {
                            str, item ->
                            str.append(item.name).append("\n")
                        }
                }\n" +
                "services : ${p.services
                        ?.fold(StringBuffer()) {
                            str, item ->
                            str.append(item.name).append("\n")
                        }
                }\n" +
                "receivers : ${p.receivers
                        ?.fold(StringBuffer()) {
                            str, item ->
                            str.append(item.name).append("\n")
                        }
                }\n" +
                "providers : ${p.providers
                        ?.fold(StringBuffer()) {
                            str, item ->
                            str.append(item.name).append("\n")
                        }
                }\n" +
                "signatures : ${p.signatures
                        ?.fold(StringBuffer()) {
                            str, item ->
                            str.append(item.toCharsString()).append("\n")
                        }
                }\n" +
                "versionName : ${p.versionName}\n" +
                "versionCode : ${p.versionCode}\n" +
                "packageName : ${p.packageName}\n" +
                "splitNames : ${p.splitNames}\n" +
                "sharedUserLabel : ${p.sharedUserLabel}\n" +
                "name : ${b.name}\n" +
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
                ""

