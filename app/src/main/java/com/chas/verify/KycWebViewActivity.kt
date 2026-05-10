package com.chas.verify

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.webkit.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class KycWebViewActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_URL = "extra_kyc_url"
        private const val REQ_CAMERA_PERM = 10
        private const val REQ_CAM_SELFIE  = 20
        private const val REQ_GALLERY     = 21
        private const val REQ_VIDEO       = 30
        private const val REQ_FILE_OTHER  = 40
    }

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var statusBar: LinearLayout
    private lateinit var statusText: TextView

    private var pendingCallback: ValueCallback<Array<Uri>>? = null
    private var tempCameraUri: Uri? = null

    private enum class AcceptType { IMAGE, VIDEO, OTHER }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()

        val url = intent.getStringExtra(EXTRA_URL) ?: run {
            Toast.makeText(this, "No URL provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        configureWebView()
        webView.loadUrl(url)
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF0D1117.toInt())
        }

        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(0xFF111318.toInt())
            setPadding(dp(12), dp(12), dp(12), dp(12))
            gravity = Gravity.CENTER_VERTICAL
            elevation = dp(4).toFloat()
        }

        val backBtn = TextView(this).apply {
            text = "←"
            textSize = 22f
            setTextColor(0xFF7C8190.toInt())
            setPadding(dp(4), 0, dp(18), 0)
            isClickable = true
            isFocusable = true
            setOnClickListener { if (webView.canGoBack()) webView.goBack() else finish() }
        }
        topBar.addView(backBtn, LinearLayout.LayoutParams(-2, -2))

        val topTitle = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val topAppName = TextView(this).apply {
            text = "chAs"
            textSize = 16f
            setTextColor(0xFFE8EAF0.toInt())
            typeface = Typeface.DEFAULT_BOLD
        }
        val topSubtitle = TextView(this).apply {
            text = "Identity Verification"
            textSize = 11f
            setTextColor(0xFF7C8190.toInt())
        }
        topTitle.addView(topAppName, LinearLayout.LayoutParams(-2, -2))
        topTitle.addView(topSubtitle, LinearLayout.LayoutParams(-2, -2))
        topBar.addView(topTitle, LinearLayout.LayoutParams(0, -2, 1f))

        val reloadBtn = TextView(this).apply {
            text = "↺"
            textSize = 20f
            setTextColor(0xFF7C8190.toInt())
            isClickable = true
            isFocusable = true
            setOnClickListener { webView.reload() }
        }
        topBar.addView(reloadBtn, LinearLayout.LayoutParams(-2, -2))
        root.addView(topBar, LinearLayout.LayoutParams(-1, -2))

        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            visibility = View.GONE
            scaleY = 0.7f
        }
        root.addView(progressBar, LinearLayout.LayoutParams(-1, dp(4)))

        statusBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(0xFF1A2A4A.toInt())
            setPadding(dp(16), dp(8), dp(16), dp(8))
            gravity = Gravity.CENTER_VERTICAL
            visibility = View.GONE
        }
        val dot = View(this).apply { setBackgroundColor(0xFF4F8EF7.toInt()) }
        statusBar.addView(dot, LinearLayout.LayoutParams(dp(7), dp(7)).also { it.marginEnd = dp(10) })

        statusText = TextView(this).apply {
            textSize = 12f
            setTextColor(0xFF4F8EF7.toInt())
            typeface = Typeface.DEFAULT_BOLD
        }
        statusBar.addView(statusText, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(statusBar, LinearLayout.LayoutParams(-1, -2))

        webView = WebView(this)
        root.addView(webView, LinearLayout.LayoutParams(-1, 0, 1f))

        setContentView(root)
    }

    private fun configureWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            mediaPlaybackRequiresUserGesture = false
            setSupportZoom(false)
            displayZoomControls = false
            builtInZoomControls = false
            useWideViewPort = true
            loadWithOverviewMode = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, fav: android.graphics.Bitmap?) {
                progressBar.visibility = View.VISIBLE
                progressBar.progress = 10
            }
            override fun onPageFinished(view: WebView, url: String) {
                progressBar.progress = 100
                progressBar.visibility = View.GONE
            }
            override fun onReceivedSslError(
                view: WebView,
                handler: android.webkit.SslErrorHandler,
                error: android.net.http.SslError
            ) {
                handler.proceed()
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                progressBar.progress = newProgress
                progressBar.visibility = if (newProgress < 100) View.VISIBLE else View.GONE
            }

            override fun onShowFileChooser(
                view: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                pendingCallback?.onReceiveValue(null)
                pendingCallback = filePathCallback

                val acceptTypes = fileChooserParams.acceptTypes
                    .joinToString(",").lowercase().trim()

                val acceptType = when {
                    acceptTypes.contains("video") -> AcceptType.VIDEO
                    acceptTypes.contains("image") || acceptTypes.isEmpty() -> AcceptType.IMAGE
                    else -> AcceptType.OTHER
                }

                when (acceptType) {
                    AcceptType.IMAGE -> {
                        showStatus("Selfie step — choose your photo source")
                        showSelfieDialog()
                    }
                    AcceptType.VIDEO -> {
                        showStatus("Video step — launching camera")
                        launchVideoCapture()
                    }
                    AcceptType.OTHER -> {
                        showStatus("File selection")
                        launchSystemPicker()
                    }
                }
                return true
            }

            override fun onPermissionRequest(request: PermissionRequest) {
                runOnUiThread { request.grant(request.resources) }
            }
        }

        WebView.setWebContentsDebuggingEnabled(true)
    }

    private fun showSelfieDialog() {
        val dialog = AlertDialog.Builder(this).create()

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
        }

        val title = TextView(this).apply {
            text = "Upload Selfie"
            textSize = 18f
            setTextColor(0xFFE8EAF0.toInt())
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        container.addView(title, rowLp(bottomMargin = dp(6)))

        val subtitle = TextView(this).apply {
            text = "Choose how to provide your photo"
            textSize = 13f
            setTextColor(0xFF7C8190.toInt())
            gravity = Gravity.CENTER
        }
        container.addView(subtitle, rowLp(bottomMargin = dp(24)))

        val galleryBtn = buildOptionCard(
            emoji = "Gallery",
            title = "Upload from Gallery",
            subtitle = "Pick an existing photo from your phone",
            fillColor = 0xFF0D1E3A.toInt(),
            strokeColor = 0xFF2A4A8A.toInt(),
            titleColor = 0xFF4F8EF7.toInt()
        )
        galleryBtn.setOnClickListener { dialog.dismiss(); openGallery() }
        container.addView(galleryBtn, rowLp(bottomMargin = dp(12)))

        val cameraBtn = buildOptionCard(
            emoji = "Camera",
            title = "Take a Selfie",
            subtitle = "Use your front camera right now",
            fillColor = 0xFF1A1D24.toInt(),
            strokeColor = 0xFF252830.toInt(),
            titleColor = 0xFFADB5BD.toInt()
        )
        cameraBtn.setOnClickListener { dialog.dismiss(); openCamera() }
        container.addView(cameraBtn, rowLp(bottomMargin = dp(20)))

        val cancelBtn = TextView(this).apply {
            text = "Cancel"
            textSize = 14f
            setTextColor(0xFF7C8190.toInt())
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
            setOnClickListener {
                dialog.dismiss()
                pendingCallback?.onReceiveValue(null)
                pendingCallback = null
                hideStatus()
            }
        }
        container.addView(cancelBtn, rowLp())

        dialog.setView(container)
        dialog.show()

        dialog.window?.apply {
            val lp = attributes
            lp.width = (resources.displayMetrics.widthPixels * 0.88).toInt()
            attributes = lp
            setBackgroundDrawable(GradientDrawable().apply {
                setColor(0xFF111318.toInt())
                cornerRadius = dp(20).toFloat()
            })
        }
    }

    private fun buildOptionCard(
        emoji: String, title: String, subtitle: String,
        fillColor: Int, strokeColor: Int, titleColor: Int
    ): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            gravity = Gravity.CENTER_VERTICAL
            isClickable = true
            isFocusable = true
            background = GradientDrawable().apply {
                setColor(fillColor)
                cornerRadius = dp(14).toFloat()
                setStroke(dp(1), strokeColor)
            }
        }
        val emojiView = TextView(this).apply {
            text = emoji
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(titleColor)
            setPadding(0, 0, dp(14), 0)
        }
        row.addView(emojiView, LinearLayout.LayoutParams(-2, -2))

        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val titleView = TextView(this).apply {
            text = title
            textSize = 15f
            setTextColor(titleColor)
            typeface = Typeface.DEFAULT_BOLD
        }
        val subView = TextView(this).apply {
            text = subtitle
            textSize = 12f
            setTextColor(0xFF7C8190.toInt())
        }
        col.addView(titleView, LinearLayout.LayoutParams(-2, -2))
        col.addView(subView, LinearLayout.LayoutParams(-2, -2).also { it.topMargin = dp(3) })
        row.addView(col, LinearLayout.LayoutParams(0, -2, 1f))
        return row
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, REQ_GALLERY)
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQ_CAMERA_PERM)
            return
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val photoFile = File.createTempFile(
            "SELFIE_${timestamp}_", ".jpg",
            getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )
        tempCameraUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, tempCameraUri)
            putExtra("android.intent.extras.CAMERA_FACING", 1)
            putExtra("android.intent.extras.LENS_FACING_FRONT", 1)
            putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
        }
        startActivityForResult(intent, REQ_CAM_SELFIE)
    }

    private fun launchVideoCapture() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
            putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30)
        }
        startActivityForResult(intent, REQ_VIDEO)
    }

    private fun launchSystemPicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(Intent.createChooser(intent, "Select File"), REQ_FILE_OTHER)
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        hideStatus()

        if (pendingCallback == null) return

        val uris: Array<Uri>? = when {
            resultCode != Activity.RESULT_OK -> null
            requestCode == REQ_GALLERY -> data?.data?.let { arrayOf(it) }
            requestCode == REQ_CAM_SELFIE -> tempCameraUri?.let { arrayOf(it) }.also { tempCameraUri = null }
            requestCode == REQ_VIDEO -> data?.data?.let { arrayOf(it) }
            requestCode == REQ_FILE_OTHER -> data?.data?.let { arrayOf(it) }
            else -> null
        }

        if (requestCode == REQ_GALLERY && uris != null) {
            Toast.makeText(this, "Photo selected", Toast.LENGTH_SHORT).show()
        }

        pendingCallback?.onReceiveValue(uris)
        pendingCallback = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_CAMERA_PERM) {
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                pendingCallback?.onReceiveValue(null)
                pendingCallback = null
                hideStatus()
            }
        }
    }

    private fun showStatus(msg: String) = runOnUiThread {
        statusText.text = msg
        statusBar.visibility = View.VISIBLE
    }

    private fun hideStatus() = runOnUiThread {
        statusBar.visibility = View.GONE
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun rowLp(bottomMargin: Int = 0) =
        LinearLayout.LayoutParams(-1, -2).also { it.bottomMargin = bottomMargin }

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}
