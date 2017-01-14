package activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.domogik.domodroid13.R;

/**
 * Created by tiki on 24/12/2016.
 */

public class webview_domogik_admin extends Activity {

    private WebView webView;
    private SharedPreferences SP_params;
    private WebView myWebView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SP_params = PreferenceManager.getDefaultSharedPreferences(this);

        setContentView(R.layout.domogik_admin_webview);
        myWebView = (WebView) findViewById(R.id.webview);

        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        myWebView.setWebViewClient(new WebViewClient());
        if (SP_params.getBoolean("SSL", false)) {
            myWebView.loadUrl("https://" + SP_params.getString("rinorIP", "") + ":" + SP_params.getString("rinorPort", ""));
        } else {
            myWebView.loadUrl("http://" + SP_params.getString("rinorIP", "") + ":" + SP_params.getString("rinorPort", ""));
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && myWebView.canGoBack()) {
            myWebView.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }
}