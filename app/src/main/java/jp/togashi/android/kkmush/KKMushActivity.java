package jp.togashi.android.kkmush;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class KKMushActivity extends Activity implements OnSeekBarChangeListener {
    private static final String ACTION_INTERCEPT = "com.adamrocker.android.simeji.ACTION_INTERCEPT";
    private static final String REPLACE_KEY = "replace_key";
    
    private static final String PREF_KEY_LEVEL = "pref_key_level";
    
    private static final Pattern mGobi = Pattern.compile("([^a-zA-Z0-9 　。ッ]+)([ 　。!！\\?？])");
    private static final Pattern mBunmatsu = Pattern.compile(".*[^a-z0-9 　。ッ]$");
    
    private String mInputStr = "";
    private String mOutputStr;
    
    private String convert(final String src, final int level) {
        String outStr = new String(src);
        
        if (level >= 1) {
            outStr = outStr.replace("ん", "ン");
        }
        
        if (level >= 2) {
            outStr = outStr.replace("っ", "ッ");
            outStr = outStr.replace("ゃあ", "ゃア");
        }
        
        if (level >= 3) {
            Matcher m = mGobi.matcher(outStr);
            outStr = m.replaceAll("$1ッ$2");
        }
        
        if (level >= 4) {
            Matcher m = mBunmatsu.matcher(outStr);
            if (m.matches()) {
                outStr += "ッ";
            }
        }
        
        return outStr;
    }
    
    private int getLevel() {
        int lv = 1;
        SeekBar sb = (SeekBar)findViewById(R.id.seekBar1);
        if (sb != null) {
            lv = sb.getProgress() + 1;
        }
        return lv;
    }
    
    private void update() {
        int lv = getLevel();
        
        TextView tv = (TextView)findViewById(R.id.beforetext);
        if (tv != null) {
            tv.setText(mInputStr);
        }
        
        tv = (TextView)findViewById(R.id.koikelevel);
        if (tv != null) {
            tv.setText(String.format(getResources().getString(R.string.koikelevel), lv));
        }
        
        String after = convert(mInputStr, lv);
        
        tv = (TextView)findViewById(R.id.aftertext);
        if (tv != null) {
            tv.setText(after);
        }
    }
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent input = getIntent();
        String action = input.getAction();
        if (action != null && ACTION_INTERCEPT.equals(action)) {
            mInputStr = input.getStringExtra(REPLACE_KEY);
        }
        
        if (TextUtils.isEmpty(mInputStr)) {
            ClipboardManager cbm = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
            CharSequence cs = cbm.getText();
            if (cs != null) {
                mInputStr = cs.toString();
            }
        }
        
        if (TextUtils.isEmpty(mInputStr)) {
            finish();
            return;
        }
        
        setContentView(R.layout.main);
        
        SeekBar sb = (SeekBar)findViewById(R.id.seekBar1);
        if (sb != null) {
            sb.setOnSeekBarChangeListener(this);
        }
        
        Button btn = (Button)findViewById(R.id.okbutton);
        if (btn != null) {
            btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOutputStr = convert(mInputStr, getLevel());
                    finish();
                }
            });
        }
    }
    
    @Override
    public void finish() {
        if (!TextUtils.isEmpty(mOutputStr)) {
            Intent output = new Intent();
            output.putExtra(REPLACE_KEY, mOutputStr);
            setResult(RESULT_OK, output);
            ClipboardManager cbm = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
            cbm.setText(mOutputStr);
        }
        super.finish();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        SeekBar sb = (SeekBar)findViewById(R.id.seekBar1);
        if (sb != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            int savedLevel = prefs.getInt(PREF_KEY_LEVEL, 1);
            sb.setProgress(savedLevel);
        }
        update();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        SeekBar sb = (SeekBar)findViewById(R.id.seekBar1);
        if (sb != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            int savedLevel = prefs.getInt(PREF_KEY_LEVEL, 1);
            int currentLevel = sb.getProgress();
            if (savedLevel != currentLevel) {
                Editor editor = prefs.edit();
                editor.putInt(PREF_KEY_LEVEL, currentLevel);
                editor.commit();
            }
        }
    }
    
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromUser) {
        update();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    private static final int DLG_ABOUT = 0;
    
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dlg = null;
        
        if (id == DLG_ABOUT) {
            View cv = getLayoutInflater().inflate(R.layout.about, null);
            TextView version = (TextView) cv.findViewById(R.id.versionTextView);
            PackageInfo pi;
            try {
                pi = getPackageManager().getPackageInfo(getPackageName(), 0);
                version.setText(String.format("%s %s", getString(R.string.label_version), pi.versionName));
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
            AlertDialog.Builder bldr = new AlertDialog.Builder(this);
            bldr.setTitle(R.string.menu_about);
            bldr.setIcon(R.drawable.kkmush_icon_launcher);
            bldr.setView(cv);
            bldr.setPositiveButton(R.string.button_dismiss, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dlg = bldr.create();
        }
        
        return dlg;
    }
    
    private boolean showAbout() {
        showDialog(DLG_ABOUT);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_about && showAbout()) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
}
