package com.example.appwidget

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.ArrayMap
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.view.children
import com.example.appwidget.databinding.ActivityDownloaderBinding
import java.util.UUID

class DownloaderActivity : ComponentActivity() {
    private lateinit var binding: ActivityDownloaderBinding

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val B = ActivityDownloaderBinding.inflate(layoutInflater)
        setContentView(B.root)

        binding = B

        if (intent != null) {

            setForm(intent)

        }

        B.downloadBtn.setOnClickListener {

            val filename = B.filenameEdittext.text.toString()
            val url = B.urlTextview.text.toString()
            val headers = ArrayMap<String, String>()
            B.headersEdittext.text.toString().lines().forEach {
                val cs = it.split(':')
                if (cs.size == 2) {
                    val k = cs[0].trim()
                    val v = cs[1].trim()
                    if (k.isNotEmpty() && v.isNotEmpty()) {
                        headers[k] = v
                    }
                }
            }
            val referer = B.refererEdittext.text.toString()
            if (referer.isNotBlank()) {
                headers["Referer"] = referer.trim()
            }
            if (B.browserUaCheckbox.isChecked) {
                headers["User-Agent"] =
                    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.2704.103 Safari/537.36"
            }

            Downloader.downloadFile(this, url, filename, null, headers)
        }
    }

    private fun setForm(intent: Intent) {

        if (intent.action != "android.appwidget.action.DOWNLOAD_FILE") return

        val uri = intent.data?.toString() ?: intent.extras?.getString("url") ?: return

        val filename = intent.extras?.getString("filename") ?: ""

        val headers = ArrayMap<String, String>()

        intent.extras?.getStringArray("headers")?.forEach {
            val cs = it.split(':', limit = 2)
            if (cs.size == 2) {
                val k = cs[0].trim()
                val v = cs[1].trim()
                if (k.isNotEmpty() && v.isNotEmpty()) {
                    headers[k] = v
                }
            }
        }

        intent.extras?.getString("referer")?.let {
            headers["Referer"] = it.trim()
        }

        if (intent.extras?.getBoolean("useua") == true) {
            headers["User-Agent"] =
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.2704.103 Safari/537.36"
        }

        Downloader.downloadFile(this, uri, filename, null, headers)

        finish()

    }

    private fun resetForm() {
        binding.container.children.forEach {
            if (it is EditText) {
                it.text.clear()
            }
        }
        binding.browserUaCheckbox.isChecked = false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menu?.add("Reset")?.apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            setOnMenuItemClickListener {

                resetForm()
                true
            }
        }
        return super.onCreateOptionsMenu(menu)
    }
}


object Downloader {
    fun downloadFile(
        context: Context,
        uri: String, filename: String, mimetype: String?, headers: Map<String, String>?
    ) {
        try {

            var fname = filename
            val u = Uri.parse(uri)
            if (filename.isBlank()) {
                u.pathSegments.lastOrNull()?.let {
                    fname = it
                }
            }
            if (fname.isBlank()) {
                fname = UUID.randomUUID().toString()
            }

            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

            val request = DownloadManager.Request(u)

            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fname)
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            headers?.forEach { (t, u) -> request.addRequestHeader(t, u) }
            mimetype?.let { request.setMimeType(mimetype) }

            downloadManager.enqueue(request)

            Toast.makeText(context, "Downloading file:\n$uri", Toast.LENGTH_SHORT)
                .show()
        } catch (e: Exception) {
            e.message?.let { Log.e("error", it) }
        }
    }

}