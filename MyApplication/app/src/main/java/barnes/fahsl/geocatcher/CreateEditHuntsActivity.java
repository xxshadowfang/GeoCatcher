package barnes.fahsl.geocatcher;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.*;
import android.location.Location;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.content.CursorLoader;
import com.cengalabs.flatui.FlatUI;
import com.cengalabs.flatui.views.FlatButton;
import com.cengalabs.flatui.views.FlatEditText;
import android.media.MediaPlayer;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class CreateEditHuntsActivity extends ActionBarActivity {

    private ArrayAdapter<String> arrayAdapter;
    private HuntDataAdapter hda;

    private ArrayList<Checkpoint> checkpoints;
    private ScavengerHunt thisHunt;
    private String name;
    ImageView imgFavorite;
    private Bitmap img;

    private barnes.fahsl.geocatcher.Location recordedLoc;
    private double currentLat = 5000;
    private double currentLong = 5000;

    private boolean isNew;

    private static final String LOG_TAG = "AudioRecordTest";
    private static String mFileName ;

    private RecordButton mRecordButton = null;
    private MediaRecorder mRecorder = null;

    private PlayButton   mPlayButton = null;
    private MediaPlayer   mPlayer = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_edit_hunts);
        try{
            mFileName = getDataDir(this);
        }
        catch(Exception e){
            Log.d("file","getDir failed" );
        }
        FlatUI.initDefaultValues(this);
        FlatUI.setDefaultTheme(FlatUI.GRASS);
        getSupportActionBar().setBackgroundDrawable(FlatUI.getActionBarDrawable(this,FlatUI.GRASS,false));
        mRecordButton = new RecordButton(this);
        mPlayButton = new PlayButton(this);
        hda = new HuntDataAdapter(this);
        hda.open();
        Spinner checkpointSpinner = (Spinner)findViewById(R.id.checkpointsSpinner);

        isNew = getIntent().getBooleanExtra(GeoCatcherMain.KEY_NEW_HUNT, true);
        String[] array;
        if(isNew) {
            checkpoints = new ArrayList<Checkpoint>();
            array = new String[1];
            array[0] = "New Checkpoint";
        }
        else {
            name = getIntent().getStringExtra(GeoCatcherMain.KEY_HUNT_NAME);
            thisHunt = hda.getHuntByName(name); // Load previous hunt data
            checkpoints = thisHunt.getCheckpoints();
            array = new String[checkpoints.size()+1];
            for (int i = 1; i < checkpoints.size()+1; i++)
                array[i-1] = "Checkpoint "+i;
            array[checkpoints.size()] = "New Checkpoint";
        }


        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, array);

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        checkpointSpinner.setAdapter(arrayAdapter);
        checkpointSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadCheckpointValues(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);





        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLat = location.getLatitude();
                currentLong = location.getLongitude();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };



        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, locationListener);




        final FlatButton recordSound = (FlatButton)findViewById(R.id.recordSoundButton);
        recordSound.setEnabled(false);
        recordSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                recordSound.setVisibility(Button.INVISIBLE);
//                LinearLayout recLayout = (LinearLayout)findViewById(R.id.SoundLayout);
//                recLayout.addView(mRecordButton);
//                recLayout.addView(mPlayButton);

            }
        });

        FlatButton takePic = (FlatButton)findViewById(R.id.takePictureButton);
        imgFavorite = (ImageView)findViewById(R.id.clue_image_view);
        takePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });
        FlatButton returnFlatButton = (FlatButton)findViewById(R.id.finishButton);
        returnFlatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFinish();
            }
        });
        FlatButton recordLocFlatButton = (FlatButton)findViewById(R.id.recordLocationButton);
        recordLocFlatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLat == 5000 && currentLong == 5000 ) {
                    Toast.makeText(getApplicationContext(), getString(R.string.please_wait_for_gps), Toast.LENGTH_SHORT).show();
                    return;
                }
                recordedLoc = new barnes.fahsl.geocatcher.Location(currentLat, currentLong);
                Toast.makeText(getApplicationContext(),getString(R.string.successful_location),Toast.LENGTH_SHORT).show();
            }
        });
        FlatButton saveCheckpointFlatButton = (FlatButton)findViewById(R.id.saveCheckpointButton);
        saveCheckpointFlatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recordedLoc == null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.please_record_location), Toast.LENGTH_SHORT).show();
                    return;
                }
                Spinner checkpointSpinner = (Spinner) findViewById(R.id.checkpointsSpinner);
                int selectedCheckpointIndex = checkpointSpinner.getSelectedItemPosition();
                String selectedCheckpointText = ((TextView)checkpointSpinner.getSelectedView()).getText().toString();
                if (selectedCheckpointText.equals("New Checkpoint")) {
                    Checkpoint newCheckpoint = new Checkpoint(recordedLoc, checkpoints.size() + 1);
                    String text = ((FlatEditText) (CreateEditHuntsActivity.this.findViewById(R.id.hint_text_box))).getText().toString();
                    text = text.replace("|", "");
                    text = text.replace(";", "");
                    newCheckpoint.setClue(text, null, null, null);
                    newCheckpoint.getClue().setImage(img);
                    checkpoints.add(newCheckpoint);
                    String[] array = new String[checkpoints.size() + 1];
                    for (int i = 1; i < checkpoints.size() + 1; i++)
                        array[i - 1] = "Checkpoint " + i;
                    array[checkpoints.size()] = "New Checkpoint";
                    arrayAdapter = new ArrayAdapter<String>(CreateEditHuntsActivity.this, android.R.layout.simple_spinner_item, array);
                    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    checkpointSpinner.setAdapter(arrayAdapter);
                    checkpointSpinner.setSelection(checkpoints.size());
                } else {
                    Checkpoint editCheckpoint = checkpoints.get(selectedCheckpointIndex);
                    editCheckpoint.setLocation(recordedLoc);
                    String text = ((EditText) (CreateEditHuntsActivity.this.findViewById(R.id.hint_text_box))).getText().toString();
                    text = text.replace("|", "");
                    text = text.replace(";", "");
                    editCheckpoint.setClue(text, null, null, null);
                    editCheckpoint.getClue().setImage(img);
                    File soundFile = new File(mFileName);

                    editCheckpoint.getClue().setSound(soundFile);
                }
            }
        });
    }

    private void loadCheckpointValues(int position) {
//        Log.d("FAHSL", "Pos: " + position);
//        Log.d("FAHSL", "Size: "+checkpoints.size());
        if (position >= checkpoints.size()) {
            ((FlatEditText)findViewById(R.id.hint_text_box)).setText("");
            ((ImageView)findViewById(R.id.clue_image_view)).setImageResource(android.R.color.transparent);
            recordedLoc = null;
            img = null;
            return;
        }
        Checkpoint currentCheckpoint = checkpoints.get(position);
        ((FlatEditText)findViewById(R.id.hint_text_box)).setText(currentCheckpoint.getClue().getText());
        ((ImageView)findViewById(R.id.clue_image_view)).setImageBitmap(currentCheckpoint.getClue().getImage());
        recordedLoc = currentCheckpoint.getLocation();
    }

    private void showFinish() {
        DialogFragment df = new DialogFragment(){
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState){
                AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                final View view  = inflater.inflate(R.layout.dialog_fragment_layout, null);
                builder.setView(view);
                final EditText input = (EditText)view.findViewById(R.id.nameHuntEdit);
                if (!isNew)
                    input.setText(thisHunt.getName());
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        name =input.getText().toString();
                        name.replace("|", "");
                        name.replace(";", "");
                        if (isNew) {
                            ScavengerHunt newHunt = new ScavengerHunt(name, checkpoints);
                            hda.addHunt(newHunt);
                        } else {
                            ScavengerHunt editedHunt = new ScavengerHunt(name, checkpoints);
                            hda.deleteHunt(thisHunt);
                            hda.addHunt(editedHunt);
                        }

                        Intent finishIntent = new Intent(getApplicationContext(), GeoCatcherMain.class);
                        Toast.makeText(getApplicationContext(), "Successfully created hunt: "+name, Toast.LENGTH_SHORT).show();
                        startActivity(finishIntent);
                    }
                });
                builder.setMessage(R.string.Name_your_Hunt);
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                return builder.create();
            }
        };
        df.show(getFragmentManager(), "exit");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hda.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_edit_hunts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void openCamera(){
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 0);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data.hasExtra("data")) {
            img = (Bitmap) data.getExtras().get("data");
            imgFavorite.setImageBitmap(img);
        }
    }
    class RecordButton extends FlatButton {
        boolean mStartRecording = true;

        @Override
        public void setTextColor(int color) {
            super.setTextColor(color);
        }

        @Override
        public void setBackgroundColor(int color) {
            super.setBackgroundColor(color);
        }


        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    setText("Stop recording");
                } else {
                    setText("Start recording");
                }
                mStartRecording = !mStartRecording;
            }
        };

        public RecordButton(Context ctx) {
            super(ctx);
            setText("Start recording");
            setOnClickListener(clicker);
        }
    }

    class PlayButton extends FlatButton {
        boolean mStartPlaying = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    setText("Stop playing");
                } else {
                    setText("Start playing");
                }
                mStartPlaying = !mStartPlaying;
            }
        };

        public PlayButton(Context ctx) {
            super(ctx);
            setText("Start playing");
            setOnClickListener(clicker);
        }
    }
    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }
    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }
    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        try{
            mFileName = getDataDir(this);
        }
        catch(Exception e){
            Log.d("file","getDir failed" );
        }
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);


        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, mFileName);
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;

    }
    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }
    public String getDataDir(Context context) throws Exception
    {
        return Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator
                + Environment.DIRECTORY_DCIM

                + File.separator
                + System.currentTimeMillis() + ".mp4";
    }

}
