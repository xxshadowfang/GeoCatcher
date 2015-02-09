package barnes.fahsl.geocatcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Console;

public class ScreenSlideFragments extends Fragment {

    private Bitmap bitMap;
    private String hintString;
    private boolean doesHaveBitMap;
    private boolean beenReached;
    private double lat;
    private double longi;
    private double answerLat;
    private double answerLong;
    static final String KEY_LAT = "KEY_LAT";
    static final String KEY_LONG = "KEY_LONG";
    public static final String BIT_MAP = "bitmaps";
    public static final String HAS_BIT_MAP = "hasbitmaps";
    public static final String HINT_TEXT = "Hinttext";
    public static final String IS_REVEALED = "revealed";
    public static final String LAT_STAT = "latatude";
    public static final String LONG_DONG = "longitude";
    private TextView latView;
    private TextView longView;
    private LocationManager locationManager;
    private LocationListener locationListener;
    //public static final String HAS_HINT_TEXT = "HASHinttext";
    public static ScreenSlideFragments create(Checkpoint checkpoint) {
        ScreenSlideFragments fragment = new ScreenSlideFragments();
        Bundle args = new Bundle();
        boolean isImg = checkpoint.getClue().getImage()!=null;
        args.putBoolean(HAS_BIT_MAP,isImg);
        args.putBoolean(IS_REVEALED,checkpoint.hasBeenReached());

            args.putDouble(LONG_DONG, checkpoint.getLocation().getLongitude());
            args.putDouble(LAT_STAT,checkpoint.getLocation().getLatitude());

        args.putCharArray(HINT_TEXT, checkpoint.getClue().getText().toCharArray());

        if(isImg) {
            Bitmap img = checkpoint.getClue().getImage();

            args.putParcelable(BIT_MAP, img);
        }

        fragment.setArguments(args);
        return fragment;
    }
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        hintString =new String(getArguments().getCharArray(HINT_TEXT));

        doesHaveBitMap = getArguments().getBoolean(HAS_BIT_MAP);
        beenReached = getArguments().getBoolean(IS_REVEALED);
        if(!beenReached){

            answerLong = getArguments().getDouble(LONG_DONG);
            answerLat = getArguments().getDouble(LAT_STAT);
            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(android.location.Location location) {
                    handleNewLocation(location);
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


        }
        else{
            longi = getArguments().getDouble(LONG_DONG);
            lat = getArguments().getDouble(LAT_STAT);
        }
        if(doesHaveBitMap)
            bitMap = (Bitmap)getArguments().getParcelable(BIT_MAP);

    }
    private void handleNewLocation(android.location.Location location) {
        lat = location.getLatitude();
        longi = location.getLongitude();
        Toast.makeText(this.getActivity(), "Lat: " + this.lat + " Long: " + this.longi, Toast.LENGTH_SHORT).show();

        latView.setText(String.format("%.8g", lat)+"° Latitude");
        longView.setText(String.format("%.8g", longi)+"° Longitude");
    }
    public ScreenSlideFragments() {
    }
    @Override public void onDestroy(){
        super.onDestroy();


    }
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_screen_slide_fragments, container, false);

        ImageView img =(ImageView)rootView.findViewById(R.id.imageViewIndClue);
        latView = (TextView)rootView.findViewById(R.id.locationTextViewFragLat);
        longView = (TextView)rootView.findViewById(R.id.locationTextViewFragLong);
        if(doesHaveBitMap)
        img.setImageBitmap(bitMap);
        TextView hint  = (TextView)rootView.findViewById(R.id.HintViewIndClue);
        hint.setText(hintString);
        if(beenReached){
            TextView title  = (TextView)rootView.findViewById(R.id.locationTextViewFrag);


            Button butt = (Button)rootView.findViewById(R.id.updatelocationButton);
            title.setText(getResources().getText(R.string.your_location_found));
            latView.setText("" +lat);
            longView.setText("" + longi);
            butt.setVisibility(View.GONE);
        }
        else{
            TextView title  = (TextView)rootView.findViewById(R.id.locationTextViewFrag);

            Button butt = (Button)rootView.findViewById(R.id.updatelocationButton);
            butt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    float[] results = new float[3];
                    Location.distanceBetween(answerLat,answerLong,lat,longi,results);
                    if(results[0]<20) {
                        locationManager.removeUpdates(locationListener);
                        ((ScreenSlideActivity) inflater.getContext()).nextCheckpoint();
                    }
                }
            });
            title.setText(getResources().getText(R.string.your_location_title));



        }


        return rootView;
    }
}