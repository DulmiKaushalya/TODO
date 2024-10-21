package com.example.todo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class UpdateNoteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_note)

        val originalNote = intent.getStringExtra("NOTE") ?: return

        // Split the original note into title and description
        val parts = originalNote.split("\n", limit = 2)
        val title = if (parts.isNotEmpty()) parts[0].trim() else ""
        val description = if (parts.size > 1) parts[1].trim() else ""

        val titleEditText = findViewById<EditText>(R.id.editTextTitle)
        val descriptionEditText = findViewById<EditText>(R.id.editTextDescription)
        val saveButton = findViewById<Button>(R.id.buttonSave)

        titleEditText.setText(title) // Set the note title
        descriptionEditText.setText(description) // Set the note description

        saveButton.setOnClickListener {
            // Get the updated title and description
            val updatedTitle = titleEditText.text.toString().trim()
            val updatedDescription = descriptionEditText.text.toString().trim()
            val updatedNote = "$updatedTitle\n$updatedDescription" // Keep separate lines

            // Pass the updated note back to MainActivity
            val resultIntent = Intent().apply {
                putExtra("UPDATED_NOTE", updatedNote)
                putExtra("ORIGINAL_NOTE", originalNote)
            }
            setResult(RESULT_OK, resultIntent)
            finish() // Navigate back to MainActivity
        }
    }
}
