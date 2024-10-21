package com.example.todo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NoteAdapter(
    private var notesList: List<String>,
    private val onUpdateClick: (String) -> Unit,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.note_item, parent, false)
        return NoteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notesList[position]

        // Split the note into title and description
        val parts = note.split("\n", limit = 2)
        holder.noteTitleTextView.text = parts[0].trim() // Display title
        holder.noteDescriptionTextView.text = if (parts.size > 1) parts[1].trim() else "" // Display description

        holder.updateButton.setOnClickListener {
            onUpdateClick(note)
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClick(note)
        }
    }

    override fun getItemCount(): Int {
        return notesList.size
    }

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noteTitleTextView: TextView = itemView.findViewById(R.id.textViewNoteTitle) // Title TextView
        val noteDescriptionTextView: TextView = itemView.findViewById(R.id.textViewNoteDescription) // Description TextView
        val updateButton: Button = itemView.findViewById(R.id.buttonUpdate)
        val deleteButton: Button = itemView.findViewById(R.id.buttonDelete)
    }
}
