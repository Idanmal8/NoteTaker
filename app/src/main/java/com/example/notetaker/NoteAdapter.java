package com.example.notetaker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<String> noteList;

    public NoteAdapter(List<String> noteList) {
        this.noteList = noteList;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        String note = noteList.get(position);
        holder.noteTextView.setText(note);
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView noteTextView;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTextView = itemView.findViewById(R.id.noteTextView);
        }
    }

    public void addNote(String note) {
        noteList.add(note);
        notifyItemInserted(noteList.size() - 1);
    }
}