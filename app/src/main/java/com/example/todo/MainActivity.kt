package com.example.todo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import android.appwidget.AppWidgetManager

class MainActivity : AppCompatActivity() {

    private var notes: MutableList<String> = mutableListOf()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NoteAdapter
    private lateinit var buttonOpenTimer: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerViewNotes)
        recyclerView.layoutManager = LinearLayoutManager(this)

        notes = readAllNotesFromInternalStorage().toMutableList()

        adapter = NoteAdapter(notes, { note ->
            // Handle update click
            val intent = Intent(this, UpdateNoteActivity::class.java)
            intent.putExtra("NOTE", note)  // Pass the note to the update activity
            startActivityForResult(intent, UPDATE_NOTE_REQUEST_CODE)
        }) { note ->
            // Handle delete click
            showDeleteConfirmationDialog(note) // Show confirmation dialog before deleting
        }

        recyclerView.adapter = adapter

        // Set up the FloatingActionButton to navigate to the add note activity
        val floatingActionButton = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.floatingActionButton2)
        floatingActionButton.setOnClickListener {
            val intent = Intent(this, MainActivity1::class.java)
            startActivityForResult(intent, ADD_NOTE_REQUEST_CODE)
        }

        // Initialize and set up the button to open the TimerActivity
        buttonOpenTimer = findViewById(R.id.buttonOpenTimer)
        buttonOpenTimer.setOnClickListener {
            val intent = Intent(this, Timer::class.java)
            startActivity(intent)
        }

        if (notes.isEmpty()) {
            Toast.makeText(this, "No notes found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteConfirmationDialog(note: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Note")
        builder.setMessage("Do you really want to delete this note?")
        builder.setPositiveButton("Yes") { _, _ -> deleteNote(note) }
        builder.setNegativeButton("No") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun deleteNote(note: String) {
        notes.remove(note)
        saveNotesToInternalStorage() // Save the updated notes list
        adapter.notifyDataSetChanged()
        updateWidget() // Update the widget after deleting a note
        Toast.makeText(this, "Note deleted: $note", Toast.LENGTH_SHORT).show()
    }

    private fun readAllNotesFromInternalStorage(): List<String> {
        val filename = "notes.txt"
        val notesList = mutableListOf<String>()
        try {
            val fileInputStream = openFileInput(filename)
            val inputStreamReader = InputStreamReader(fileInputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            val stringBuilder = StringBuilder()
            var line: String? = bufferedReader.readLine()

            while (line != null) {
                stringBuilder.append(line).append("\n")
                if (line.isEmpty()) {
                    notesList.add(stringBuilder.toString().trim())
                    stringBuilder.setLength(0)
                }
                line = bufferedReader.readLine()
            }

            if (stringBuilder.isNotEmpty()) {
                notesList.add(stringBuilder.toString().trim())
            }

            fileInputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return notesList
    }

    private fun saveNotesToInternalStorage() {
        val filename = "notes.txt"
        try {
            val fileOutputStream: FileOutputStream = openFileOutput(filename, MODE_PRIVATE)
            for (note in notes) {
                fileOutputStream.write("$note\n".toByteArray())
            }
            fileOutputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun updateWidget() {
        val intent = Intent(this, TaskWidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        sendBroadcast(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPDATE_NOTE_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.let {
                val updatedNote = it.getStringExtra("UPDATED_NOTE") ?: return
                val originalNote = it.getStringExtra("ORIGINAL_NOTE") ?: return

                // Update the note in the list
                val index = notes.indexOf(originalNote)
                if (index != -1) {
                    notes[index] = updatedNote
                    saveNotesToInternalStorage()
                    adapter.notifyDataSetChanged()
                    updateWidget() // Update the widget after changing the notes
                }
            }
        } else if (requestCode == ADD_NOTE_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.let {
                val newNote = it.getStringExtra("NEW_NOTE") ?: return
                notes.add(newNote)
                saveNotesToInternalStorage()
                adapter.notifyDataSetChanged()
                updateWidget() // Update the widget after adding a new note
            }
        }
    }

    companion object {
        private const val UPDATE_NOTE_REQUEST_CODE = 1
        private const val ADD_NOTE_REQUEST_CODE = 2
    }
}
