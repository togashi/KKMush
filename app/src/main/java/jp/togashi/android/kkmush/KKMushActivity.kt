package jp.togashi.android.kkmush

import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.preference.PreferenceManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import jp.togashi.android.kkmush.databinding.MainBinding
import timber.log.Timber

class KKMushActivity : AppCompatActivity() {

    companion object {
        private const val ACTION_INTERCEPT = "com.adamrocker.android.simeji.ACTION_INTERCEPT"
        private const val REPLACE_KEY = "replace_key"

        private const val PREF_KEY_LEVEL = "pref_key_level"
    }

    init {
        Timber.plant(
                if (BuildConfig.DEBUG) Timber.DebugTree()
                else object: Timber.Tree() {
                    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                        val priorityLabel = when (priority) {
                            Log.VERBOSE, Log.DEBUG -> return
                            Log.INFO -> "I"
                            Log.WARN -> "W"
                            Log.ERROR -> "E"
                            else -> "I"
                        }
                        FirebaseCrashlytics.getInstance().apply {
                            log("$priorityLabel/${tag ?: "NONE"}: $message")
                            t?.let {
                                recordException(it)
                            }
                        }
                    }
                }
        )
    }

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) return

        if (viewModel.source.isNullOrEmpty()) {
            val cbm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            viewModel.source = cbm.primaryClip?.getItemAt(0)?.text.toString()
        }
    }

    private lateinit var binding: MainBinding
    private lateinit var viewModel: KKMushViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAnalytics = Firebase.analytics

        viewModel = KKMushViewModel(this)

        if (intent.action == ACTION_INTERCEPT) {
            viewModel.source = intent.getStringExtra(REPLACE_KEY)
        }

        if (viewModel.source.isNullOrEmpty()) {
            val cbm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            viewModel.source = cbm.primaryClip?.getItemAt(0)?.text?.toString()
        }

        setContentView(MainBinding.inflate(LayoutInflater.from(this)).also {
            it.viewModel = viewModel
            it.lifecycleOwner = this
            binding = it
        }.root)

        binding.buttonOk.setOnClickListener {
            viewModel.lastResult?.let {
                firebaseAnalytics.logEvent("convert") {
                    it.counts.keys.forEach { k ->
                        param(k, it.counts[k]?.toLong() ?: 0)
                    }
                }
            }
            finish()
        }
    }

    override fun finish() {
        if (!TextUtils.isEmpty(viewModel.converted)) {
            val output = Intent()
            output.putExtra(REPLACE_KEY, viewModel.converted)
            setResult(Activity.RESULT_OK, output)
            val cbm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cbm.setPrimaryClip(ClipData.newPlainText(getString(R.string.after_text), viewModel.converted))
        }
        super.finish()
    }

    public override fun onResume() {
        super.onResume()
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val savedLevel = prefs.getInt(PREF_KEY_LEVEL, 1)
        viewModel.koikeLevel = savedLevel
    }

    public override fun onPause() {
        super.onPause()
        PreferenceManager.getDefaultSharedPreferences(applicationContext).apply {
            edit().putInt(PREF_KEY_LEVEL, viewModel.koikeLevel).apply()
        }
    }

//    private val seekBarChangeListener = object: OnSeekBarChangeListener {
//        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
//            update()
//        }
//
//        override fun onStartTrackingTouch(p0: SeekBar?) {
//        }
//
//        override fun onStopTrackingTouch(p0: SeekBar?) {
//        }
//    }
//
    class AboutDialogFragment: AppCompatDialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return AlertDialog.Builder(requireContext()).apply {
                setTitle(R.string.menu_about)
                setIcon(R.mipmap.ic_launcher_round)
                setView(View.inflate(context, R.layout.about, null).apply {
                    (findViewById<TextView>(R.id.versionTextView))?.text = BuildConfig.VERSION_NAME
                })
                setPositiveButton(R.string.button_dismiss) { dialog, _ -> dialog.dismiss() }
            }.create()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_about -> {
                AboutDialogFragment().show(supportFragmentManager, "TAG_DIALOG")
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
