package com.gDyejeekis.aliencompanion.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.gDyejeekis.aliencompanion.AppConstants;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;

/**
 * Created by sound on 4/11/2016.
 */
public class SplashActivity extends AppCompatActivity {

    public static final String TAG = "SplashActivity";

    public static final int SPLASH_DISPLAY_DURATION = 100;

    @Override
    public void onCreate(Bundle bundle) {
        //getTheme().applyStyle(MyApplication.fontStyle, true);
        getTheme().applyStyle(MyApplication.fontFamily, true);
        super.onCreate(bundle);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            // Activity was brought to front and not created,
            // Thus finishing this will get us to the last viewed activity
            Log.d(TAG, "Killing additional SplashActivity that was brought to front");
            finish();
            return;
        }

        if (AppConstants.SHOW_UPDATE_MESSAGE && !MyApplication.showedWelcomeMessage) {
            setContentView(R.layout.splash_screen_welcome);

            Button button = (Button) findViewById(R.id.button_welcome_done);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MyApplication.showedWelcomeMessage = true;
                    SharedPreferences.Editor editor = MyApplication.prefs.edit();
                    editor.putBoolean("welcomeMsg", true);
                    editor.commit();
                    startMainActivity();
                }
            });
        } else {
            setContentView(R.layout.splash_screen);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startMainActivity();
                }
            }, SPLASH_DISPLAY_DURATION);
        }
    }

    private void startMainActivity() {
        Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
