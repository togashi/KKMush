package jp.togashi.android.kkmush

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDialogFragment
import android.text.ClipboardManager
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import java.util.regex.Pattern

class KKMushActivity : AppCompatActivity() {

    companion object {
        private val ACTION_INTERCEPT = "com.adamrocker.android.simeji.ACTION_INTERCEPT"
        private val REPLACE_KEY = "replace_key"

        private val PREF_KEY_LEVEL = "pref_key_level"

        private val mGobi = Pattern.compile("([^a-zA-Z0-9 　。ッ]+)([ 　。!！\\?？])")
        private val mBunmatsu = Pattern.compile(".*[^a-z0-9 　。ッ]$")

        private val DLG_ABOUT = 0
    }

    private var mInputStr: String? = null
    private var mOutputStr: String? = null

    private fun convert(src: String, level: Int): String {
        var outStr = src

        if (level >= 1) {
            outStr = outStr.replace("ん", "ン")
        }

        if (level >= 2) {
            outStr = outStr.replace("っ", "ッ")
            outStr = outStr.replace("ゃあ", "ゃア")
        }

        if (level >= 3) {
            val m = mGobi.matcher(outStr)
            outStr = m.replaceAll("$1ッ$2")
        }

        if (level >= 4) {
            val m = mBunmatsu.matcher(outStr)
            if (m.matches()) {
                outStr += "ッ"
            }
        }

        return outStr
    }

    private val level: Int
        get() {
            return (findViewById(R.id.seekBar1) as SeekBar?)?.let { it.progress + 1 } ?: 1
        }

    private fun update() {
        val lv = level
        (findViewById(R.id.before_text) as TextView?)?.text = mInputStr
        (findViewById(R.id.koike_level) as TextView?)?.text = getString(R.string.koikelevel, lv)
        (findViewById(R.id.after_text) as TextView?)?.text = convert(mInputStr ?: "", lv)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.action == ACTION_INTERCEPT) {
            mInputStr = intent.getStringExtra(REPLACE_KEY)
        }

        if (mInputStr.isNullOrEmpty()) {
            val cbm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            mInputStr = cbm.text?.toString()
        }

        if (mInputStr.isNullOrEmpty()) {
            finish()
            return
        }

        setContentView(R.layout.main)

        (findViewById(R.id.seekBar1) as SeekBar?)?.setOnSeekBarChangeListener(seekBarChangeListener)

        (findViewById(R.id.okbutton) as Button?)?.setOnClickListener {
            mOutputStr = convert(mInputStr ?: "", level)
            finish()
        }
    }

    override fun finish() {
        if (!TextUtils.isEmpty(mOutputStr)) {
            val output = Intent()
            output.putExtra(REPLACE_KEY, mOutputStr)
            setResult(Activity.RESULT_OK, output)
            val cbm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cbm.text = mOutputStr
        }
        super.finish()
    }

    public override fun onResume() {
        super.onResume()
        val sb = findViewById(R.id.seekBar1) as SeekBar
        if (sb != null) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            val savedLevel = prefs.getInt(PREF_KEY_LEVEL, 1)
            sb.progress = savedLevel
        }
        update()
    }

    public override fun onPause() {
        super.onPause()
        val currentLevel = (findViewById(R.id.seekBar1) as SeekBar?)?.progress ?: 1
        PreferenceManager.getDefaultSharedPreferences(applicationContext).apply {
            if (currentLevel != getInt(PREF_KEY_LEVEL, 1)) {
                edit().putInt(PREF_KEY_LEVEL, currentLevel).apply()
            }
        }
    }

    private val seekBarChangeListener = object: OnSeekBarChangeListener {
        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            update()
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {
        }

        override fun onStopTrackingTouch(p0: SeekBar?) {
        }
    }

    class AboutDialogFragment: AppCompatDialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return AlertDialog.Builder(context).apply {
                setTitle(R.string.menu_about)
                setIcon(R.drawable.kkmush_icon_launcher)
                setView(View.inflate(context, R.layout.about, null).apply {
                    (findViewById(R.id.versionTextView) as TextView?)?.text = BuildConfig.VERSION_NAME
                })
                setPositiveButton(R.string.button_dismiss) { dialog, which -> dialog.dismiss() }
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
