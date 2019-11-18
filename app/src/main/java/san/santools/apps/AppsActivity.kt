package san.santools.apps

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.ApplicationInfo.FLAG_SYSTEM
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.GET_ACTIVITIES
import android.content.pm.PackageManager.GET_CONFIGURATIONS
import android.content.pm.PackageManager.GET_DISABLED_COMPONENTS
import android.content.pm.PackageManager.GET_DISABLED_UNTIL_USED_COMPONENTS
import android.content.pm.PackageManager.GET_GIDS
import android.content.pm.PackageManager.GET_INSTRUMENTATION
import android.content.pm.PackageManager.GET_INTENT_FILTERS
import android.content.pm.PackageManager.GET_META_DATA
import android.content.pm.PackageManager.GET_PERMISSIONS
import android.content.pm.PackageManager.GET_PROVIDERS
import android.content.pm.PackageManager.GET_RECEIVERS
import android.content.pm.PackageManager.GET_SERVICES
import android.content.pm.PackageManager.GET_SHARED_LIBRARY_FILES
import android.content.pm.PackageManager.GET_SIGNATURES
import android.content.pm.PackageManager.GET_UNINSTALLED_PACKAGES
import android.content.pm.PackageManager.GET_URI_PERMISSION_PATTERNS
import android.content.pm.PackageManager.MATCH_APEX
import android.net.Uri
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_apps.*
import kotlinx.android.synthetic.main.item_app.view.*
import kotlinx.android.synthetic.main.item_switch.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import san.santools.R
import san.santools.RecyclerAdapter
import san.santools.RecyclerViewHolder
import san.santools.ViewBinder
import san.santools.utils.AdapterList
import san.santools.utils.ObservableList
import san.santools.utils.ObservableList.OnListChangedCallback
import san.santools.utils.removeFirst
import san.santools.utils.snackBar
import java.io.File
import java.text.Collator
import java.text.SimpleDateFormat
import java.util.Comparator
import java.util.Date
import java.util.Locale

interface AppSortComparator : Comparator<AppItem> {
    var invert: Boolean

    override fun compare(o1: AppItem, o2: AppItem): Int {
        var a = o1
        var b = o2
        if (invert) {
            a = b.also { b = a }
        }
        return compareField(a, b)
    }

    fun compareField(o1: AppItem, o2: AppItem): Int
}

class UpdateTimeSort(override var invert: Boolean = false) : AppSortComparator {

    override fun compareField(o1: AppItem, o2: AppItem) = o2.lastTime.compareTo(o1.lastTime)
}

class FirstTimeSort(override var invert: Boolean = false) : AppSortComparator {
    override fun compareField(o1: AppItem, o2: AppItem) = o2.firstTime.compareTo(o1.firstTime)
}

class SizeSort(override var invert: Boolean = false) : AppSortComparator {
    override fun compareField(o1: AppItem, o2: AppItem) = o2.size.compareTo(o1.size)
}

class NameSort(override var invert: Boolean = false) : AppSortComparator {
    override fun compareField(o1: AppItem, o2: AppItem) =
        Collator.getInstance(Locale.CHINA).compare(o1.name, o2.name)
}

class AppsActivity : AppCompatActivity() {

    //狂立flag 获取应用信息
    val mFlag =
        (GET_ACTIVITIES
            or GET_CONFIGURATIONS
            or GET_GIDS
            or GET_INSTRUMENTATION
            or GET_INTENT_FILTERS
            or GET_META_DATA
            or GET_PERMISSIONS
            or GET_PROVIDERS
            or GET_RECEIVERS
            or GET_SERVICES
            or GET_SHARED_LIBRARY_FILES
            or GET_SIGNATURES
            // or GET_SIGNING_CERTIFICATES
            or GET_URI_PERMISSION_PATTERNS
            // or MATCH_UNINSTALLED_PACKAGES
            // or MATCH_DISABLED_COMPONENTS
            // or MATCH_DISABLED_UNTIL_USED_COMPONENTS
            // or MATCH_SYSTEM_ONLY
            // or MATCH_FACTORY_ONLY
            // or MATCH_DEBUG_TRIAGED_MISSING
            // or MATCH_INSTANT
            or MATCH_APEX
            or GET_DISABLED_COMPONENTS
            or GET_DISABLED_UNTIL_USED_COMPONENTS
            or GET_UNINSTALLED_PACKAGES
            //or MATCH_HIDDEN_UNTIL_INSTALLED_COMPONENTS
            )

    private val mAppList = AdapterList<AppItem>()
    private val mOriginList = ArrayList<AppItem>()
    private var mShowAll = true
    private var comparator: AppSortComparator = UpdateTimeSort()
    /**
     * 广播
     */
    private val mReceiver: BroadcastReceiver = AppBroadcastReceiver()

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)
        initView()
        //
        registerReceiver(mReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addDataScheme("package")
        })

        updateAppList()
    }

    private fun initView() {
        setSupportActionBar(toolbar)

        mAppList.addOnListChangedCallback(object :
            OnListChangedCallback<ObservableList<AppItem>>() {
            override fun onItemRangeInserted(
                sender: ObservableList<AppItem>,
                positionStart: Int,
                itemCount: Int
            ) {
                supportActionBar?.title = "应用：${sender.size}"
            }

            override fun onItemRangeMoved(
                sender: ObservableList<AppItem>,
                fromPosition: Int,
                toPosition: Int,
                itemCount: Int
            ) {
                supportActionBar?.title = "应用：${sender.size}"
            }

            override fun onItemRangeRemoved(
                sender: ObservableList<AppItem>,
                positionStart: Int,
                itemCount: Int
            ) {
                supportActionBar?.title = "应用：${sender.size}"
            }

            override fun onChanged(sender: ObservableList<AppItem>) {
                supportActionBar?.title = "应用：${sender.size}"
            }
        })

        app_switch.apply {
            setOnCheckedChangeListener { _, isChecked ->
                mShowAll = isChecked
                mAppList.set(applyShowAll(mOriginList))
            }
        }

        recycler.run {
            addItemDecoration(
                DividerItemDecoration(
                    this@AppsActivity,
                    DividerItemDecoration.VERTICAL
                )
            )
            layoutManager = LinearLayoutManager(this@AppsActivity)
            adapter = RecyclerAdapter(mAppList)
                .apply {
                    registBinder(AppItem::class.java, R.layout.item_app, AppViewBinder())
                }
        }
    }

    private fun applyShowAll(
        list: MutableList<AppItem>,
        removeOther: ((AppItem) -> Boolean)? = null
    ): List<AppItem> {
        val a = if (mShowAll) list.run {
            removeOther?.run {
                filter { !removeOther(it) }
            } ?: this
        }
        else list.run {
            filter { !it.isSystemApp && !(removeOther?.invoke(it) ?: false) }
        }
        return a.sortedWith(comparator)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_act_menu, menu)
        menu?.findItem(R.id.search)?.actionView.run {
            this as SearchView
            inputType = EditorInfo.TYPE_CLASS_TEXT
            imeOptions = EditorInfo.IME_ACTION_SEARCH
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    mAppList.set(applyShowAll(mOriginList) {
                        !(it.name.contains(newText) || it.packageName.contains(newText))
                    })
                    return true
                }
            })
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.takeIf { item.itemId != R.id.search }
            ?.run { onItemCall(item.itemId, item.title) }
        return super.onOptionsItemSelected(item)
    }

    private fun onItemCall(temId: Int, title: CharSequence) {
        comparator = when (temId) {
            R.id.update_time_sort -> UpdateTimeSort()
            R.id.first_time_sort -> FirstTimeSort()
            R.id.size_sort -> SizeSort()
            R.id.name_sort -> NameSort()
            else -> comparator
        }

        mAppList.sortWith(comparator)

        runOnUiThread {
            mAppList.changed()
            recycler.snackBar("按${title}排序")
        }
    }

    private suspend fun getApps() = withContext(Dispatchers.IO) {
        packageManager.getInstalledPackages(mFlag)
    }

    private fun updateAppList() =
        GlobalScope.launch(Dispatchers.Main) {
            val apps = getApps()
            apps.forEach { p ->
                val element = createAppItem(p)
                mAppList.add(element)
                mOriginList.add(element)
            }
            onItemCall(R.id.update_time_sort, "更新时间")
        }

    private suspend fun createAppItem(
        p: PackageInfo,
        b: ApplicationInfo = p.applicationInfo,
        m: PackageManager = packageManager
    ) = withContext(Dispatchers.IO) {
        AppItem(
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

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    private fun allInfo(p: PackageInfo, b: ApplicationInfo) =
        "firstInstallTime : ${p.firstInstallTime}\n ${dateFormat.format(Date(p.firstInstallTime))} \n" +
            "lastUpdateTime : ${p.lastUpdateTime}\n ${dateFormat.format(Date(p.lastUpdateTime))} \n" +
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

    override fun onDestroy() {
        unregisterReceiver(mReceiver)
        super.onDestroy()
    }

    inner class AppBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_PACKAGE_REMOVED -> {
                    recycler.snackBar("卸载${intent.dataString ?: "empty"}")
                    mAppList.removeFirst {
                        "package:${it.packageName}" == intent.dataString
                    }
                    mOriginList.removeFirst {
                        "package:${it.packageName}" == intent.dataString
                    }
                }
                Intent.ACTION_PACKAGE_ADDED, Intent.ACTION_PACKAGE_REPLACED -> {
                    recycler.snackBar("安装${intent.dataString ?: "empty"}")
                    val info =
                        packageManager.getPackageInfo(intent.dataString?.substring(8), mFlag)
                    info.run {
                        GlobalScope.launch(Dispatchers.Main) {
                            val element = createAppItem(info)
                            mAppList.indexOfFirst {
                                "package:${it.packageName}" == intent.dataString
                            }.takeIf { it != -1 }
                                ?.run {
                                    mAppList[this] = element
                                    mOriginList[this] = element
                                } ?: run {
                                mAppList.add(0, element)
                                mOriginList.add(0, element)
                            }
                        }
                    }
                }
            }
        }
    }

    inner class AppViewBinder : ViewBinder() {

        @SuppressLint("SetTextI18n")
        override fun onBindView(holder: RecyclerViewHolder) {
            holder.itemView.run {
                val item = holder.getItem<AppItem>()
                app_name.text =
                    "${item.name} \n " +
                        "size:${item.size} b\n " +
                        "version:${item.versionCode} \n " +
                        "version:${item.versionName}"
                app_icon.setImageDrawable(item.icon)
            }
        }

        override fun onCreteView(holder: RecyclerViewHolder) {
            holder.itemView.run {
                uninstall.setOnClickListener {
                    val item = holder.get<AppItem>("Item")
                    val uri = Uri.parse("package:" + item.packageName)
                    val intent = Intent(Intent.ACTION_DELETE, uri)
                    this.context.startActivity(intent)
                }
                info.setOnClickListener {
                    val item = holder.get<AppItem>("Item")
                    val uri = Uri.parse("package:" + item.packageName)
                    val intent =
                        Intent("android.settings.APPLICATION_DETAILS_SETTINGS", uri)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    this.context.startActivity(intent)
                }

                app_layout.setOnClickListener {
                    val item = holder.get<AppItem>("Item")
                    AlertDialog.Builder(this@AppsActivity).setMessage(item.allInfo)
                        .show()
                }
                app_icon.setOnClickListener {
                    val item = holder.get<AppItem>("Item")
                    item.intent
                        ?.run { startActivity(item.intent) }
                        ?: run { recycler.snackBar("启动个p") }
                }
            }
        }

        override fun onDetach(holder: RecyclerViewHolder) {
            holder.itemView.swipe.run {
                close(false)
            }
        }
    }
}

