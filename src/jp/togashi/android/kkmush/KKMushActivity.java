package jp.togashi.android.kkmush;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class KKMushActivity extends Activity {
    private static final String ACTION_INTERCEPT = "com.adamrocker.android.simeji.ACTION_INTERCEPT";
    private static final String REPLACE_KEY = "replace_key";
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent input = getIntent();
        String action = input.getAction();
        if (action != null && ACTION_INTERCEPT.equals(action)) {
            String outStr = input.getStringExtra(REPLACE_KEY);
            if (outStr != null) {
                outStr = outStr.replace("ん", "ン");
                outStr = outStr.replace("っ", "ッ");
                Intent output = new Intent();
                output.putExtra(REPLACE_KEY, outStr);
                setResult(RESULT_OK, output);
            }
        }
        finish();
    }
}
