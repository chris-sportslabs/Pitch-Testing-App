package com.example.conal.soundrecord;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import org.apache.commons.io.FileUtils;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.IOException;

import be.tarsos.dsp.io.android.AndroidFFMPEGLocator;

import static com.example.conal.soundrecord.HomeActivity.MyPREFERENCES;

public class ProcessingActivity extends AppCompatActivity {
    //Declare variables
    public static final String LOCATION = "com.example.conal.soundrecord.LOCATION";
    public static final String SOUND = "com.example.conal.soundrecord.SOUND";
    Intent intent;
    Result result;
    Location loc;
    AsyncTask<String, Void, Result> runner;
    double[] sound;
    SharedPreferences sharedPreferences;
    private boolean mapNeeded;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing);

        final ProgressBar spinner;
        spinner = findViewById(R.id.progressBar1);
        spinner.setVisibility(View.VISIBLE);

        intent = getIntent();

        LatLng l = intent.getParcelableExtra(RecordingActivity.POSITION);
        String path = intent.getStringExtra(RecordingActivity.PATH);

        loc = new Location(l);

        new AndroidFFMPEGLocator(this);
        runner = new AsyncRunner().execute(path);

        intent.setClass(this, ResultsActivity.class);

        // time is super long(5min), but timer cancels early whenever processing is done
        new CountDownTimer(500000, 1000) {

            public void onTick(long millisUntilFinished) {
                if (runner.getStatus() == Status.FINISHED){
                    afterProcessing();
                    this.cancel();
                }
            }

            // realistically never executes
            public void onFinish() {
                spinner.setVisibility(View.GONE);
                afterProcessing();
            }
        }.start();
    }

    private void afterProcessing() {
        if (result != null) {
            loc.addResult(result);
            Log.i("Recordings", "bounceHeight: " + result.bounceHeight);

            if(loc.getResults().size() == 5){
                SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                final SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean("mapNeeded", true);

            }

            intent.putExtra(LOCATION, loc);
            intent.putExtra(SOUND, sound);
        } else {
            Log.i("Recordings", "bounceTime was 0");
            intent.setClass(this, RecordingActivity.class);
        }

        File folderRecordings = (File) intent.getSerializableExtra(MapsActivity.FOLDER);
        try {
            FileUtils.deleteDirectory(folderRecordings);
            Log.i("Recordings", "Folder is deleted.");
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("Recordings", "No folder to delete.");
        }

        startActivity(intent);
    }


    private class AsyncRunner extends AsyncTask<String, Void, Result> {
        SoundProcessing processor;

        protected Result doInBackground(String... strings) {
            Log.i("Recordings", "In Runner");
            return processor.process(strings[0]);
        }

        protected void onPreExecute() {
            processor = new SoundProcessing(44100, 2048);
        }

        protected void onPostExecute(Result returnResult) {
            if (returnResult != null) {
                sound = processor.soundArray;
                result = returnResult;
                result.setBounceHeight(1.23 * Math.pow((result.timeOfBounce - 0.025), 2.0));
            } else {
                Log.i("Recordings", "returnResult is null");
            }
        }
    }

    public void openResultsActivity(Intent intent){
        //Intent intent = new Intent(this, ResultsActivity.class);
        startActivity(intent);
    }

}
