package com.eminence.sitasrm.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;

import com.eminence.sitasrm.MainActivity;
import com.eminence.sitasrm.R;
import com.eminence.sitasrm.Utils.YourPreference;

import java.util.Locale;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        YourPreference yourPrefrence = YourPreference.getInstance(getApplicationContext());
        String language = yourPrefrence.getData("language");

        if (language != null || !language.equalsIgnoreCase("")) {
            updateResources(SplashScreen.this, language);
        } else {
            updateResources(SplashScreen.this, "en");
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                intentServiceFire();

            }
        }, 2000);

    }

    private static boolean updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        return true;
    }

    private void intentServiceFire() {
        YourPreference yourPrefrence = YourPreference.getInstance(getApplicationContext());
        String id = yourPrefrence.getData("id");
        String language = yourPrefrence.getData("language");

        if (language.equalsIgnoreCase("")) {
            startActivity(new Intent(getApplicationContext(), LanguageActivity.class));
            finish();
        } else if (id.equalsIgnoreCase("") || id.equalsIgnoreCase(null)) {
            startActivity(new Intent(getApplicationContext(), OnBordingScreen.class));
            finish();
        } else {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("goto", "home");
            startActivity(intent);
            finish();
        }
    }


//    request.setRetryPolicy(new RetryPolicy() {
//        @Override
//        public int getCurrentTimeout() {
//            return 50000;
//        }
//
//        @Override
//        public int getCurrentRetryCount() {
//            return 50000;
//        }
//
//        @Override
//        public void retry(VolleyError error) throws VolleyError {
//
//        }
//    });

}