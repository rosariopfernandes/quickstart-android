/*
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.quickstart.appindexing.java;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
// [START import_classes]
import com.google.firebase.appindexing.FirebaseAppIndex;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.appindexing.Indexable;
import com.google.firebase.appindexing.builders.Actions;
// [END import_classes]
import com.google.samples.quickstart.appindexing.R;
import com.google.samples.quickstart.appindexing.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    // Define a title for your current page, shown in autocompletion UI
    private static final String TITLE = "Sample Article";
    private String articleId;

    // [START handle_intent]
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // [START_EXCLUDE]
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        final FirebaseAppIndex firebaseAppIndex = FirebaseAppIndex.getInstance(this);

        binding.addStickersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(MainActivity.this, AppIndexingService.class));
            }
        });
        binding.clearStickersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppIndexingUtil.clearStickers(MainActivity.this, firebaseAppIndex);
            }
        });
        // [END_EXCLUDE]
        onNewIntent(getIntent());
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction();
        Uri data = intent.getData();
        if (Intent.ACTION_VIEW.equals(action) && data != null) {
            articleId = data.getLastPathSegment();
            TextView linkText = findViewById(R.id.link);
            linkText.setText(data.toString());
        }
    }
    // [END handle_intent]

    // [START app_indexing_view]
    @Override
    public void onStart() {
        super.onStart();
        if (articleId == null) return;
        final Uri BASE_URL = Uri.parse("https://www.example.com/articles/");
        final String APP_URI = BASE_URL.buildUpon().appendPath(articleId).build().toString();

        Indexable articleToIndex = new Indexable.Builder()
                .setName(TITLE)
                .setUrl(APP_URI)
                .build();

        Task<Void> task = FirebaseAppIndex.getInstance(this).update(articleToIndex);

        // If the Task is already complete, a call to the listener will be immediately
        // scheduled
        task.addOnSuccessListener(MainActivity.this, new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "App Indexing API: Successfully added " + TITLE + " to index");
            }
        });

        task.addOnFailureListener(MainActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e(TAG, "App Indexing API: Failed to add " + TITLE + " to index. " + exception
                        .getMessage());
            }
        });

        // log the view action
        Task<Void> actionTask = FirebaseUserActions.getInstance(this).start(Actions.newView(TITLE,
                APP_URI));

        actionTask.addOnSuccessListener(MainActivity.this, new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "App Indexing API: Successfully started view action on " + TITLE);
            }
        });

        actionTask.addOnFailureListener(MainActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e(TAG, "App Indexing API: Failed to start view action on " + TITLE + ". "
                        + exception.getMessage());
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();

        if (articleId == null) return;
        final Uri BASE_URL = Uri.parse("https://www.example.com/articles/");
        final String APP_URI = BASE_URL.buildUpon().appendPath(articleId).build().toString();

        Task<Void> actionTask = FirebaseUserActions.getInstance(this).end(Actions.newView(TITLE,
                APP_URI));

        actionTask.addOnSuccessListener(MainActivity.this, new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "App Indexing API: Successfully ended view action on " + TITLE);
            }
        });

        actionTask.addOnFailureListener(MainActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e(TAG, "App Indexing API: Failed to end view action on " + TITLE + ". "
                        + exception.getMessage());
            }
        });
    }
    // [END app_indexing_view]
}
