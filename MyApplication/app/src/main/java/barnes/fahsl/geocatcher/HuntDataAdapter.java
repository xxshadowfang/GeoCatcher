package barnes.fahsl.geocatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

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

    // return the new ID
    public void addHunt(ScavengerHunt hunt) {
        for (Checkpoint p : hunt.getCheckpoints()) {
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
        Cursor cursor = mDatabase.query(CHECKPOINT_CLUES_TABLE_NAME, null, KEY_HUNT_NAME+" = "+name, null, null, null, KEY_CC_NO+" ASC");
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

    }
}

