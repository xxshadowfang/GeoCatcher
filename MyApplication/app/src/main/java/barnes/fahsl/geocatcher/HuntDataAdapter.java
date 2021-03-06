package barnes.fahsl.geocatcher;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
import org.apache.http.impl.client.DefaultHttpClient;

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
        Cursor cursor = mDatabase.query(CHECKPOINT_CLUES_TABLE_NAME, null, null, null, null, null, KEY_CC_ID + " DESC");
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

    public void executeStatements(String stringy) {
        String[] checkReps = stringy.split("\\|");
        String name = "";
        ArrayList<Checkpoint> checks = new ArrayList<Checkpoint>();
        for (String q : checkReps)
            Log.d("FAHSL", q);
        for (int i = 0; i < checkReps.length; i++) {
            String s = checkReps[i];
            if (i != 0) {
                String[] params = s.split(";");
                int checkNo = Integer.parseInt(params[0]);
                boolean reached = params[1].equals("1");
                double lat = Double.parseDouble(params[2]);
                double lon = Double.parseDouble(params[3]);
                String text = params[4];
                String imageURL = params[5].equals("null")?null:params[5];
                String soundURL = params[6].equals("null")?null:params[6];
                String videoURL = params[7].equals("null")?null:params[7];
                Checkpoint newCheck = new Checkpoint(new Location(lat, lon), checkNo);
                newCheck.setReached(reached);
                newCheck.setClue(text, imageURL, soundURL, videoURL);
                checks.add(newCheck);
            } else {
                name = s;
            }
        }
        ScavengerHunt newHunt = new ScavengerHunt(name, checks);
        this.addHunt(newHunt);
    }

    public String generateStringForHunt(String huntName) {
        String query = "";
        ScavengerHunt huntToSend = getHuntByName(huntName);
        query+= huntToSend.getName()+"|";
        for (int i = 0; i < huntToSend.getCheckpoints().size(); i++) {
            Checkpoint check = huntToSend.getCheckpoints().get(i);
            query+=check.getCheckNo()+";"+check.hasBeenReached()+";"+check.getLocation().getLatitude()+";";
            query+=check.getLocation().getLongitude()+";"+check.getClue().getText()+";"+check.getClue().getImageURL()+";";
            query+=check.getClue().getSoundURL()+";"+check.getClue().getVideoURL();

            if (i != huntToSend.getCheckpoints().size()-1) {
                query += "|";
            }
//            tried sending raw query, didn't work. (We know, it was a terribly insecure way!)
//            query += "INSERT INTO " + CHECKPOINT_CLUES_TABLE_NAME + " (";
//            query += KEY_HUNT_NAME + "," + KEY_CC_NO + "," + KEY_CHECK_REACHED + ",";
//            query += KEY_CHECK_LAT + "," + KEY_CHECK_LONG + "," + KEY_CLUE_TEXT + ",";
//            query += KEY_CLUE_PIC + "," + KEY_CLUE_SOUND + "," + KEY_CLUE_VIDEO + ") ";
//            query += "VALUES ('";
//            query += huntToSend.getName() + "',";
//            query += check.getCheckNo()+","+(check.hasBeenReached()?"1":"0")+",";
//            query += check.getLocation().getLatitude()+","+check.getLocation().getLongitude()+",";
//            query += "'"+check.getClue().getText()+"','"+check.getClue().getImageURL()+"','";
//            query += check.getClue().getSoundURL()+"','"+check.getClue().getVideoURL()+"')";
        }
        return query;
    }

    public void addHunt(ScavengerHunt hunt) {

        int counter = 0;
        for (Checkpoint p : hunt.getCheckpoints()) {
            boolean needsAssignment = false;
            boolean needsSoundAssignment = false;
            if (p.getClue().getImage() != null && p.getClue().getImageURL() == null) {
                Log.d("FAHSL", "Has image, assigning URL");
                needsAssignment = true;
            }
            if (p.getClue().getSound() != null && p.getClue().getSoundURL() == null) {
                Log.d("FAHSL", "Has sound, assigning URL");
                needsAssignment = true;
            }
            new AddCheckpointTask().execute(p, needsAssignment,needsSoundAssignment, hunt.getName(), counter);
            counter++;
        }
    }

    private class AddCheckpointTask extends AsyncTask<Object, Void, Void> {

        String name;
        Checkpoint check;

        @Override
        protected Void doInBackground(Object... params) {
            check = (Checkpoint)params[0];
            boolean assign = (Boolean)params[1];
            boolean assignSound = (Boolean)params[2];
            name = (String)params[4];
            int counter = (Integer)params[4];
            if (assign) {
                assignURLToBMP(check, counter);
            } else {
                // dont do image stuff
            }
            if(assignSound){
                assignURLToMPEG(check,counter);
            }
            return null;
        }

        private void assignURLToMPEG(Checkpoint check, int num) {
            assignURLToFile(check, num, ".mp4");
        }

        private void assignURLToBMP(Checkpoint check, int num) {
            assignURLToFile(check, num, ".png");
        }

        private void assignURLToFile(Checkpoint check, int num, String fileExtension) {
            String filename = name+""+num+""+fileExtension;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            check.getClue().getImage().compress(Bitmap.CompressFormat.PNG, 0, bos);
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());

            String requestURL = "";
            String baseGetUrl = "http://fahsl-barnes-geocatcher.appspot.com/";
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(baseGetUrl);
                HttpResponse response = httpClient.execute(httpGet);
                HttpEntity urlEntity = response.getEntity();
                InputStream in = urlEntity.getContent();
                while (true) {
                    int ch = in.read();
                    if (ch == -1)
                        break;
                    requestURL += (char) ch;
                }
            } catch (Exception e) {
                Log.d("FAHSL", e.getStackTrace().toString());
            }

            String charset = "UTF-8";
            String responseStr = "";
            try {
                MultipartUtility multipart = new MultipartUtility(requestURL, charset);

                multipart.addFilePartFromStream("file", filename, bis);

                List<String> response = multipart.finish();

                //System.out.println("SERVER REPLIED:");

                for (String line : response) {
                    responseStr += line;
                }
            } catch (IOException ex) {
                System.err.println(ex);
            }
            check.getClue().setImageURL(baseGetUrl+"serve/"+responseStr);
            Log.d("FAHSL", baseGetUrl+"serve/"+responseStr);
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            ContentValues row = getContentValuesFromCheckpoint(check, name);
            long id = mDatabase.insert(CHECKPOINT_CLUES_TABLE_NAME, null, row);
            check.setCheckId(id);
        }
    }

    public void deleteHunt(ScavengerHunt hunt) {
        for (Checkpoint p : hunt.getCheckpoints()) {
            mDatabase.delete(CHECKPOINT_CLUES_TABLE_NAME, KEY_CC_ID + " = " + p.getCheckId(), null);
        }
    }

    public ScavengerHunt getHuntByName(String name) {
        Cursor cursor = mDatabase.query(CHECKPOINT_CLUES_TABLE_NAME, null, KEY_HUNT_NAME + " = '" + name + "'", null, null, null, KEY_CC_NO + " ASC");
        ArrayList<Checkpoint> checkpoints = new ArrayList<Checkpoint>();
        if (cursor == null || !cursor.moveToFirst()) {
            return null;
        }
        do {
            checkpoints.add(getCheckpointFromCursor(cursor));
        } while (cursor.moveToNext());
        return new ScavengerHunt(name, checkpoints);
    }

    private ContentValues getContentValuesFromCheckpoint(Checkpoint check, String huntName) {
        ContentValues row = new ContentValues();
        row.put(KEY_HUNT_NAME, huntName);
        row.put(KEY_CC_NO, check.getCheckNo());
        row.put(KEY_CHECK_REACHED, check.hasBeenReached() ? 1 : 0);
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
        boolean reached = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_CHECK_REACHED)) == 1;
        double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_CHECK_LAT));
        double lon = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_CHECK_LONG));
        String text = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CLUE_TEXT));
        String imageURL = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CLUE_PIC));
        String soundURL = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CLUE_SOUND));
        String videoURL = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CLUE_VIDEO));
        Location loc = new Location(lat, lon);
        Checkpoint check = new Checkpoint(loc, checkNo);
        check.setReached(reached);
        check.setClue(text, imageURL, soundURL, videoURL);
        check.setCheckId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_CC_ID)));
            populateMediaFromURLs(check);
        return check;
    }

    private String getNameFromCursor(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(KEY_HUNT_NAME));
    }

    public void setAllHunts(ArrayList<ScavengerHunt> hunts) {
        String[] columns = {KEY_HUNT_NAME};
        Cursor cursor = mDatabase.query(CHECKPOINT_CLUES_TABLE_NAME, columns, null, null, null, null, KEY_HUNT_NAME + " DESC");
        if (cursor == null || !cursor.moveToFirst()) {
            return;
        }
        hunts.clear();
        String prevName = null;
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

        private static final String DROP_STATEMENT = "DROP TABLE IF EXISTS " + CHECKPOINT_CLUES_TABLE_NAME;

        public HuntDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_STATEMENT);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d("SLS", "Upgrading the database from version " +
                    oldVersion + " to " + newVersion +
                    " will destroy all your data.");
            db.execSQL(DROP_STATEMENT);
            db.execSQL(CREATE_STATEMENT);
        }
    }

    private void populateMediaFromURLs(Checkpoint check) {
        new PopulateMediaTask().execute(check);
    }

    private class PopulateMediaTask extends AsyncTask<Checkpoint, Void, Bitmap> {
        private Checkpoint currentCheck;

        @Override
        protected Bitmap doInBackground(Checkpoint... checks) {
            Bitmap returnedBitmap;
            currentCheck = checks[0];
            try {
                Log.d("FAHSL", "Loading image.... url: "+currentCheck.getClue().getImageURL());
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
            Log.d("FAHSL", "Successfully set image.");
        }
    }

}

