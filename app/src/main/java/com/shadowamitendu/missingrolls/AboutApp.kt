package com.shadowamitendu.missingrolls

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.shadowamitendu.missingrolls.databinding.ActivityAboutAppBinding

class AboutApp : AppCompatActivity() {

    private lateinit var binding: ActivityAboutAppBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAboutAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the toolbar
        setSupportActionBar(binding.toolbar)

        // Set up the FAB click listener to open GitHub after confirmation
        binding.fab.setOnClickListener {
            showGitHubConfirmationDialog()
        }
        val mainToolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(mainToolbar)
    }

    // Function to show a confirmation dialog for GitHub URL
    private fun showGitHubConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Open GitHub Repository")
            .setMessage("Do you want to open the code repository on GitHub?")
            .setPositiveButton("Open") { _, _ -> openGitHub() }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    // Function to open the GitHub repository URL
    private fun openGitHub() {
        val githubUrl = "https://github.com/ShadowAmitendu/MissingRolls-AndroidVersion" // Replace with your GitHub URL
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
        startActivity(intent)
    }

}
