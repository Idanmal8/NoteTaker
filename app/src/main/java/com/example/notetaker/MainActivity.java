package com.example.notetaker;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    private EditText noteTextField; // Member variable for EditText
    private TextView dateTimeTextView; // TextView for date and time

    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private String noteTextController;

    // Member variables for the selected image
    private LinearLayout imageContainer;
    private ImageView selectedImageView;
    private Uri imageUri;

    // Constants for image selection
    private static final int PICK_IMAGE_REQUEST = 1;



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
                    noteTextController += transcribedText + " ";
                    Log.d(TAG, "Transcription result: " + transcribedText);
                    setNoteTextField(noteTextController);
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

        noteTextField = bottomSheetDialog.findViewById(R.id.noteTextField); // Initialize the EditText
        ImageButton recordButton = bottomSheetDialog.findViewById(R.id.recordButton);
        ImageButton sendButton = bottomSheetDialog.findViewById(R.id.sendButton);
        ImageButton addImageButton = bottomSheetDialog.findViewById(R.id.addImage);
        dateTimeTextView = bottomSheetDialog.findViewById(R.id.btn_open_date_time_picker);

        imageContainer = bottomSheetDialog.findViewById(R.id.imageContainer);
        selectedImageView = bottomSheetDialog.findViewById(R.id.selectedImageView);
        Button removeImageButton = bottomSheetDialog.findViewById(R.id.removeImageButton);

        updateDateTime();
        //check that dateTimeTextView is not null before calling setText

        // Image selection handling
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        // Remove image handling
        removeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageContainer.setVisibility(View.GONE);
                selectedImageView.setVisibility(View.GONE);
                imageUri = null;
            }
        });

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

        assert dateTimeTextView != null;
        dateTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateTimePicker();
            }
        });

        bottomSheetDialog.show();
    }

    private void setNoteTextField(String noteText) {
        Log.d(TAG, "setNoteTextField: " + noteText);
        if (noteTextField != null) {
            Log.d(TAG, "setNoteTextField2: " + noteText);
            noteTextField.setText(noteText);
        }
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

    private void updateDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd-MM-yyyy", Locale.getDefault());
        Log.d(TAG, "updateDateTime: " + sdf.format(new Date().getTime()));
        String currentDateTime = sdf.format(new Date().getTime());
        dateTimeTextView.setText(currentDateTime);
    }

    private void showDateTimePicker() {
        final Calendar currentDate = Calendar.getInstance();
        final Calendar selectedDate = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                selectedDate.set(Calendar.YEAR, year);
                selectedDate.set(Calendar.MONTH, month);
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedDate.set(Calendar.MINUTE, minute);

                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd-MM-yyyy", Locale.getDefault());
                        String selectedDateTime = sdf.format(selectedDate.getTime());
                        dateTimeTextView.setText(selectedDateTime);
                    }
                }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), true);

                timePickerDialog.show();
            }
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
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

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                selectedImageView.setImageBitmap(bitmap);
                imageContainer.setVisibility(View.VISIBLE);
                selectedImageView.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
