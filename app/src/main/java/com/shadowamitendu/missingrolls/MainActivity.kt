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
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var startingRollEditText: TextInputEditText
    private lateinit var endingRollEditText: TextInputEditText
    private lateinit var missingRollNumbersTextView: TextView
    private lateinit var copyMissingRollBtn: Button
    private lateinit var browseFolderBtn: Button
    private lateinit var startCheckingBtn: Button
    private lateinit var scrollView: ScrollView

    private var folderUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI elements
        startingRollEditText = findViewById(R.id.startingRollEditText)
        endingRollEditText = findViewById(R.id.endingRollEditText)
        missingRollNumbersTextView = findViewById(R.id.missingRollNumbers)
        copyMissingRollBtn = findViewById(R.id.copyMissingRollBtn)
        browseFolderBtn = findViewById(R.id.browseFolderBtn)
        startCheckingBtn = findViewById(R.id.startCheckingBtn)
        scrollView = findViewById(R.id.scrollView)

        // Set listeners on buttons
        startCheckingBtn.setOnClickListener {
            checkMissingRollNumbers()
        }

        copyMissingRollBtn.setOnClickListener {
            copyToClipboard()
        }

        browseFolderBtn.setOnClickListener {
            openFolderSelector()
        }

        // Optional: Add input validation
        addTextChangeListeners()
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
        // Validate the input fields and enable/disable startCheckingBtn accordingly
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

            // Use DocumentFile to access files within the selected folder
            val documentFile = DocumentFile.fromTreeUri(this, folderUri!!) ?: return
            documentFile.listFiles().forEach { file ->
                if (file.name?.endsWith(".pdf") == true) {
                    // Extract roll number from the file name (first 11 characters)
                    val fileName = file.name?.substring(0, 11)
                    val rollNumber = fileName?.takeLast(3)?.toIntOrNull() // Extract last 3 digits

                    // Collect roll numbers that are present
                    rollNumber?.let {
                        presentRollNumbers.add(it)
                    }
                }
            }

            // Find missing roll numbers
            val missingRollNumbers = (startRoll..endRoll).filter { it % 1000 !in presentRollNumbers }

            // Update the TextView with missing roll numbers
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

    // Open folder selector
    private fun openFolderSelector() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        folderLauncher.launch(intent)
    }

    // Folder selection callback
    private val folderLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            folderUri = result.data?.data

            if (folderUri != null) {
                // Grant permission to the folder
                contentResolver.takePersistableUriPermission(folderUri!!, Intent.FLAG_GRANT_READ_URI_PERMISSION)

                Toast.makeText(this, "Folder selected", Toast.LENGTH_SHORT).show()
                startCheckingBtn.isEnabled = true
            } else {
                Toast.makeText(this, "Folder selection failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Dismiss focus from EditText when tapping outside
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
