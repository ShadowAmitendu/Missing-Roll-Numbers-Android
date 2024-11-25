package com.shadowamitendu.missingrolls

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var startingRollEditText: TextInputEditText
    private lateinit var endingRollEditText: TextInputEditText
    private lateinit var missingRollNumbersTextView: TextView
    private lateinit var copyMissingRollBtn: Button
    private lateinit var browseFolderBtn: Button
    private lateinit var startCheckingBtn: Button
    private lateinit var aboutButton: ImageButton
    private lateinit var newReleasesButton: ImageButton
    private lateinit var helpButton: ImageButton
    private lateinit var scrollView: ScrollView

    private var folderUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mainToolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.mainToolbar)
        setSupportActionBar(mainToolbar)

        // Initialize UI elements
        startingRollEditText = findViewById(R.id.startingRollEditText)
        endingRollEditText = findViewById(R.id.endingRollEditText)
        missingRollNumbersTextView = findViewById(R.id.missingRollNumbers)
        copyMissingRollBtn = findViewById(R.id.copyMissingRollBtn)
        browseFolderBtn = findViewById(R.id.browseFolderBtn)
        startCheckingBtn = findViewById(R.id.startCheckingBtn)
        aboutButton = findViewById(R.id.aboutButton)
        newReleasesButton = findViewById(R.id.newReleases)
        helpButton = findViewById(R.id.howTos)
        scrollView = findViewById(R.id.scrollView)

        // Set listeners on buttons
        startCheckingBtn.setOnClickListener { checkMissingRollNumbers() }

        copyMissingRollBtn.setOnClickListener { copyToClipboard() }

        browseFolderBtn.setOnClickListener { openFolderSelector() }

        aboutButton.setOnClickListener { openAboutApp() }

        newReleasesButton.setOnClickListener { showNewReleasesDialog() }

        helpButton.setOnClickListener { openHelpActivity() }

        addTextChangeListeners()
    }

    private fun openAboutApp() {
        val intent = Intent(this, AboutApp::class.java)
        startActivity(intent)
    }

    private fun openHelpActivity() {
        val intent = Intent(this, help::class.java)
        startActivity(intent)
    }

    private fun showNewReleasesDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("New Releases")
            .setMessage("Do you want to visit the GitHub release page?")
            .setIcon(R.drawable.new_releases_24dp) // Optional: Add a relevant icon
            .setPositiveButton("Yes") { _, _ ->
                val releaseUrl = "https://github.com/ShadowAmitendu/MissingRolls-AndroidVersion/releases"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(releaseUrl))
                startActivity(intent)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss() // Simply dismiss the dialog
            }
            .setCancelable(false) // Allows dismissing the dialog by tapping outside
            .show()
    }


    private fun addTextChangeListeners() {
        startingRollEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateInput()
            }
        })

        endingRollEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateInput()
            }
        })
    }

    private fun validateInput() {
        val startRoll = startingRollEditText.text.toString().trim()
        val endRoll = endingRollEditText.text.toString().trim()

        startCheckingBtn.isEnabled = startRoll.isNotEmpty() && endRoll.isNotEmpty()
    }

    private fun checkMissingRollNumbers() {
        val startRoll = startingRollEditText.text.toString().toIntOrNull()
        val endRoll = endingRollEditText.text.toString().toIntOrNull()

        if (startRoll == null || endRoll == null || startRoll > endRoll) {
            Toast.makeText(this, "Invalid roll numbers", Toast.LENGTH_SHORT).show()
            return
        }

        if (folderUri == null) {
            Toast.makeText(this, "Please select a folder first", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val presentRollNumbers = mutableListOf<Int>()

            val documentFile = DocumentFile.fromTreeUri(this, folderUri!!) ?: return
            documentFile.listFiles().forEach { file ->
                if (file.name?.endsWith(".pdf") == true) {
                    val fileName = file.name?.substring(0, 11)
                    val rollNumber = fileName?.takeLast(3)?.toIntOrNull()

                    rollNumber?.let {
                        presentRollNumbers.add(it)
                    }
                }
            }

            val missingRollNumbers = (startRoll..endRoll).filter { it % 1000 !in presentRollNumbers }

            missingRollNumbersTextView.text = if (missingRollNumbers.isEmpty()) {
                "All roll numbers are present!"
            } else {
                "Missing roll numbers: ${missingRollNumbers.joinToString(", ")}"
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Error checking files", Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyToClipboard() {
        val textToCopy = missingRollNumbersTextView.text.toString()

        if (textToCopy.isNotEmpty()) {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Missing Roll Numbers", textToCopy)
            clipboard.setPrimaryClip(clip)

            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Nothing to copy", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openFolderSelector() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        folderLauncher.launch(intent)
    }

    private val folderLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                folderUri = result.data?.data

                if (folderUri != null) {
                    contentResolver.takePersistableUriPermission(
                        folderUri!!,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                    Toast.makeText(this, "Folder selected", Toast.LENGTH_SHORT).show()
                    startCheckingBtn.isEnabled = true
                } else {
                    Toast.makeText(this, "Folder selection failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is TextInputEditText) {
                val outRect = android.graphics.Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}
