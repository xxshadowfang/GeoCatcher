package barnes.fahsl.geocatcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.cengalabs.flatui.FlatUI;

import java.util.ArrayList;
import java.util.List;


public class ShareHuntsActivity extends ActionBarActivity implements NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback {
    private ArrayAdapter<String> arrayAdapter;
    private ScavengerHunt shareHunt;
    private String huntName;
    HuntDataAdapter hda;
    NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_hunts);
        getSupportActionBar().setBackgroundDrawable(FlatUI.getActionBarDrawable(this, FlatUI.GRASS, false));
        HuntDataAdapter hda = new HuntDataAdapter(this);
        hda.open();
        ArrayList<String> names = hda.getAllHuntNames();
        Spinner huntNames = (Spinner)findViewById(R.id.shareHuntsSelectHuntSpinner);

        if (names != null) {
            String[] huntArray = new String[names.size()];
            for (int i = 0; i < huntArray.length; i++)
                huntArray[i] = names.get(i);

            arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, huntArray);
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            huntNames.setAdapter(arrayAdapter);
            huntNames.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    huntName = arrayAdapter.getItem(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "No NFC Adapter Exists", Toast.LENGTH_SHORT).show();
        } else {
            nfcAdapter.setNdefPushMessageCallback(this, this);
            nfcAdapter.setOnNdefPushCompleteCallback(this, this);
        }

        Button myShareButton = (Button)findViewById(R.id.sendButton);
        myShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ShareHuntsActivity.this, getString(R.string.hold_devices_message), Toast.LENGTH_SHORT).show();
            }
        });
        Button myReturnButton = (Button)findViewById(R.id.returnToMenuFromShareButton);
        myReturnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchMainIntent = new Intent(getApplicationContext(), GeoCatcherMain.class);
                startActivity(launchMainIntent);
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        hda = new HuntDataAdapter(this);
        hda.open();
        Intent intent = getIntent();
        String action = intent.getAction();
        if(action != null && action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
            Parcelable[] parcelables =
                    intent.getParcelableArrayExtra(
                            NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage inNdefMessage = (NdefMessage) parcelables[0];
            NdefRecord[] inNdefRecords = inNdefMessage.getRecords();
            NdefRecord NdefRecord_0 = inNdefRecords[0];
            String inMsg = new String(NdefRecord_0.getPayload());
            if (inMsg.equals(";;;")) {
                Toast.makeText(this, getString(R.string.they_have_no_hunts), Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(this, getString(R.string.received_hunt_message), Toast.LENGTH_LONG).show();
            Log.d("FAHSL", inMsg);
            hda.executeStatements(inMsg);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        hda.close();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_share_hunts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        byte[] bytes = hda.generateStringForHunt(huntName).getBytes();
        if (huntName.equals(getString(R.string.no_hunts_message))) {
            Toast.makeText(this, getString(R.string.you_need_a_hunt), Toast.LENGTH_SHORT).show();
            bytes = ";;;".getBytes();
        }
        NdefRecord ndefRecordOut = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, "text/plain".getBytes(), new byte[] {}, bytes);
        NdefMessage nDefMessageOut = new NdefMessage(ndefRecordOut);
        return nDefMessageOut;
    }

    @Override
    public void onNdefPushComplete(NfcEvent event) {
        final String eventString = getString(R.string.share_hunt_message_complete);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ShareHuntsActivity.this, eventString, Toast.LENGTH_LONG).show();
            }
        });
    }
}
