package barnes.fahsl.geocatcher;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ScreenSlideFragments extends Fragment {

    private Bitmap bitMap;
    private String hintString;
    private boolean doesHaveBitMap;
    private boolean beenReached;
    private double lat;
                    private double longi;
    static final String KEY_LAT = "KEY_LAT";
    static final String KEY_LONG = "KEY_LONG";
    public static final String BIT_MAP = "bitmaps";
    public static final String HAS_BIT_MAP = "hasbitmaps";
    public static final String HINT_TEXT = "Hinttext";
    public static final String IS_REVEALED = "revealed";
    public static final String LAT_STAT = "latatude";
    public static final String LONG_DONG = "longitude";
    //public static final String HAS_HINT_TEXT = "HASHinttext";
    public static ScreenSlideFragments create(Checkpoint checkpoint) {
        ScreenSlideFragments fragment = new ScreenSlideFragments();
        Bundle args = new Bundle();
        boolean isImg = checkpoint.getClue().getImage()!=null;
        args.putBoolean(HAS_BIT_MAP,isImg);
        args.putBoolean(IS_REVEALED,checkpoint.hasBeenReached());
        if(checkpoint.hasBeenReached()){
            args.putDouble(LONG_DONG, checkpoint.getLocation().getLongitude());
            args.putDouble(LAT_STAT,checkpoint.getLocation().getLatitude());
        }
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
        if(beenReached){
            longi = getArguments().getDouble(LONG_DONG);
            lat = getArguments().getDouble(LAT_STAT);
        }
        if(doesHaveBitMap)
            bitMap = (Bitmap)getArguments().getParcelable(BIT_MAP);
        if (savedInstanceState != null) {
            lat = savedInstanceState.getDouble(KEY_LAT, 0);
            longi = savedInstanceState.getDouble(KEY_LONG, 0);
            TextView latView = (TextView)getActivity().findViewById(R.id.locationTextViewFragLat);
            TextView longView = (TextView)getActivity().findViewById(R.id.locationTextViewFragLong);
            latView.setText(String.format("%.8g", lat)+"° Latitude");
            longView.setText(String.format("%.8g", longi)+"° Longitude");
        }
    }
    public ScreenSlideFragments() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_screen_slide_fragments, container, false);

        ImageView img =(ImageView)rootView.findViewById(R.id.imageViewIndClue);

        if(doesHaveBitMap)
        img.setImageBitmap(bitMap);
        TextView hint  = (TextView)rootView.findViewById(R.id.HintViewIndClue);
        hint.setText(hintString);
        if(beenReached){
            TextView title  = (TextView)rootView.findViewById(R.id.locationTextViewFrag);
            TextView latitude  = (TextView)rootView.findViewById(R.id.locationTextViewFragLat);
            TextView longitude  = (TextView)rootView.findViewById(R.id.locationTextViewFragLong);
            Button butt = (Button)rootView.findViewById(R.id.updatelocationButton);
            title.setText(getResources().getText(R.string.your_location_found));
            latitude.setText("" +lat);
            longitude.setText("" + longi);
            butt.setVisibility(View.GONE);
        }
        else{
            TextView title  = (TextView)rootView.findViewById(R.id.locationTextViewFrag);
            TextView latitude  = (TextView)rootView.findViewById(R.id.locationTextViewFragLat);
            TextView longitude  = (TextView)rootView.findViewById(R.id.locationTextViewFragLong);
            Button butt = (Button)rootView.findViewById(R.id.updatelocationButton);
            title.setText(getResources().getText(R.string.your_location_title));


        }


        return rootView;
    }
}