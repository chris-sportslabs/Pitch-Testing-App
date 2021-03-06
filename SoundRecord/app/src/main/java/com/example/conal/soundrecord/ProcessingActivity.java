// AUTHORS - This code was written by Artem Usov, 2296905u@student.gla.ac.uk, Olga Jodelka 2266755K@student.gla.ac.uk
// Kara Newlands 2264457n@student.gla.ac.uk, Conall Clements 2197397c@student.gla.ac.uk
// It is licensed under the GNU General Public License version 3, 29 June 2007 (https://www.gnu.org/licenses/gpl-3.0.en.html)

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
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import be.tarsos.dsp.io.android.AndroidFFMPEGLocator;

import static com.example.conal.soundrecord.HomeActivity.MyPREFERENCES;
import static com.example.conal.soundrecord.MapsActivity.TEST;
import static com.example.conal.soundrecord.RecordingActivity.PATH;
import static com.example.conal.soundrecord.RecordingActivity.FOLDER;

public class ProcessingActivity extends AppCompatActivity {

    private static final int SAMPLERATE = 44100;
    private static final int OVERLAP = 2048;
    private Intent intent;
    private Result result;
    private PitchTest test;
    private AsyncTask<String, Void, Result> runner;
    public static double[] sound;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing);

        final ProgressBar spinner;
        spinner = findViewById(R.id.progressBar1);
        spinner.setVisibility(View.VISIBLE);

        intent = getIntent();

        test = intent.getParcelableExtra(TEST);
        String path = intent.getStringExtra(PATH);

        // load the ffmpeg library, then run processing on file
        // alongside current activity
        new AndroidFFMPEGLocator(this);
        runner = new AsyncRunner().execute(path);

        // time is super long(5min) to be safe, but timer cancels early whenever processing is done
        new CountDownTimer(500000, 1000) {

            public void onTick(long millisUntilFinished) {
                if (runner.getStatus() == Status.FINISHED) {
                    afterProcessing();
                    this.cancel();
                }
            }

            // realistically never executes as we will finish early
            public void onFinish() {
                spinner.setVisibility(View.GONE);
                afterProcessing();
            }
        }.start();
    }

    // stop user from being able to go back, i.e. does nothing
    @Override
    public void onBackPressed() {
    }

    private void afterProcessing() {
        File folderRecordings = (File) intent.getSerializableExtra(FOLDER);
        try {
            FileUtils.deleteDirectory(folderRecordings);
            Log.i("Processing", "Folder is deleted.");
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("Processing", "No folder to delete.");
        }

        if (result != null) {
            // add result to current location, current drop
            test.getLocation(test.getNumDone()).addResult(result);

            Log.i("Processing", "bounceHeight: " + result.getBounceHeight());

            // if this is our last drop for a location, toggle that we 
            // want to show map again
            if (test.getLocation(test.getNumDone()).getNumDone() == 4) {
                SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                final SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean("mapNeeded", true);
                editor.apply();
            }
            openResultsActivity();
        } else {
            // if processing not successful, try again
            Log.i("Processing", "bounceTime was 0");
            Toast.makeText(this, "Failed, try again", Toast.LENGTH_LONG).show();
            openRecordingActivity();
        }
    }


    private class AsyncRunner extends AsyncTask<String, Void, Result> {
        private SoundProcessing processor;

        // we want to do processing in background so doesn't pause
        // the current UI thread
        protected Result doInBackground(String... strings) {
            Log.i("Recordings", "In Runner");
            return processor.process(strings[0]);
        }

        protected void onPreExecute() {
            processor = new SoundProcessing(SAMPLERATE, OVERLAP);
        }

        protected void onPostExecute(Result returnResult) {
            if (returnResult != null) {
                sound = processor.getSoundArray();
                result = returnResult;
                result.setBounceHeight(1.23 * Math.pow((result.getTimeOfBounce() - 0.025), 2.0));
            }
        }
    }

    private void openResultsActivity() {
        intent = getIntent();

        intent.putExtra(TEST, test);

        intent.setClass(this, ResultsActivity.class);
        startActivity(intent);
    }

    private void openRecordingActivity() {
        intent = getIntent();

        intent.setClass(this, RecordingActivity.class);
        startActivity(intent);
    }

}
