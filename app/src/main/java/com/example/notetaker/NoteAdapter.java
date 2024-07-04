package com.example.notetaker;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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
        holder.titleView.setText(note);
        holder.dateView.setText("HH:MM DD/MM/YYYY");
        holder.imageView.setImageResource(R.drawable.fab_background);

        int backgroundColor = ContextCompat.getColor(holder.itemView.getContext(),
                R.color.black); // Replace with your default color

        TypedValue typedValue = new TypedValue();
        if (holder.itemView.getContext().getTheme().resolveAttribute(
                android.R.attr.windowBackground, typedValue, true)) {
            if (typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT &&
                    typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                backgroundColor = typedValue.data;
            }
        }

        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] {backgroundColor, Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT}
        );

        holder.gradientOverlayView.setBackground(gradientDrawable);
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        View gradientOverlayView;
        TextView titleView;
        TextView dateView;
        ImageView imageView;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.note_title);
            dateView = itemView.findViewById(R.id.note_date_time);
            imageView = itemView.findViewById(R.id.note_image);
            gradientOverlayView = itemView.findViewById(R.id.gradient_overlay);
        }

    }

    public void addNote(String note) {
        noteList.add(note);
        notifyItemInserted(noteList.size() - 1);
    }
}