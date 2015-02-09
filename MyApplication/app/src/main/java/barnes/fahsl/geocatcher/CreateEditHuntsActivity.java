package barnes.fahsl.geocatcher;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Geocoder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;


public class CreateEditHuntsActivity extends ActionBarActivity {
    private ArrayList<Checkpoint> checkpoints;
    private ScavengerHunt thisHunt;
    private String name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_edit_hunts);

        Boolean isNew = true;
        if(isNew) {
            checkpoints = new ArrayList<Checkpoint>();
        }
        else
            isNew = isNew;
        Button returnButton = (Button)findViewById(R.id.finishButton);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFinish();
                Intent finishIntent = new Intent(getApplicationContext(), GeoCatcherMain.class);
                startActivity(finishIntent);
            }
        });
    }

    private void showFinish() {

        DialogFragment df = new DialogFragment(){
            final EditText input = new EditText(getActivity());
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState){

                AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
                //set options


                builder.setMessage(R.string.Name_your_Hunt);
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                });
                builder.setView(input);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        name = input.getText().toString();
                        //thisHunt = new ScavengerHunt()
                        //database
                        finish();
                    }
                });

                return builder.create();
            }
        };
        df.show(getFragmentManager(), "exit");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_edit_hunts, menu);
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
