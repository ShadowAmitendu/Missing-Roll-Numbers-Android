package com.shadowamitendu.missingrolls

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shadowamitendu.missingrolls.databinding.ActivityAboutAppBinding

class AboutApp : AppCompatActivity() {

    private lateinit var binding: ActivityAboutAppBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAboutAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the toolbar
        setSupportActionBar(binding.toolbar)

        // Enable the back button in the toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "About the App" // Set title

        // Update the FAB icon based on the current theme
        updateGitHubIcon()

        // Set up the FAB click listener to open GitHub after confirmation
        binding.fab.setOnClickListener {
            showGitHubConfirmationDialog()
        }
        updateLogoIcon()

        // Set the app version dynamically
        setAppVersion()
    }

    // Handle the back button action
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Function to update the GitHub icon based on the current theme
    private fun updateGitHubIcon() {
        val isDarkMode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES

        if (isDarkMode) {
            binding.fab.setImageResource(R.drawable.github_mark_white) // White icon for dark mode
        } else {
            binding.fab.setImageResource(R.drawable.github_mark) // Black icon for light mode
        }
    }

    // Function to show a confirmation dialog for GitHub URL
    private fun showGitHubConfirmationDialog() {
        val iconResId = getDialogIcon() // Get the appropriate icon

        MaterialAlertDialogBuilder(this)
            .setTitle("Open GitHub Repository")
            .setMessage("Do you want to open the code repository on GitHub?")
            .setIcon(iconResId)
            .setPositiveButton("Open") { _, _ -> openGitHub() }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .setCancelable(true)
            .show()
    }

    // Function to open the GitHub repository URL
    private fun openGitHub() {
        val githubUrl = "https://github.com/ShadowAmitendu/MissingRolls-AndroidVersion"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
        startActivity(intent)
    }

    // Function to determine the appropriate icon for the dialogs
    private fun getDialogIcon(): Int {
        val nightModeFlags = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return if (nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            R.drawable.github_mark_white // Icon for dark mode
        } else {
            R.drawable.github_mark // Icon for light mode
        }
    }

    // Function to dynamically set the app version in the TextView
    private fun setAppVersion() {
        val versionName = try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName
        } catch (e: Exception) {
            e.printStackTrace()
            "Unknown"
        }
        binding.appVersion.text = "App Version: $versionName"
    }
    private fun updateLogoIcon() {


        // Get the ImageView
        val logoImageView = binding.imageView

        // Set the icon based on the theme
        val logoRes = if (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            R.drawable.baseline_fact_check_24 // Dark mode icon
        } else {
            R.drawable.baseline_fact_check_24_bl // Light mode icon
        }

        // Update the ImageView
        logoImageView.setImageResource(logoRes)
    }

}
