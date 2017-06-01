package san.santools.apps

import android.content.pm.ApplicationInfo
import android.content.pm.ApplicationInfo.FLAG_SYSTEM
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.*
import android.support.v7.widget.DividerItemDecoration
import kotlinx.android.synthetic.main.activity_apps.*
import kotlinx.android.synthetic.main.item_app.view.*
import kotlinx.android.synthetic.main.item_switch.view.*
import san.santools.R
import san.santools.RecyclerAdapter
import san.santools.snackBar
import java.io.File
import java.util.*


/**
 * 简单功能，就不做mvp了
 */
class AppsActivity : android.support.v7.app.AppCompatActivity() {

    val mList = mutableListOf<Any>()
    val mAppList = mutableListOf<AppItem>()
    val b: String = "应用"
    var mIsAll = true


    val mReceiver: android.content.BroadcastReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
            when (intent?.action) {
                android.content.Intent.ACTION_PACKAGE_REMOVED -> {
                    recycler.snackBar("卸载${intent.dataString ?: "empty"}")
                    mAppList.iterator().apply {
                        forEach {
                            if ("package:${it.packageName}" == intent.dataString) {
                                this.remove()
                                updateData(mAppList)
                                return@apply
                            }
                        }
                    }
                }
                android.content.Intent.ACTION_PACKAGE_ADDED -> {
                    recycler.snackBar("安装${intent.dataString ?: "empty"}")
                    val info = packageManager.getPackageInfo(intent.dataString.substring(8), mFlag)
                    info?.run {
                        mAppList.add(0, createAppItem(info))
                        updateData(mAppList)
                    }
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(san.santools.R.layout.activity_apps)
        setSupportActionBar(toolbar)
        getAppList(mAppList)
        recycler.run {
            addItemDecoration(android.support.v7.widget.DividerItemDecoration(this@AppsActivity, DividerItemDecoration.VERTICAL))
            layoutManager = android.support.v7.widget.LinearLayoutManager(this@AppsActivity)
            adapter = san.santools.RecyclerAdapter().apply {
                register(AppItem::class.java, san.santools.R.layout.item_app) {
                    holder, item ->
                    holder.itemView.run {
                        uninstall.setOnClickListener {
                            val uri = android.net.Uri.parse("package:" + item.packageName)
                            val intent = android.content.Intent(android.content.Intent.ACTION_DELETE, uri)
                            this.context.startActivity(intent)
                        }
                        info.setOnClickListener {
                            val intent = android.content.Intent("android.settings.APPLICATION_DETAILS_SETTINGS")
                            intent.data = android.net.Uri.parse("package:" + item.packageName)
                            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            this.context.startActivity(intent)
                        }
                        //FIXME 此处在onBindViewHolder(),不合理的监听器设置写法
                        app_name.text = "${item.name} \n ${item.size} b\n ${item.versionCode} \n ${item.versionName}"
                        app_name.setOnClickListener {
                            android.app.AlertDialog.Builder(this@AppsActivity).setMessage(item.allInfo).show()
                        }
                        app_icon.setImageDrawable(item.icon)
                        app_icon.setOnClickListener {
                            item.intent
                                    ?.run { startActivity(item.intent) }
                                    ?: run { recycler.snackBar("启动个p") }
                        }
                    }
                }
                register(String::class.java, san.santools.R.layout.item_switch) {
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
                                    updateData(mAppList)
                                } else {
                                    mAppList.filter { !it.isSystemApp }
                                            .run {
                                                updateData(this)
                                            }
                                }
                            }
                }
                updateData(mAppList, this)

            }
        }

        registerReceiver(mReceiver, android.content.IntentFilter("android.intent.action.PACKAGE_ADDED").apply {
            addAction(android.content.Intent.ACTION_PACKAGE_ADDED)
            addAction(android.content.Intent.ACTION_PACKAGE_REMOVED)
            addDataScheme("package")
        })

    }

    override fun onDestroy() {
        unregisterReceiver(mReceiver)
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(san.santools.R.menu.app_act_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem?): Boolean {
        mAppList.sortWith(java.util.Comparator { o1, o2 ->
            when (item?.itemId) {
            //TODO 瞎写的规则
                R.id.update_time_sort -> o2.lastTime.compareTo(o1.lastTime)
                R.id.first_time_sort -> o2.firstTime.compareTo(o1.firstTime)
                R.id.size_sort -> o2.size.compareTo(o1.size)
                R.id.name_sort -> o2.name.compareTo(o1.name)
                else -> 0
            }
        })

        updateData(mAppList)
        recycler.snackBar(item?.title.toString())
        return super.onOptionsItemSelected(item)
    }

    private fun updateData(sortedWith: List<AppItem>, adapter: RecyclerAdapter = recycler.adapter as RecyclerAdapter) {
        mList.clear()
        mList.add(b)
        mList.addAll(sortedWith)
        mIsAll = true
        adapter.setData(mList)
        adapter.notifyDataSetChanged()
        supportActionBar?.title = "应用：${mList.size - 1
        }"
    }

    //狂立flag
    val mFlag = (GET_ACTIVITIES
            or GET_SIGNATURES
            or GET_SERVICES
            or GET_PROVIDERS
            or GET_INTENT_FILTERS
            or GET_RECEIVERS
            or GET_URI_PERMISSION_PATTERNS
            or GET_PERMISSIONS)

    private fun getAppList(list: MutableList<AppItem>) =
            packageManager.run {
                getInstalledPackages(mFlag)
                        //使用fold 因为可以遍历同时返回其他类型
                        .fold(list) { cl, p ->
                            cl.add(createAppItem(p))
                            cl
                        }

            }


    private fun createAppItem(p: PackageInfo, b: ApplicationInfo = p.applicationInfo, m: PackageManager = packageManager): AppItem {
        return AppItem(
                m.getApplicationLabel(b).toString(),
                b.loadIcon(m),
                b.flags and FLAG_SYSTEM != 0,
                p.firstInstallTime,
                p.lastUpdateTime,
                p.versionCode,
                p.versionName ?: "",
                p.packageName,
                m.getLaunchIntentForPackage(p.packageName),
                File(b.publicSourceDir).length(),
                allInfo(p, b)
        )
    }


    val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun allInfo(p: PackageInfo, b: ApplicationInfo) =
            "firstInstallTime : ${p.firstInstallTime}\n ${dateFormat.format(java.util.Date(p.firstInstallTime))} \n" +
                    "lastUpdateTime : ${p.lastUpdateTime}\n ${dateFormat.format(java.util.Date(p.lastUpdateTime))} \n" +
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
                    ""

}