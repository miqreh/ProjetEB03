package com.example.tpeea.projeteb03;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;


public class BluetoothConnectActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private ArrayList foundDevicesArray = new ArrayList();
    private ArrayList pairedDevicesArray = new ArrayList();

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //on récupère l'adresse MAC, soit les 17 derniers caractères pour chaque appareil
        String deviceInfo = ((TextView) view).getText().toString();
        String address = deviceInfo.substring(deviceInfo.length() - 17);

        Intent returnIntent = new Intent();
        returnIntent.putExtra("btDevice",mBluetoothAdapter.getRemoteDevice(address));
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    //classe interne étendant broadcastreceiver
    class cReceiver extends BroadcastReceiver{

        private ArrayAdapter adapteur;

        public cReceiver (ArrayAdapter adapteur){
            super();
            this.adapteur=adapteur;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                BluetoothDevice newDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (newDevice.getName()!=null && newDevice.getBondState()!=newDevice.BOND_BONDED){
                    adapteur.add(newDevice.getName()+"\n"+newDevice.getAddress());
                }

            }else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                mBluetoothAdapter.cancelDiscovery();
                ProgressBar pb = findViewById(R.id.progressBar);
                pb.setVisibility(View.GONE);
                getSupportActionBar().setTitle("Appareils Bluetooth");
            }
        }
    };

    private cReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_connect);
        setSupportActionBar((Toolbar) findViewById(R.id.toolb));
        getSupportActionBar().setTitle("Appareils Bluetooth");

        //couplage de l'adapter et la listview des appareils trouvés
        final ArrayAdapter<String> foundDevicesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, foundDevicesArray);
        ListView listFound = (ListView) findViewById(R.id.ListFound);
        listFound.setAdapter(foundDevicesAdapter);
        listFound.setOnItemClickListener(this);

        IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_FOUND);

        mReceiver = new cReceiver(foundDevicesAdapter);
        registerReceiver(mReceiver, filter1);
        registerReceiver(mReceiver, filter2);
        mBluetoothAdapter.startDiscovery();
        getSupportActionBar().setTitle("Recherche...");



        //affichage des appareils appairés
        Set<BluetoothDevice> pairedDevicesSet = mBluetoothAdapter.getBondedDevices();
        for(BluetoothDevice bt : pairedDevicesSet) {
            pairedDevicesArray.add(bt.getName()+"\n"+bt.getAddress());
        }
        final ArrayAdapter<String> pairedDevicesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, pairedDevicesArray);
        ListView listApp = (ListView) findViewById(R.id.ListApp);
        listApp.setAdapter(pairedDevicesAdapter);
        listApp.setOnItemClickListener(this);

    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }
}