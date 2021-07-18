package com.HM.translator;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentification;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity<dataAdapter> extends AppCompatActivity {

    private String sourceText;
    private TextView mTranslatedText;
    private TextToSpeech mTTs;
    private TextView mDetecting;
    private EditText mSourceText;
    private Button mButtonSpeak;
    private Spinner spinnerTextSize;
    private String code;

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        Button mTranslateBtn = findViewById(R.id.button2);
        mDetecting = findViewById(R.id.textView);
        mSourceText = findViewById(R.id.sourceText);
        mTranslatedText = findViewById(R.id.sourceText2);
        mButtonSpeak = findViewById(R.id.button3);
        spinnerTextSize = findViewById(R.id.spinner);

        String message = getIntent().getStringExtra("keyname");
        mSourceText.setText(message);

        List<String> categories = new ArrayList<>();
        categories.add(0,"Choose Language");
        categories.add("English");
        categories.add("German");
        categories.add("French");
        categories.add("Spanish");
        categories.add("Marathi");
        categories.add("Hindi");
        categories.add("Tamil");

        ArrayAdapter<String> dataAdapter;
        dataAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, categories);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerTextSize.setAdapter(dataAdapter);

        spinnerTextSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(adapterView.getItemAtPosition(i).equals("Choose Language")){

                }
                else {
                    code = adapterView.getItemAtPosition(i).toString();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mTTs = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    int result = mTTs.setLanguage(Locale.ENGLISH);
                    System.out.println("in speak0");
                    if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("TTS","Language not supported");
                    } else {
                        //mButtonSpeak.setEnabled(true);
                    }
                } else {
                    Log.e("TTS","Initialization Failed");
                }
            }
        });

        mButtonSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak();
            }
        });

        mTranslateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                identifyLanguage();
            }
        });

    }

    private void speak() {
        System.out.println("in speak");
        String text = mTranslatedText.getText().toString();
        mTTs.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    protected void onDestroy() {
        if(mTTs != null) {
            mTTs.stop();
            mTTs.shutdown();
        }
        super.onDestroy();
    }

    public void btnSpeech(View view) {
        mDetecting.setText("");
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hi Speak Something");
        try {
            startActivityForResult(intent, 1);
        }catch (ActivityNotFoundException e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 1:

                if(resultCode==RESULT_OK && null!=data){

                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    mSourceText.setText(result.get(0));
                }
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    private void identifyLanguage() {
        sourceText = mSourceText.getText().toString();

        FirebaseLanguageIdentification identifier = FirebaseNaturalLanguage.getInstance().getLanguageIdentification();

        mDetecting.setText("Detecting.....");

        identifier.identifyLanguage(sourceText).addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                if(s.equals("und")){
                    Toast.makeText(getApplicationContext(), "Language Not Identified", Toast.LENGTH_SHORT).show();
                }
                else {
                    getLanguageCode(s);
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void getLanguageCode(String language) {
        int langCode;
        switch (language)
        {
            case "fr":
                langCode = FirebaseTranslateLanguage.FR;
                mDetecting.setText("French");
                break;
            case "de":
                langCode = FirebaseTranslateLanguage.DE;
                mDetecting.setText("German");
                break;
            case "es":
                langCode = FirebaseTranslateLanguage.ES;
                mDetecting.setText("Spanish");
                break;
            case "mr":
                langCode = FirebaseTranslateLanguage.MR;
                mDetecting.setText("Marathi");
                break;
            case "hi":
                langCode = FirebaseTranslateLanguage.HI;
                mDetecting.setText("Hindi");
                break;
            case "ta":
                langCode = FirebaseTranslateLanguage.TA;
                mDetecting.setText("Tamil");
                break;
            case "en":
                langCode = FirebaseTranslateLanguage.EN;
                mDetecting.setText("English");
                break;
            default:
                langCode = 0;
        }

        translateText(langCode,code);
    }

    @SuppressLint("SetTextI18n")
    private void translateText(int langCode, String code) {

        if (code == "English") {
            mTranslatedText.setText("Translating...");
            FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder().setSourceLanguage(langCode).setTargetLanguage(FirebaseTranslateLanguage.EN).build();

            final FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);

            FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();

            translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    translator.translate(sourceText).addOnSuccessListener(new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(String s) {
                            mTranslatedText.setText(s);
                        }
                    });
                }
            });
        }

        if (code == "German") {
            mTranslatedText.setText("Translating...");
            FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder().setSourceLanguage(langCode).setTargetLanguage(FirebaseTranslateLanguage.DE).build();

            final FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);

            FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();

            translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    translator.translate(sourceText).addOnSuccessListener(new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(String s) {
                            mTranslatedText.setText(s);
                        }
                    });
                }
            });
        }

        if (code == "French") {
            mTranslatedText.setText("Translating...");
            FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder().setSourceLanguage(langCode).setTargetLanguage(FirebaseTranslateLanguage.FR).build();

            final FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);

            FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();

            translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    translator.translate(sourceText).addOnSuccessListener(new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(String s) {
                            mTranslatedText.setText(s);
                        }
                    });
                }
            });
        }

        if (code == "Spanish") {
            mTranslatedText.setText("Translating...");
            FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder().setSourceLanguage(langCode).setTargetLanguage(FirebaseTranslateLanguage.ES).build();

            final FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);

            FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();

            translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    translator.translate(sourceText).addOnSuccessListener(new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(String s) {
                            mTranslatedText.setText(s);
                        }
                    });
                }
            });
        }

        if (code == "Tamil") {
            mTranslatedText.setText("Translating...");
            FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder().setSourceLanguage(langCode).setTargetLanguage(FirebaseTranslateLanguage.TA).build();

            final FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);

            FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();

            translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    translator.translate(sourceText).addOnSuccessListener(new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(String s) {
                            mTranslatedText.setText(s);
                        }
                    });
                }
            });
        }

        if (code == "Hindi") {
            mTranslatedText.setText("Translating...");
            FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder().setSourceLanguage(langCode).setTargetLanguage(FirebaseTranslateLanguage.HI).build();

            final FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);

            FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();

            translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    translator.translate(sourceText).addOnSuccessListener(new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(String s) {
                            mTranslatedText.setText(s);
                        }
                    });
                }
            });
        }

        if (code == "Marathi") {
            mTranslatedText.setText("Translating...");
            FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder().setSourceLanguage(langCode).setTargetLanguage(FirebaseTranslateLanguage.MR).build();

            final FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);

            FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();

            translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    translator.translate(sourceText).addOnSuccessListener(new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(String s) {
                            mTranslatedText.setText(s);
                        }
                    });
                }
            });
        }

    }



}