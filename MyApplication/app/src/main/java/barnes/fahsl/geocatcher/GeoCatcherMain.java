package barnes.fahsl.geocatcher;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import com.cengalabs.flatui.FlatUI;
import com.cengalabs.flatui.views.FlatButton;

import com.cengalabs.flatui.views.FlatTextView;




public class GeoCatcherMain extends ActionBarActivity {

    public static final String KEY_NEW_HUNT = "KEY_NEW_HUNT";
    public static final String KEY_HUNT_NAME = "KEY_HUNT_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_catcher_main);
        FlatUI.initDefaultValues(this);
        FlatUI.setDefaultTheme(FlatUI.GRASS);
        //getActionBar().setBackgroundDrawable(FlatUI.getActionBarDrawable(this,FlatUI.GRASS,false));
        getSupportActionBar().setBackgroundDrawable(FlatUI.getActionBarDrawable(this,FlatUI.GRASS,false));

        FlatButton myHuntsButton = (FlatButton)findViewById(R.id.myHuntsButton);
        myHuntsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchMyHuntsIntent = new Intent(getApplicationContext(), MyHuntsActivity.class);
                startActivity(launchMyHuntsIntent);
            }
        });
        FlatButton myCreateButton = (FlatButton)findViewById(R.id.CreateNewHuntsButton);
        myCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchCreateEditHuntsIntent = new Intent(getApplicationContext(), CreateEditHuntsActivity.class);
                launchCreateEditHuntsIntent.putExtra(KEY_NEW_HUNT, true);
                startActivity(launchCreateEditHuntsIntent);
            }
        });
        FlatButton shareButton = (FlatButton)findViewById(R.id.ShareHuntsButton);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent(getApplicationContext(), ShareHuntsActivity.class);
                startActivity(shareIntent);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_geo_catcher_main, menu);
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
}
