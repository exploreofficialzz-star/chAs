package com.chas.verify

import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
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

        val icon = TextView(this).apply {
            text = "🔐"
            textSize = 56f
            gravity = Gravity.CENTER
        }
        root.addView(icon, rowLp(bottomMargin = dp(18), centerH = true))

        val appName = TextView(this).apply {
            text = "chAs"
            textSize = 36f
            setTextColor(0xFFE8EAF0.toInt())
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.05f
        }
        root.addView(appName, rowLp(bottomMargin = dp(6), centerH = true))

        val tagline = TextView(this).apply {
            text = "Identity Verification Wrapper"
            textSize = 13f
            setTextColor(0xFF4F8EF7.toInt())
            gravity = Gravity.CENTER
        }
        root.addView(tagline, rowLp(bottomMargin = dp(28), centerH = true))

        val divider = android.view.View(this).apply {
            setBackgroundColor(0xFF252830.toInt())
        }
        root.addView(divider, LinearLayout.LayoutParams(-1, dp(1)).also { it.bottomMargin = dp(28) })

        val label = TextView(this).apply {
            text = "VERIFICATION LINK"
            textSize = 11f
            setTextColor(0xFF7C8190.toInt())
            letterSpacing = 0.1f
            typeface = Typeface.DEFAULT_BOLD
        }
        root.addView(label, rowLp(bottomMargin = dp(8)))

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
            typeface = Typeface.MONOSPACE
            maxLines = 3
        }
        root.addView(urlInput, rowLp(bottomMargin = dp(8)))

        val hint2 = TextView(this).apply {
            text = "Long-press above to paste your link"
            textSize = 11f
            setTextColor(0xFF3A3F4A.toInt())
        }
        root.addView(hint2, rowLp(bottomMargin = dp(30)))

        val startBtn = TextView(this).apply {
            text = "Start Verification  →"
            textSize = 16f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
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
            typeface = Typeface.DEFAULT_BOLD
        }
        card.addView(cardTitle, rowLp(bottomMargin = dp(14)))

        val steps = listOf(
            "1" to "Paste your KYC link and tap Start",
            "2" to "When the selfie step appears, choose Gallery or Camera",
            "3" to "Gallery upload replaces the camera — no live selfie needed",
            "4" to "Video step is untouched — works exactly as normal"
        )

        steps.forEachIndexed { i, (stepNum, stepLabel) ->
            val stepRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val numView = TextView(this)
            numView.text = stepNum
            numView.textSize = 13f
            numView.setTextColor(0xFF4F8EF7.toInt())
            numView.typeface = Typeface.DEFAULT_BOLD
            numView.setPadding(0, 0, dp(12), 0)

            val labelView = TextView(this)
            labelView.text = stepLabel
            labelView.textSize = 13f
            labelView.setTextColor(0xFFADB5BD.toInt())
            labelView.setLineSpacing(0f, 1.4f) // use method, not property — lineSpacingMultiplier is read-only

            stepRow.addView(numView, LinearLayout.LayoutParams(-2, -2))
            stepRow.addView(labelView, LinearLayout.LayoutParams(0, -2, 1f))
            card.addView(stepRow, rowLp(bottomMargin = if (i < 3) dp(12) else 0))
        }

        setContentView(scroll)

        startBtn.setOnClickListener {
            val url = urlInput.text.toString().trim()
            if (url.isEmpty() || !url.startsWith("http")) {
                Toast.makeText(this, "Please enter a valid https:// URL", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(urlInput.windowToken, 0)
            val intent = Intent(this, KycWebViewActivity::class.java)
            intent.putExtra(KycWebViewActivity.EXTRA_URL, url)
            startActivity(intent)
        }

        demoBtn.setOnClickListener {
            urlInput.setText("https://demo.withpersona.com/verify?inquiry-template-id=itmpl_demo")
        }

        urlInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) { startBtn.performClick(); true } else false
        }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun rowLp(bottomMargin: Int = 0, centerH: Boolean = false) =
        LinearLayout.LayoutParams(-1, -2).also {
            it.bottomMargin = bottomMargin
            if (centerH) it.gravity = Gravity.CENTER_HORIZONTAL
        }

    private fun roundRect(fillColor: Int, strokeColor: Int?, radius: Float): GradientDrawable {
        return GradientDrawable().apply {
            setColor(fillColor)
            cornerRadius = radius
            if (strokeColor != null) {
                setStroke(resources.displayMetrics.density.toInt(), strokeColor)
            }
        }
    }
}
