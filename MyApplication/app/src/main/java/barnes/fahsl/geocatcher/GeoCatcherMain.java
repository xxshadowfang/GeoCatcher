package barnes.fahsl.geocatcher;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class GeoCatcherMain extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_catcher_main);

        Button myHuntsButton = (Button)findViewById(R.id.myHuntsButton);
        myHuntsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchMyHuntsIntent = new Intent(getApplicationContext(), MyHuntsActivity.class);
                startActivity(launchMyHuntsIntent);
            }
        });
        Button myCreateButton = (Button)findViewById(R.id.CreateNewHuntsButton);
        myCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchCreateEditHuntsIntent = new Intent(getApplicationContext(), CreateEditHuntsActivity.class);
                startActivity(launchCreateEditHuntsIntent);
            }
        });
        Button shareButton = (Button)findViewById(R.id.ShareHuntsButton);
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
