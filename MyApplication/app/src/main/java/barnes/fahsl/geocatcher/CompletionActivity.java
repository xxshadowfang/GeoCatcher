package barnes.fahsl.geocatcher;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class CompletionActivity extends ActionBarActivity {
    private int compclues;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completion);
        compclues = getIntent().getIntExtra(ScreenSlideActivity.CLUES_COMP,5);
        TextView clues = (TextView)findViewById(R.id.clues_found);
        clues.setText(getResources().getText(R.string.clues_found_completion)+" "+compclues+"/"+compclues );

        Button myMenuReturnButton = (Button)findViewById(R.id.returnToMenuFromComplete);
        myMenuReturnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchGoToMenuIntent = new Intent(getApplicationContext(), GeoCatcherMain.class);
                startActivity(launchGoToMenuIntent);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_completion, menu);
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
