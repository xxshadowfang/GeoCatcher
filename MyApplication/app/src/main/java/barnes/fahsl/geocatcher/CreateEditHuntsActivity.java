package barnes.fahsl.geocatcher;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;


public class CreateEditHuntsActivity extends ActionBarActivity {
    private ArrayList<Checkpoint> checkpoints;
    private ScavengerHunt thisHunt;
    private String name;
    ImageView imgFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_edit_hunts);

        Boolean isNew = true;
        if(isNew) {
            checkpoints = new ArrayList<Checkpoint>();
        }
        else
            ; // Load previous hunt data

        EditText input = (EditText)findViewById(R.id.nameHuntEdit);
        Button takePic = (Button)findViewById(R.id.takePictureButton);
        imgFavorite = (ImageView)findViewById(R.id.samplepicview);
        takePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });
        Button returnButton = (Button)findViewById(R.id.finishButton);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFinish();
            }
        });
    }

    private void showFinish() {
        DialogFragment df = new DialogFragment(){
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState){
                AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                final View view  = inflater.inflate(R.layout.dialog_fragment_layout, null);
                builder.setView(view);
                final EditText input = (EditText)findViewById(R.id.nameHuntEdit);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final EditText input = (EditText)view.findViewById(R.id.nameHuntEdit);
                        name =input.getText().toString();
                        //finish();
                        Intent finishIntent = new Intent(getApplicationContext(), GeoCatcherMain.class);
                        Toast.makeText(getApplicationContext(), "Successfully created hunt: "+name, Toast.LENGTH_SHORT).show();
                        startActivity(finishIntent);
                    }
                });
                builder.setMessage(R.string.Name_your_Hunt);
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

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
    public void openCamera(){
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 0);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bp = (Bitmap) data.getExtras().get("data");
        imgFavorite.setImageBitmap(bp);
    }
}
