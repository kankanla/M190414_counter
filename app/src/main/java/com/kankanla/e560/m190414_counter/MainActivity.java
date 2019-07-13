package com.kankanla.e560.m190414_counter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class MainActivity extends AppCompatActivity {
    private final String T = "### MainActivity---";
    private final String T2 = this.getClass().getName();
    private MySurfaceView mySurfaceView;
    private ViewGroup viewGroup;
    private ActionBar ab;
    public static boolean isonPause;
    private RelativeLayout.LayoutParams layoutParams;
    private SharedPreferences sharedPreferencesAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // // Log.i(T, "--onCreate--");
        setContentView(R.layout.activity_main);

        ab = getSupportActionBar();
        ab.hide();
        sharedPreferencesAll = getSharedPreferences("allcont", Context.MODE_PRIVATE);
        mySurfaceView = new MySurfaceView(this, new MySurfaceView.CallBack() {
            @Override
            public void showadmob() {
                // Log.i(T, "--showadmob--");
                viewGroup.bringToFront();
            }

            @Override
            public void hideadmob() {
                // Log.i(T, "--hideadmob--");
                mySurfaceView.bringToFront();
            }

            @Override
            public void onSurfaceCreated() {

            }
        });

        layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        this.addContentView(mySurfaceView, layoutParams);
        viewGroup = findViewById(R.id.activity_main);
        Google_admob();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Log.i(T, "onCreateOptionsMenu");
        MenuItem m = menu.add("設定");
        m.setIcon(R.mipmap.ic_launcher);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Log.i(T, "onResume");
        isonPause = false;

    }

    @Override
    protected void onPause() {
        super.onPause();
        isonPause = true;
        // Log.i(T, "onPause");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // Log.i(T, "onRestart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Log.i(T, "onDestroy");
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        mySurfaceView.onKeyUp(keyCode, event);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return true;
    }

    /**
     * Google Adsense 表示
     */
    protected void Google_admob() {
        // Log.i(T, "--Google_admob--");
        MobileAds.initialize(this, getString(R.string.AdMob_ID));

        ViewGroup viewGroup = findViewById(R.id.activity_main);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        adView.setLayoutParams(layoutParams);
        adView.setAdUnitId(getString(R.string.setAdUnitId));

        AdRequest.Builder builder = new AdRequest.Builder();
//        builder.addTestDevice("78957C13BCC9AA1AC5D8462F2DEC083A");
//        builder.addTestDevice("C9517774AEB25C5D4B40D8175F152E03");


        AdRequest adRequest = builder.build();
        viewGroup.addView(adView);
        adView.loadAd(adRequest);

        /*広告リスナー*/
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                // Log.i(T, "onAdLoaded");
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                // Log.i(T, "onAdOpened");
                mySurfaceView.bringToFront();
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
                // Log.i(T, "onAdLeftApplication");
                SharedPreferences.Editor editor = sharedPreferencesAll.edit();
                editor.putInt("allcont", 0);
                editor.apply();
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                // Log.i(T, "onAdClosed");
            }
        });
    }

}