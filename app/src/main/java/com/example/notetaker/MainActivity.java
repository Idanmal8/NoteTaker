package com.example.notetaker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final String TAG = "MainActivity";
    private Button fab;
    private NoteAdapter noteAdapter;
    private List<String> noteList;
    private TextView placeholderTextView;
    private boolean isRecording = false; // Flag to track recording state

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;

    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

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

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.d(TAG, "Ready for speech");
            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d(TAG, "Beginning of speech");
            }

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                Log.d(TAG, "End of speech");
            }

            @Override
            public void onError(int error) {
                Log.e(TAG, "Transcription error: " + error);
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String transcribedText = matches.get(0);
                    Log.d(TAG, "Transcription result: " + transcribedText);
                    addNoteToList(transcribedText);
                } else {
                    Log.d(TAG, "No transcription result");
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });

        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
    }

    private void showBottomSheetDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_layout);

        final EditText noteTextField = bottomSheetDialog.findViewById(R.id.noteTextField);
        ImageButton recordButton = bottomSheetDialog.findViewById(R.id.recordButton);
        ImageButton sendButton = bottomSheetDialog.findViewById(R.id.sendButton);

        assert recordButton != null;
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle recording state
                isRecording = !isRecording;

                if (isRecording) {
                    Log.d(TAG, "Starting transcription");
                    speechRecognizer.startListening(speechRecognizerIntent);
                    recordButton.setImageResource(R.mipmap.ic_launcher_stop_record_round); // Update to your stop icon
                } else {
                    Log.d(TAG, "Stopping transcription");
                    speechRecognizer.stopListening();
                    recordButton.setImageResource(R.mipmap.ic_launcher_record_round); // Update to your record icon
                }
            }
        });

        assert sendButton != null;
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assert noteTextField != null;
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
        Log.d(TAG, "Note added to list: " + noteText);
    }

    private void updatePlaceholderVisibility() {
        if (noteList.isEmpty()) {
            placeholderTextView.setVisibility(View.VISIBLE);
        } else {
            placeholderTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionToRecordAccepted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        if (!permissionToRecordAccepted) {
            Log.d(TAG, "Permissions not granted. Closing the app.");
            finish();
        } else {
            Log.d(TAG, "Permissions granted.");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}
