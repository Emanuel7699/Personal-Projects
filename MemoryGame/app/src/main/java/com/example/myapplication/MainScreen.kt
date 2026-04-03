package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainScreen : AppCompatActivity() {

    // [Variable] The button that starts the game and launches MainActivity.
    lateinit var buttonStart: Button

    // [Variable] The TextView that displays the user's highest achieved score.
    lateinit var BestScoreValue: TextView

    // [Object] SharedPreferences - Used for storing and retrieving simple key-value data, making the best score persistent.
    lateinit var prefs: SharedPreferences

    // [Variable - Int] Stores the best score. It's loaded from SharedPreferences on startup.
    var bestScore = 0

    /**
     * [Activity Launcher] An ActivityResultLauncher that handles starting the game Activity (MainActivity)
     * and processing the result when it returns. This is the modern, recommended way to handle
     * "startActivityForResult".
     */
    val gameLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // This code block is executed when MainActivity closes and returns a result to MainScreen.
        if (result.resultCode == Activity.RESULT_OK) {
            // Retrieve the score returned from the game activity. Defaults to 0 if not found.
            val returnedScore = result.data?.getIntExtra("bestScore", 0) ?: 0

            // Check if the score from the completed game is higher than the current best score.
            if (returnedScore > bestScore) {
                bestScore = returnedScore // Update the local best score variable.
                // Save the new best score to SharedPreferences to persist it even after the app closes.
                prefs.edit().putInt("bestScore", bestScore).apply()
            }
            // Update the TextView on the screen to display the current (and possibly new) best score.
            BestScoreValue.text = "$bestScore"
        }
    }

    /**
     * [Main Function] onCreate - Called when the MainScreen is first created.
     * It's responsible for initializing the UI components, loading the saved best score,
     * and setting up the listener for the start button.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen) // Link the code to its XML layout file.

        // --- Initialize UI Widgets ---
        buttonStart = findViewById(R.id.buttonStart)
        BestScoreValue = findViewById(R.id.BestScoreValue)

        // --- Load Saved Data ---
        // Get a reference to the app's private preferences file named "simon_prefs".
        prefs = getSharedPreferences("simon_prefs", MODE_PRIVATE)
        // Load the integer value for "bestScore". If it doesn't exist, it defaults to 0.
        bestScore = prefs.getInt("bestScore", 0)
        // Display the loaded best score in the TextView.
        BestScoreValue.text = "$bestScore"

        // --- Set up Click Listener ---
        buttonStart.setOnClickListener {
            // Create an Intent to navigate from this screen (MainScreen) to the game screen (MainActivity).
            val i = Intent(this, MainActivity::class.java)
            // Pass the current best score to the game activity so it knows the score to beat.
            i.putExtra("bestScore", bestScore)
            // Launch the game activity using the launcher defined above, which will wait for a result.
            gameLauncher.launch(i)
        }
    }
}
