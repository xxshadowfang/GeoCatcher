package barnes.fahsl.geocatcher;

import android.content.Context;
import android.content.Intent;
import android.location.*;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class YourLocationActivity extends ActionBarActivity {

    static final String KEY_LAT = "KEY_LAT";
    static final String KEY_LONG = "KEY_LONG";

    private double currentLat;
    private double currentLong;
    private ScavengerHunt myHunt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_location);

        if (savedInstanceState != null) {
            currentLat = savedInstanceState.getDouble(KEY_LAT, 0);
            currentLong = savedInstanceState.getDouble(KEY_LONG, 0);
            TextView latView = (TextView)findViewById(R.id.LatitudeTextView);
            TextView longView = (TextView)findViewById(R.id.LongitudeTextView);
            latView.setText(String.format("%.8g", currentLat)+"° Latitude");
            longView.setText(String.format("%.8g", currentLong)+"° Longitude");
        }

        Button myClueButton = (Button)findViewById(R.id.clue_button_your_location);
        myClueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchMyCluesIntent = new Intent(getApplicationContext(), ScreenSlideActivity.class);
                startActivity(launchMyCluesIntent);
            }
        });
        Button myExitButton = (Button)findViewById(R.id.exit_your_location);
        myExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchExitToMainIntent = new Intent(getApplicationContext(), GeoCatcherMain.class);
                startActivity(launchExitToMainIntent);
            }
        });

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble(KEY_LAT, currentLat);
        outState.putDouble(KEY_LONG, currentLong);
    }

    private void handleNewLocation(android.location.Location location) {
        this.currentLat = location.getLatitude();
        this.currentLong = location.getLongitude();
        //Toast.makeText(this, "Lat: "+this.currentLat+" Long: "+this.currentLong, Toast.LENGTH_SHORT).show();
        TextView latView = (TextView)findViewById(R.id.LatitudeTextView);
        TextView longView = (TextView)findViewById(R.id.LongitudeTextView);
        latView.setText(String.format("%.8g", currentLat)+"° Latitude");
        longView.setText(String.format("%.8g", currentLong)+"° Longitude");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_your_location, menu);
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
