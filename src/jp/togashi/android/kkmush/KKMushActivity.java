package jp.togashi.android.kkmush;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class KKMushActivity extends Activity implements OnSeekBarChangeListener {
    private static final String ACTION_INTERCEPT = "com.adamrocker.android.simeji.ACTION_INTERCEPT";
    private static final String REPLACE_KEY = "replace_key";
    
    private static final Pattern mGobi = Pattern.compile("([^a-zA-Z0-9 　。ッ]+)[ 　。!！\\?？]");
    private static final Pattern mBunmatsu = Pattern.compile(".*[^a-z0-9 　。ッ]$");
    
    private String mInputStr = "";
    private String mOutputStr = "";
    
    private static final int PROC_MODE_MUSHROOM = 0;
    private static final int PROC_MODE_CLIPBOARD = 1;
    
    private int mMode = PROC_MODE_MUSHROOM;
    
    private String convert(final String src, final int level) {
        String outStr = new String(src);
        
        if (level >= 1) {
            outStr = outStr.replace("ん", "ン");
        }
        
        if (level >= 2) {
            outStr = outStr.replace("っ", "ッ");
        }
        
        if (level >= 3) {
            Matcher m = mGobi.matcher(outStr);
            while (m.find()) {
                StringBuilder sb = new StringBuilder();
                sb.append(outStr.substring(0, m.end(1))).append("ッ").append(outStr.substring(m.end(1)));
                outStr = sb.toString();
                m = mGobi.matcher(outStr);
            }
        }
        
        if (level >= 4) {
            Matcher m = mBunmatsu.matcher(outStr);
            if (m.matches()) {
                outStr += "ッ";
            }
        }
        
        return outStr;
    }
    
    private void update() {
        int lv = 1;
        SeekBar sb = (SeekBar)findViewById(R.id.seekBar1);
        if (sb != null) {
            lv = sb.getProgress() + 1;
        }
        
        TextView tv = (TextView)findViewById(R.id.beforetext);
        if (tv != null) {
            tv.setText(mInputStr);
        }
        
        mOutputStr = convert(mInputStr, lv);
        
        tv = (TextView)findViewById(R.id.aftertext);
        if (tv != null) {
            tv.setText(mOutputStr);
        }
    }
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent input = getIntent();
        String action = input.getAction();
        if (action != null && ACTION_INTERCEPT.equals(action)) {
            mInputStr = input.getStringExtra(REPLACE_KEY);
            mMode = PROC_MODE_MUSHROOM;
        }
        
        if (TextUtils.isEmpty(mInputStr)) {
            ClipboardManager cbm = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
            CharSequence cs = cbm.getText();
            if (cs != null) {
                mInputStr = cs.toString();
                mMode = PROC_MODE_CLIPBOARD;
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
        update();
        
        Button btn = (Button)findViewById(R.id.okbutton);
        if (btn != null) {
            btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
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
}
