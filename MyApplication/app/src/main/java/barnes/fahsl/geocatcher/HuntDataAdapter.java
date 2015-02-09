package barnes.fahsl.geocatcher;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

/**
 * Created by fahslaj on 2/7/2015.
 */
public class HuntDataAdapter {

    // Becomes the filename of the database
    private static final String DATABASE_NAME = "hunts.db";
    // table name
    private static final String CHECKPOINT_CLUES_TABLE_NAME = "checkpoints_clues";
    // We increment this every time we change the database schema which will
    // kick off an automatic upgrade
    private static final int DATABASE_VERSION = 1;

    private SQLiteOpenHelper mOpenHelper;
    private SQLiteDatabase mDatabase;

    // checkpoints_clues db keys
    static final String KEY_CC_ID = "_id";
    static final String KEY_HUNT_NAME = "_hunt_name";
    static final String KEY_CC_NO = "_no";
    static final String KEY_CHECK_REACHED = "_check_reached";
    static final String KEY_CHECK_LAT = "_check_lat";
    static final String KEY_CHECK_LONG = "_check_long";
    static final String KEY_CLUE_TEXT = "_clue_text";
    static final String KEY_CLUE_PIC = "_clue_pic";
    static final String KEY_CLUE_SOUND = "_clue_sound";
    static final String KEY_CLUE_VIDEO = "_clue_video";

    public HuntDataAdapter(Context context) {
        mOpenHelper = new HuntDbHelper(context);
    }

    public void open() {
        mDatabase = mOpenHelper.getWritableDatabase();
    }

    public void close() {
        mDatabase.close();
    }

    public ArrayList<String> getAllHuntNames() {
        Cursor cursor = mDatabase.query(CHECKPOINT_CLUES_TABLE_NAME, null, null, null, null, null, KEY_CC_ID+" DESC");
        ArrayList<String> names = new ArrayList<String>();
        if (cursor == null || !cursor.moveToFirst()) {
            return null;
        }
        do {
            String name = getNameFromCursor(cursor);
            if (!names.contains(name))
                names.add(name);
        } while (cursor.moveToNext());
        return names;
    }

    public void addHunt(ScavengerHunt hunt) {
        for (Checkpoint p : hunt.getCheckpoints()) {
            assignURLToBMP(p);
            ContentValues row = getContentValuesFromCheckpoint(p, hunt.getName());
            long id = mDatabase.insert(CHECKPOINT_CLUES_TABLE_NAME, null, row);
            p.setCheckId(id);
        }
    }

    public void deleteHunt(ScavengerHunt hunt) {
        for (Checkpoint p : hunt.getCheckpoints()) {
            mDatabase.delete(CHECKPOINT_CLUES_TABLE_NAME, KEY_CC_ID + " = " + p.getCheckId(), null);
        }
    }

    public ScavengerHunt getHuntByName(String name) {
        Cursor cursor = mDatabase.query(CHECKPOINT_CLUES_TABLE_NAME, null, KEY_HUNT_NAME+" = '"+name+"'", null, null, null, KEY_CC_NO+" ASC");
        ArrayList<Checkpoint> checkpoints = new ArrayList<Checkpoint>();
        if (cursor == null || !cursor.moveToFirst()) {
            return null;
        }
        do {
            Checkpoint check = getCheckpointFromCursor(cursor);
            populateMediaFromURLs(check);
            checkpoints.add(check);
        } while (cursor.moveToNext());
        return new ScavengerHunt(name, checkpoints);
    }

    private ContentValues getContentValuesFromCheckpoint(Checkpoint check, String huntName) {
        ContentValues row = new ContentValues();
        row.put(KEY_HUNT_NAME, huntName);
        row.put(KEY_CC_NO, check.getCheckNo());
        row.put(KEY_CHECK_REACHED, check.hasBeenReached()?1:0);
        Location loc = check.getLocation();
        row.put(KEY_CHECK_LAT, loc.getLatitude());
        row.put(KEY_CHECK_LONG, loc.getLongitude());
        Checkpoint.Clue clue = check.getClue();
        row.put(KEY_CLUE_TEXT, clue.getText());
        row.put(KEY_CLUE_PIC, clue.getImageURL());
        row.put(KEY_CLUE_SOUND, clue.getSoundURL());
        row.put(KEY_CLUE_VIDEO, clue.getVideoURL());
        return row;
    }

    public Checkpoint getCheckpointFromCursor(Cursor cursor) {
        int checkNo = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_CC_NO));
        boolean reached = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_CHECK_REACHED))==1;
        double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_CHECK_LAT));
        double lon = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_CHECK_LONG));
        String text = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CLUE_TEXT));
        String imageURL = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CLUE_PIC));
        String soundURL = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CLUE_SOUND));
        String videoURL = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CLUE_VIDEO));
        Location loc = new Location(lat, lon);
        Checkpoint check = new Checkpoint(loc, checkNo);
        check.setClue(text, imageURL, soundURL, videoURL);
        check.setCheckId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_CC_ID)));
        return check;
    }

    private String getNameFromCursor(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(KEY_HUNT_NAME));
    }

    public void setAllHunts(ArrayList<ScavengerHunt> hunts) {
        String[] columns = {KEY_HUNT_NAME};
        Cursor cursor = mDatabase.query(CHECKPOINT_CLUES_TABLE_NAME, columns, null, null, null, null, KEY_HUNT_NAME+" DESC");
        if (cursor == null || !cursor.moveToFirst()) {
            return;
        }
        hunts.clear();
        String prevName = null;
        ArrayList<String> huntsCreated = new ArrayList<String>();
        ArrayList<Checkpoint> checkpoints = new ArrayList<Checkpoint>();
        checkpoints.add(getCheckpointFromCursor(cursor));
        prevName = getNameFromCursor(cursor);
        boolean b = cursor.moveToNext();
        while (b) {
            if (getNameFromCursor(cursor) == prevName) {
                checkpoints.add(getCheckpointFromCursor(cursor));
                b = cursor.moveToNext();
            } else {
                hunts.add(new ScavengerHunt(prevName, checkpoints));
                checkpoints = new ArrayList<Checkpoint>();
                prevName = getNameFromCursor(cursor);
            }
        }
        hunts.add(new ScavengerHunt(prevName, checkpoints));
    }

    public Cursor getCheckpointsCursor() {
        String[] projection = new String[] {KEY_CC_ID, KEY_HUNT_NAME, KEY_CC_NO, KEY_CHECK_REACHED,
                KEY_CHECK_LAT, KEY_CHECK_LONG, KEY_CLUE_TEXT, KEY_CLUE_PIC, KEY_CLUE_SOUND, KEY_CLUE_VIDEO };
        return mDatabase.query(CHECKPOINT_CLUES_TABLE_NAME, projection, null, null, null, null, KEY_CC_ID+" DESC");
    }

    private static class HuntDbHelper extends SQLiteOpenHelper {

        private static final String CREATE_STATEMENT;
        static {
            StringBuilder sb = new StringBuilder();
            sb.append("CREATE TABLE "+CHECKPOINT_CLUES_TABLE_NAME+"(");
            sb.append(KEY_CC_ID + " integer primary key autoincrement, ");
            sb.append(KEY_HUNT_NAME + " text, ");
            sb.append(KEY_CC_NO + " integer, ");
            sb.append(KEY_CHECK_REACHED + " integer, ");
            sb.append(KEY_CHECK_LAT + " double, ");
            sb.append(KEY_CHECK_LONG + " double, ");
            sb.append(KEY_CLUE_TEXT + " text, ");
            sb.append(KEY_CLUE_PIC + " text, ");
            sb.append(KEY_CLUE_SOUND + " text, ");
            sb.append(KEY_CLUE_VIDEO + " text");
            sb.append(");");
            CREATE_STATEMENT = sb.toString();
        }
        private static final String DROP_STATEMENT = "DROP TABLE IF EXISTS "+CHECKPOINT_CLUES_TABLE_NAME;

        public HuntDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_STATEMENT);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d("SLS", "Upgrading the database from version "+
                    oldVersion+" to "+newVersion+
                    " will destroy all your data.");
            db.execSQL(DROP_STATEMENT);
            db.execSQL(CREATE_STATEMENT);
        }
    }

    private void populateMediaFromURLs(Checkpoint check) {
        new PopulateMediaTask().execute(check);
//        Bitmap returnedBitmap;
//        Checkpoint currentCheck = check;
//        try {
//            URL url = new URL("http://fahsl-barnes-geocatcher.appspot.com/serve/AMIfv9751n_1Bnig5JgkR7U0wI3I05fzZeCs_Fi7wk_TvnIthyBNtpsdIk9YMoM2Z6DxVy7HPrMdFvsrnxoocavPxeq4iHXGCZd-eos5fV4x9B9s6WJKFRTKd4rxdjS_ZXZx7VS5SB7dwyM8bnkVFYcuovKLmDpPkuto4cxVS4GmM1hu-asOz_A");//currentCheck.getClue().getImageURL());
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            connection.setDoInput(true);
//            connection.connect();
//            InputStream input = connection.getInputStream();
//            returnedBitmap = BitmapFactory.decodeStream(input);
//            check.getClue().setImage(returnedBitmap);
//        } catch (IOException e) {
//            Log.d("FAHSL", e.getStackTrace().toString());
//        }
    }

    private class PopulateMediaTask extends AsyncTask<Checkpoint, Void, Bitmap> {
        private Checkpoint currentCheck;

        @Override
        protected Bitmap doInBackground(Checkpoint... checks) {
            Bitmap returnedBitmap;
            currentCheck = checks[0];
            try {
                URL url = new URL(currentCheck.getClue().getImageURL());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                returnedBitmap = BitmapFactory.decodeStream(input);
                return returnedBitmap;
            } catch (IOException e) {
                Log.d("FAHSL", e.getStackTrace().toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if (result == null) {
                Log.d("FAHSL", "Failed loading image");
                return;
            }
            currentCheck.getClue().setImage(result);
        }
    }

    private void assignURLToBMP(Checkpoint check) {
//        try {
//            HttpClient httpClient = new DefaultHttpClient();
//            HttpGet httpGet = new HttpGet("http://fahsl-barnes-geocatcher.appspot.com/upload");
//            HttpResponse response = httpClient.execute(httpGet);
//            HttpEntity urlEntity = response.getEntity();
//            InputStream in = urlEntity.getContent();
//            String str = "";
//            while (true) {
//                int ch = in.read();
//                if (ch == -1)
//                    break;
//                str += (char) ch;
//            }
//        } catch (Exception e) {
//            Log.d("FAHSL", e.getStackTrace().toString());
//        }

//        Bitmap bmp = check.getClue().getImage();
//        HttpURLConnection connection = null;
//        DataOutputStream outputStream = null;
////        DataInputStream inputStream = null;
//        String pathToOurFile = "/data/file_to_send.mp3";
//        String urlServer = "http://fahsl-barnes-geocatcher.appspot.com/_ah/upload/";
//        //String urlServer = "http://fahsl-barnes-geocatcher.appspot.com/";
//        String lineEnd = "\r\n";
//        String twoHyphens = "--";
//        String boundary =  "*****";
//
//        int bytesRead, bytesAvailable, bufferSize;
//        byte[] buffer;
//        int maxBufferSize = 1*1024*1024;
//
//        try
//        {
//            FileInputStream fileInputStream = new FileInputStream(new File(pathToOurFile) );
//
//            URL url = new URL(urlServer);
//            connection = (HttpURLConnection) url.openConnection();
//
//            // Allow Inputs &amp; Outputs.
//            connection.setDoInput(true);
//            connection.setDoOutput(true);
//            connection.setUseCaches(false);
//
//            // Set HTTP method to POST.
//            connection.setRequestMethod("POST");
//
//            connection.setRequestProperty("Connection", "Keep-Alive");
//            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
//
//            outputStream = new DataOutputStream( connection.getOutputStream() );
//            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
//            outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + pathToOurFile +"\"" + lineEnd);
//            outputStream.writeBytes(lineEnd);
//
//            bytesAvailable = fileInputStream.available();
//            bufferSize = Math.min(bytesAvailable, maxBufferSize);
//            buffer = new byte[bufferSize];
//
////            // Read file
////            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//
//            ByteArrayOutputStream bos = new ByteArrayOutputStream();
//            bmp.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
//            byte[] bitmapdata = bos.toByteArray();
//            ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
//
//            bytesRead = bs.read(buffer, 0, bufferSize);
//
//            while (bytesRead > 0)
//            {
//                outputStream.write(buffer, 0, bufferSize);
//                bytesAvailable = bs.available();
//                bufferSize = Math.min(bytesAvailable, maxBufferSize);
//                bytesRead = bs.read(buffer, 0, bufferSize);
//            }
//
//            outputStream.writeBytes(lineEnd);
//            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
//
//            // Responses from the server (code and message)
//            int serverResponseCode = connection.getResponseCode();
//            String serverResponseMessage = connection.getResponseMessage();
//            //Log.d("FAHSL", "URL: "+serverResponseMessage);
//            check.getClue().setImageURL(serverResponseMessage);
//            fileInputStream.close();
//            outputStream.flush();
//            outputStream.close();
//        }
//        catch (Exception ex)
//        {
//            //Exception handling
//            Log.d("FAHSL", ex.getStackTrace().toString());
//        }
    }
}

