package san.santools.apps

import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.ApplicationInfo.FLAG_SYSTEM
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.*
import android.net.Uri
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_apps.*
import kotlinx.android.synthetic.main.item_app.view.*
import kotlinx.android.synthetic.main.item_switch.view.*
import san.santools.R
import san.santools.RecyclerAdapter
import san.santools.snackBar
import java.io.File
import java.text.Collator
import java.util.*
import kotlin.concurrent.thread


class AppsActivity : android.support.v7.app.AppCompatActivity() {

    private val mList = mutableListOf<Any>()
    private val mAppList = mutableListOf<AppItem>()
    private val b: String = "应用"
    private var mIsAll = true


    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_PACKAGE_REMOVED -> {
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
                Intent.ACTION_PACKAGE_ADDED -> {
                    recycler.snackBar("安装${intent.dataString ?: "empty"}")
                    val info = packageManager.getPackageInfo(intent.dataString.substring(8), mFlag)
                    info.run {
                        mAppList.add(0, createAppItem(info))
                        updateData(mAppList)
                    }
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)
        setSupportActionBar(toolbar)
        recycler.run {
            addItemDecoration(DividerItemDecoration(this@AppsActivity, DividerItemDecoration.VERTICAL))
            layoutManager = LinearLayoutManager(this@AppsActivity)
            adapter = RecyclerAdapter().apply {

                register(AppItem::class.java, R.layout.item_app) { holder, item ->
                    holder.itemView.run {
                        uninstall.setOnClickListener {
                            val uri = Uri.parse("package:" + item.packageName)
                            val intent = Intent(Intent.ACTION_DELETE, uri)
                            this.context.startActivity(intent)
                        }
                        info.setOnClickListener {
                            val intent = Intent("android.settings.APPLICATION_DETAILS_SETTINGS")
                            intent.data = Uri.parse("package:" + item.packageName)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
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

                register(String::class.java, R.layout.item_switch) { holder, _ ->
                    //FIXME 此处在onBindViewHolder(),不合理的监听器设置写法
                    holder.itemView
                            .app_switch
                            .apply {
                                isChecked.takeIf {
                                    it != mIsAll
                                }?.run {
                                    setOnCheckedChangeListener(null)
                                    isChecked = mIsAll
                                }
                            }
                            .setOnCheckedChangeListener { _, isChecked ->
                                mIsAll = isChecked
                                if (isChecked) {
                                    updateData(mAppList)
                                } else {
                                    mAppList.filter { !it.isSystemApp }
                                            .run {
                                                updateData(this)
                                                mIsAll = false
                                            }
                                }
                            }
                }

                val show = ProgressDialog.show(this@AppsActivity, "", "")
                thread {
                    getAppList(mAppList)
                    runOnUiThread {
                        show.cancel()
                        updateData(mAppList, this)
                    }
                }

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_act_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        mAppList.sortWith(java.util.Comparator { o1, o2 ->
            when (item?.itemId) {
            //TODO 瞎写的规则
                R.id.update_time_sort -> o2.lastTime.compareTo(o1.lastTime)
                R.id.first_time_sort -> o2.firstTime.compareTo(o1.firstTime)
                R.id.size_sort -> o2.size.compareTo(o1.size)
                R.id.name_sort -> Collator.getInstance(Locale.CHINA).compare(o1.name, o2.name)
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
        supportActionBar?.title = "应用：${mList.size - 1}"
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


    private val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    private fun allInfo(p: PackageInfo, b: ApplicationInfo) =
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
                            ?.fold(StringBuilder()) { str, item ->
                                str.append(item.name).append("\n")
                            }}\n" +
                    "requestedPermission : ${p.requestedPermissions
                            ?.fold(StringBuffer()) { str, item ->
                                str.append(item).append("\n")
                            }
                    }\n" +
                    "permission : ${p.permissions
                            ?.fold(StringBuffer()) { str, item ->
                                str.append(item.name).append("\n")
                            }
                    }\n" +
                    "services : ${p.services
                            ?.fold(StringBuffer()) { str, item ->
                                str.append(item.name).append("\n")
                            }
                    }\n" +
                    "receivers : ${p.receivers
                            ?.fold(StringBuffer()) { str, item ->
                                str.append(item.name).append("\n")
                            }
                    }\n" +
                    "providers : ${p.providers
                            ?.fold(StringBuffer()) { str, item ->
                                str.append(item.name).append("\n")
                            }
                    }\n" +
                    "signatures : ${p.signatures
                            ?.fold(StringBuffer()) { str, item ->
                                str.append(item.toCharsString()).append("\n")
                            }
                    }\n" +
                    ""

}
