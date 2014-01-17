package edu.umd.bfruin.mapwindowrecorder;

import android.app.AlertDialog;
import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends ActionBarActivity implements GoogleMap.OnCameraChangeListener {
    private static final String TAG = "edu.umd.bfruin.mapwindowrecorder";

    private static final String WINDOWS_FILENAME = "map_windows";

    public static final int NUM_WINDOWS = 50; // Number of map windows to record
    public static final int PLAY_DELAY = 1500; // Delay between playing windows in milliseconds

    private GoogleMap mMap;
    private ArrayList<LatLngBoundsWrapper> mMapWindows;

    private TextView mWindowTextView;

    private boolean mIsRecording;
    private boolean mIsPlaying;
    private int mWindowNum;

    // ================================================================================
    // Activity Lifecycle
    // ================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        mMapWindows = new ArrayList<LatLngBoundsWrapper>();
        mIsRecording = false;
        mWindowNum = 1;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(false);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mMap == null) {
            mMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();
            mMap.setOnCameraChangeListener(this);
        }

        mWindowTextView = (TextView) findViewById(R.id.window_label);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_record) {
            beginRecording();
            return true;
        } else if (id == R.id.action_play) {
            beginPlaying();
            return true;
        } else if (id == R.id.action_save) {
            saveMapWindowsToFile();
            return true;
        } else if (id == R.id.action_load) {
            mMapWindows = loadMapWindowsFromFile();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

    // ================================================================================
    // Map Recording / Playing
    // ================================================================================
    private void beginRecording() {
        mIsPlaying = false;

        // Clear current map windows and add current map window
        if (mMapWindows == null) {
            mMapWindows = new ArrayList<LatLngBoundsWrapper>();
        }
        mMapWindows.clear();
        mMapWindows.add(new LatLngBoundsWrapper(mMap.getProjection().getVisibleRegion().latLngBounds));

        // Set TextView Value Denoting Current Window
        mWindowTextView.setText("1 / " + NUM_WINDOWS);
        mWindowTextView.setVisibility(View.VISIBLE);

        // Reset window counter
        mWindowNum = 1;
        mIsRecording = true;
    }

    private void beginPlaying() {
        mIsRecording = false;

        // Make sure that there are currently map windows
        if (mMapWindows == null || mMapWindows.size() == 0) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setMessage("No map windows found. Map windows must be recorded or loaded.");
            alert.setTitle("Playback Failed");
            alert.setPositiveButton("OK", null);
            alert.show();
        } else {
            mIsPlaying = true;
            mWindowNum = 1;

            CameraUpdate firstWindow = CameraUpdateFactory.newLatLngBounds(mMapWindows.get(0).getLatLngBounds(), 0);
            mMap.moveCamera(firstWindow);
            mWindowTextView.setText("1 / " + mMapWindows.size());
            mWindowTextView.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onCameraChange(CameraPosition position) {
        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;

        if (mIsRecording) {
            mWindowNum++;

            if (mWindowNum <= NUM_WINDOWS) {
                mMapWindows.add(new LatLngBoundsWrapper(bounds));
                mWindowTextView.setText(mWindowNum + " / " + NUM_WINDOWS);

                if (mWindowNum == NUM_WINDOWS) { // Finished Recording
                    mWindowNum = 1;
                    mIsRecording = false;

                    AlertDialog.Builder alert = new AlertDialog.Builder(this);
                    alert.setMessage(NUM_WINDOWS + " map windows have been recorded. Press Play to replay them or " +
                            "Save to commit to device.");
                    alert.setTitle("Recording Completed");
                    alert.setPositiveButton("OK", null);
                    alert.show();

                    mWindowTextView.setVisibility(View.GONE);
                }
            }
        } else if (mIsPlaying) {
            mWindowNum++;
            if (mWindowNum - 1 < mMapWindows.size()) {
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    public void run() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                if (mIsPlaying) {
                                    CameraUpdate firstWindow = CameraUpdateFactory.newLatLngBounds(mMapWindows.get(mWindowNum - 1).getLatLngBounds(), 0);
                                    mMap.moveCamera(firstWindow);
                                    mWindowTextView.setText(mWindowNum + " / " + mMapWindows.size());
                                    mWindowTextView.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                }, PLAY_DELAY);
            } else {
                mWindowNum = 1;
                mIsPlaying = false;

                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setMessage("Finished playing map windows.");
                alert.setTitle("Play Completed");
                alert.setPositiveButton("OK", null);
                alert.show();

                mWindowTextView.setVisibility(View.GONE);
            }
        }
    }

    // ================================================================================
    // Save / Load Map Windows
    // ================================================================================
    private void saveMapWindowsToFile() {


        if (mMapWindows == null || mMapWindows.size() == 0) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setMessage("There are currently no map windows to save.");
            alert.setTitle("Save Unsuccessful");
            alert.setPositiveButton("OK", null);
            alert.show();
        } else {

        FileOutputStream fos = null;
        ObjectOutputStream oos = null;

        try {
            fos = openFileOutput(WINDOWS_FILENAME, Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(mMapWindows);
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setMessage("Saved current map windows to device.");
            alert.setTitle("Save Successful");
            alert.setPositiveButton("OK", null);
            alert.show();
        } catch (Exception e) {
            Log.e(TAG, "failed to write query records: ", e);
        } finally {
            try {
                if (oos != null) oos.close();
                if (fos != null) fos.close();
            } catch (Exception e) {
            }
        }
        }
    }

    private ArrayList<LatLngBoundsWrapper> loadMapWindowsFromFile() {
        mIsPlaying = false;
        mIsRecording = false;
        mWindowTextView.setVisibility(View.GONE);

        ArrayList<LatLngBoundsWrapper> mapWindows = new ArrayList<LatLngBoundsWrapper>();
        FileInputStream fis = null;
        ObjectInputStream ois = null;

        try {
            fis = openFileInput(WINDOWS_FILENAME);
            ois = new ObjectInputStream(fis);
            mapWindows = (ArrayList<LatLngBoundsWrapper>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) fis.close();
                if (ois != null) ois.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (mapWindows == null || mapWindows.size() == 0) {
            mapWindows = new ArrayList<LatLngBoundsWrapper>();

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setMessage("There are no map windows saved to device.");
            alert.setTitle("Load Unsuccessful");
            alert.setPositiveButton("OK", null);
            alert.show();
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setMessage("Loaded " + mapWindows.size() + " map windows from device.");
            alert.setTitle("Load Successful");
            alert.setPositiveButton("OK", null);
            alert.show();
        }

        return mapWindows;
    }
}


