package com.chas.verify

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this).apply {
            setBackgroundColor(0xFF0D1117.toInt())
            isFillViewport = true
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(64), dp(24), dp(40))
        }
        scroll.addView(root, LinearLayout.LayoutParams(-1, -1))

        // ── Icon ──────────────────────────────────────────────────────────
        val icon = TextView(this).apply {
            text = "🔐"
            textSize = 56f
            gravity = Gravity.CENTER
        }
        root.addView(icon, rowLp(gravity = Gravity.CENTER_HORIZONTAL, bottomMargin = dp(18)))

        // ── App name ──────────────────────────────────────────────────────
        val appName = TextView(this).apply {
            text = "chAs"
            textSize = 36f
            setTextColor(0xFFE8EAF0.toInt())
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            letterSpacing = 0.05f
        }
        root.addView(appName, rowLp(gravity = Gravity.CENTER_HORIZONTAL, bottomMargin = dp(6)))

        val tagline = TextView(this).apply {
            text = "Identity Verification Wrapper"
            textSize = 13f
            setTextColor(0xFF4F8EF7.toInt())
            gravity = Gravity.CENTER
            letterSpacing = 0.04f
        }
        root.addView(tagline, rowLp(gravity = Gravity.CENTER_HORIZONTAL, bottomMargin = dp(10)))

        val divider = View(this).apply {
            setBackgroundColor(0xFF252830.toInt())
        }
        root.addView(divider, LinearLayout.LayoutParams(-1, dp(1)).also {
            it.topMargin = dp(14); it.bottomMargin = dp(28)
        })

        // ── Label ─────────────────────────────────────────────────────────
        val label = TextView(this).apply {
            text = "VERIFICATION LINK"
            textSize = 11f
            setTextColor(0xFF7C8190.toInt())
            letterSpacing = 0.1f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        root.addView(label, rowLp(bottomMargin = dp(8)))

        // ── URL input ─────────────────────────────────────────────────────
        val urlInput = EditText(this).apply {
            hint = "https://verify.example.com/kyc?token=..."
            setHintTextColor(0xFF3A3F4A.toInt())
            setTextColor(0xFFE8EAF0.toInt())
            textSize = 12f
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_URI
            imeOptions = EditorInfo.IME_ACTION_GO
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = roundRect(0xFF111318.toInt(), 0xFF252830.toInt(), dp(10).toFloat())
            typeface = android.graphics.Typeface.MONOSPACE
            maxLines = 3
        }
        root.addView(urlInput, rowLp(bottomMargin = dp(8)))

        val hint2 = TextView(this).apply {
            text = "💡  Long-press above to paste your link"
            textSize = 11f
            setTextColor(0xFF3A3F4A.toInt())
        }
        root.addView(hint2, rowLp(bottomMargin = dp(30)))

        // ── Start button ──────────────────────────────────────────────────
        val startBtn = TextView(this).apply {
            text = "Start Verification  →"
            textSize = 16f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(dp(24), dp(18), dp(24), dp(18))
            background = roundRect(0xFF4F8EF7.toInt(), null, dp(14).toFloat())
            isClickable = true
            isFocusable = true
            elevation = dp(2).toFloat()
        }
        root.addView(startBtn, rowLp(bottomMargin = dp(14)))

        val demoBtn = TextView(this).apply {
            text = "Use demo URL for testing"
            textSize = 13f
            setTextColor(0xFF4F8EF7.toInt())
            gravity = Gravity.CENTER
            setPadding(0, dp(10), 0, dp(10))
            isClickable = true
            isFocusable = true
        }
        root.addView(demoBtn, rowLp(bottomMargin = dp(36)))

        // ── Info card ─────────────────────────────────────────────────────
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(16), dp(18), dp(16))
            background = roundRect(0xFF111318.toInt(), 0xFF252830.toInt(), dp(14).toFloat())
        }
        root.addView(card, rowLp())

        val cardTitle = TextView(this).apply {
            text = "HOW THIS WORKS"
            textSize = 10f
            setTextColor(0xFF7C8190.toInt())
            letterSpacing = 0.12f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        card.addView(cardTitle, rowLp(bottomMargin = dp(14)))

        listOf(
            "1️⃣" to "Paste your KYC link and tap Start",
            "2️⃣" to "When the selfie step appears, choose Gallery or Camera",
            "3️⃣" to "Gallery upload replaces the camera — no need to take a live selfie",
            "4️⃣" to "Video step is untouched — works exactly as normal"
        ).forEachIndexed { i, (emoji, text) ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            val e = TextView(this).apply {
                this.text = emoji; textSize = 15f
                setPadding(0, 0, dp(12), 0)
            }
            val t = TextView(this).apply {
                this.text = text; textSize = 13f
                setTextColor(0xFFADB5BD.toInt())
                lineSpacingMultiplier = 1.4f
            }
            row.addView(e, LinearLayout.LayoutParams(-2, -2))
            row.addView(t, LinearLayout.LayoutParams(0, -2, 1f))
            card.addView(row, rowLp(bottomMargin = if (i < 3) dp(12) else 0))
        }

        setContentView(scroll)

        // ── Click handlers ────────────────────────────────────────────────

        startBtn.setOnClickListener {
            val url = urlInput.text.toString().trim()
            if (url.isEmpty() || !url.startsWith("http")) {
                Toast.makeText(this, "⚠️ Please enter a valid https:// URL", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(urlInput.windowToken, 0)
            startActivity(Intent(this, KycWebViewActivity::class.java).apply {
                putExtra(KycWebViewActivity.EXTRA_URL, url)
            })
        }

        demoBtn.setOnClickListener {
            urlInput.setText("https://demo.withpersona.com/verify?inquiry-template-id=itmpl_demo")
        }

        urlInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) { startBtn.performClick(); true } else false
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun rowLp(
        bottomMargin: Int = 0,
        gravity: Int = -1
    ) = LinearLayout.LayoutParams(-1, -2).also {
        it.bottomMargin = bottomMargin
        if (gravity != -1) it.gravity = gravity
    }

    private fun roundRect(
        fillColor: Int,
        strokeColor: Int?,
        radius: Float
    ): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable().apply {
            setColor(fillColor)
            cornerRadius = radius
            if (strokeColor != null) setStroke(
                (1 * resources.displayMetrics.density).toInt(), strokeColor
            )
        }
    }
}
