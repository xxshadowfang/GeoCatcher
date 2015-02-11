package barnes.fahsl.geocatcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shadow on 2/10/2015.
 */
public class WifiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private ShareHuntsActivity mActivity;
    //WifiP2pManager.PeerListListener myPeerListListener;
    private List<WifiP2pDevice> peer = new ArrayList<WifiP2pDevice>();

    public WifiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       ShareHuntsActivity activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
//        myPeerListListener = new WifiP2pManager.PeerListListener() {
//            @Override
//            public void onPeersAvailable(WifiP2pDeviceList peers) {
//
//            }
//        };
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi P2P is enabled
            } else {
                // Wi-Fi P2P is not enabled
            }
        }
        else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (mManager != null) {
                mManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                        peer.clear();
                        peer.addAll(peers.getDeviceList());

                        // If an AdapterView is backed by this data, notify it
                        // of the change.  For instance, if you have a ListView of available
                        // peers, trigger an update.
                        for(WifiP2pDevice wif: peer){
                            Log.d("test",wif.toString());
                        }
                        if (peer.size() == 0) {
                            Log.d("BARNESGL", "No devices found");
                            return;
                        }

                    }
                });

            }
        }
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
        }
        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            
        }

    }
}
