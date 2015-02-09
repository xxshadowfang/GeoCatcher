package barnes.fahsl.geocatcher;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.ArrayList;


public class MyHuntsActivity extends ActionBarActivity {
    RadioGroup myGroup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_hunts);

        HuntDataAdapter hda = new HuntDataAdapter(this);
        hda.open();
        ArrayList <String> names = hda.getAllHuntNames();
        //Toast.makeText(this, "Null? "+(names == null), Toast.LENGTH_SHORT).show();

        myGroup = (RadioGroup)findViewById(R.id.all_hunts_radio_group);
        RadioButton button;
        for (int i = 0; i < names.size(); i++) {
            button = new RadioButton(this);
            button.setText(names.get(i));
            myGroup.addView(button);
        }

        Button startButton = (Button)findViewById(R.id.startHuntButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startHuntIntent = new Intent(getApplicationContext(), ScreenSlideActivity.class);
                startHuntIntent.putExtra(GeoCatcherMain.KEY_HUNT_NAME,((RadioButton)MyHuntsActivity.this.findViewById(myGroup.getCheckedRadioButtonId())).getText().toString());
                startActivity(startHuntIntent);
            }
        });

        Button exitButton = (Button)findViewById(R.id.exitButton);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent exitIntent = new Intent(getApplicationContext(), GeoCatcherMain.class);
                startActivity(exitIntent);
            }
        });
        hda.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_hunts, menu);
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
