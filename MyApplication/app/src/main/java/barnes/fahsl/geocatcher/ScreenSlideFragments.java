package barnes.fahsl.geocatcher;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ScreenSlideFragments extends Fragment {

    public static final String BIT_MAP = "bitmaps";
    public static final String HINT_TEXT = "Hinttext";
    public static ScreenSlideFragments create(Checkpoint checkpoint) {
        ScreenSlideFragments fragment = new ScreenSlideFragments();
        Bundle args = new Bundle();
        args.putCharArray(HINT_TEXT, checkpoint.getClue().getText().toCharArray());
        args.putParcelable(BIT_MAP,checkpoint.getClue().getImage());
        fragment.setArguments(args);
        return fragment;
    }
    public ScreenSlideFragments() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_screen_slide_fragments, container, false);

        ImageView img =(ImageView)rootView.findViewById(R.id.imageViewIndClue);
        img.setImageBitmap((Bitmap)savedInstanceState.getParcelable(BIT_MAP));
        TextView hint  = (TextView)rootView.findViewById(R.id.HintViewIndClue);
        hint.setText(new String(savedInstanceState.getCharArray(HINT_TEXT)));

        return rootView;
    }
}