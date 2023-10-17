package com.oapps.webviewapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText


private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    lateinit var webView: WebView
    lateinit var urlEditText: TextInputEditText
    lateinit var goButton: FloatingActionButton

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WebView.setWebContentsDebuggingEnabled(true)

        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.webView)
        goButton = findViewById(R.id.goBtn)
        urlEditText = findViewById(R.id.urlEditText)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                webView.loadUrl("javascript:(function(){document.body.style.background = 'pink';})()");
                urlEditText.setText(webView.url)
            }
        }
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
//        webView.loadUrl("https://www.google.com")
        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(message: String?, lineNumber: Int, sourceID: String?) {
                Log.d(TAG, "onConsoleMessage: $message")
            }
        }
//        webView.addJavascriptInterface(JavaScriptInterface(this, webView), "JSBridge")
        webView.loadUrl("file:///android_asset/index.html")

        goButton.setOnClickListener {
            webView.loadUrl(urlEditText.text.toString())
        }
    }
}

class JavaScriptInterface(val activity: ComponentActivity, val webView: WebView) {
    var callbackAdded = false
    val callbacks = mutableSetOf<String>()

    @JavascriptInterface
    fun showToast(toast: String?) {
        Toast.makeText(activity, toast, Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun registerBackPressedCallback(callback: String?) {
        callback?.let { callbacks.add(it) }
        if (!callbackAdded) {
            activity.runOnUiThread {
                activity.onBackPressedDispatcher.addCallback(
                    activity,
                    object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            webView.evaluateJavascript(callbacks.joinToString("; "){ "$it()"}) {

                            }
                        }
                    })
            }
            callbackAdded = true
        }
    }

    @JavascriptInterface
    fun unRegisterBackPressedCallback(callback: String?) {
        callbacks.remove(callback)
    }
}