package com.example.notetaker;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button fab;
    private NoteAdapter noteAdapter;
    private List<String> noteList;
    private TextView placeholderTextView;
    private boolean isRecording = false; // Flag to track recording state


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        placeholderTextView = findViewById(R.id.placeholderTextView);
        noteList = new ArrayList<>();
        noteAdapter = new NoteAdapter(noteList);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(noteAdapter);

        fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheetDialog();
            }
        });

        updatePlaceholderVisibility();
    }

    private void showBottomSheetDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_layout);

        final EditText noteTextField = bottomSheetDialog.findViewById(R.id.noteTextField);
        ImageButton recordButton = bottomSheetDialog.findViewById(R.id.recordButton);
        ImageButton sendButton = bottomSheetDialog.findViewById(R.id.sendButton);

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle recording state
                isRecording = !isRecording;

                // Change button image based on recording state
                if (isRecording) {
                    recordButton.setImageResource(R.mipmap.ic_launcher_record); // Update to your stop icon
                } else {
                    recordButton.setImageResource(R.mipmap.ic_launcher_stop_record_foreground); // Update to your record icon
                }
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String noteText = noteTextField.getText().toString();
                if (!noteText.isEmpty()) {
                    addNoteToList(noteText);
                    bottomSheetDialog.dismiss();
                }
            }
        });

        bottomSheetDialog.show();
    }

    private void addNoteToList(String noteText) {
        noteAdapter.addNote(noteText);
        updatePlaceholderVisibility();
    }

    private void updatePlaceholderVisibility() {
        if (noteList.isEmpty()) {
            placeholderTextView.setVisibility(View.VISIBLE);
        } else {
            placeholderTextView.setVisibility(View.GONE);
        }
    }
}