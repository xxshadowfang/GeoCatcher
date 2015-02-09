package barnes.fahsl.geocatcher;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;

public class ScreenSlideActivity extends FragmentActivity {

    private ArrayList<Checkpoint> checkpoints = new ArrayList<>();
    private ScavengerHunt myHunt;





    private ViewPager mPager;


    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_slide);
        Intent intent = getIntent();
        String name =intent.getStringExtra(GeoCatcherMain.KEY_HUNT_NAME);


        HuntDataAdapter hDA = new HuntDataAdapter(this);
        hDA.open();
        myHunt =hDA.getHuntByName(name);
        hDA.close();
        checkpoints = myHunt.getRevealedCheckpoints();

        checkpoints.add(myHunt.getNextCheckpoint());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPagerAdapter.notifyDataSetChanged();


    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {

            super.onBackPressed();
        } else {

            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }
    public void nextCheckpoint(){
        myHunt.oneRevealedCheckpoint();
        checkpoints = myHunt.getRevealedCheckpoints();

        checkpoints.add(myHunt.getNextCheckpoint());
        mPagerAdapter.notifyDataSetChanged();
        mPager.setCurrentItem(checkpoints.size()-1);
        mPagerAdapter.notifyDataSetChanged();

    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {


            return  ScreenSlideFragments.create(checkpoints.get(position));
        }

        @Override
        public int getCount() {
            return checkpoints.size();

        }
    }
}