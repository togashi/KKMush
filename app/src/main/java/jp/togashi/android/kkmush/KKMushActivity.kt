package jp.togashi.android.kkmush

import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.preference.PreferenceManager
import java.util.regex.Pattern

class KKMushActivity : AppCompatActivity() {

    companion object {
        private const val ACTION_INTERCEPT = "com.adamrocker.android.simeji.ACTION_INTERCEPT"
        private const val REPLACE_KEY = "replace_key"

        private const val PREF_KEY_LEVEL = "pref_key_level"

        private val mGobi = Pattern.compile("([^a-zA-Z0-9 　。ッ]+)([ 　。!！\\?？])")
        private val mBunmatsu = Pattern.compile(".*[^a-z0-9 　。ッ]$")
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
            return (findViewById<SeekBar>(R.id.seekBar1))?.let { it.progress + 1 } ?: 1
        }

    private fun update() {
        val lv = level
        (findViewById<TextView>(R.id.before_text))?.text = mInputStr
        (findViewById<TextView>(R.id.koike_level))?.text = getString(R.string.koikelevel, lv)
        (findViewById<TextView>(R.id.after_text))?.text = convert(mInputStr ?: "", lv)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.action == ACTION_INTERCEPT) {
            mInputStr = intent.getStringExtra(REPLACE_KEY)
        }

        if (mInputStr.isNullOrEmpty()) {
            val cbm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            mInputStr = cbm.primaryClip?.getItemAt(0)?.text.toString()
        }

        if (mInputStr.isNullOrEmpty()) {
            finish()
            return
        }

        setContentView(R.layout.main)

        (findViewById<SeekBar>(R.id.seekBar1))?.setOnSeekBarChangeListener(seekBarChangeListener)

        (findViewById<Button>(R.id.okbutton))?.setOnClickListener {
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
            cbm.setPrimaryClip(ClipData.newPlainText(getString(R.string.after_text), mOutputStr))
        }
        super.finish()
    }

    public override fun onResume() {
        super.onResume()
        val sb = findViewById<SeekBar>(R.id.seekBar1)
        if (sb != null) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            val savedLevel = prefs.getInt(PREF_KEY_LEVEL, 1)
            sb.progress = savedLevel
        }
        update()
    }

    public override fun onPause() {
        super.onPause()
        val currentLevel = (findViewById<SeekBar>(R.id.seekBar1))?.progress ?: 1
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
            return AlertDialog.Builder(context!!).apply {
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
