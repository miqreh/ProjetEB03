package com.example.tpeea.projeteb03;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;


public class BluetoothConnectActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private ArrayList foundDevicesArray = new ArrayList();
    private ArrayList pairedDevicesArray = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_connect);
        setSupportActionBar((Toolbar) findViewById(R.id.toolb));

        //couplage de l'adapter et la listview des appareils trouvés
        final ArrayAdapter<String> foundDevicesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, foundDevicesArray);
        ListView listFound = (ListView) findViewById(R.id.ListFound);
        listFound.setAdapter(foundDevicesAdapter);

        IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        //on utilise des classes anonymes pour les BroadcastReceiver
        registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                    //arret de la recherche
                    mBluetoothAdapter.cancelDiscovery();
                    unregisterReceiver(this);
                    Toast.makeText(context, "Recherche terminée", Toast.LENGTH_SHORT).show(); //idéalement affichage dans la progressbar
                }
            }
        }, filter1);

        registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                if (BluetoothDevice.ACTION_FOUND.equals(action))
                {
                    //ajout des devices trouvés à la listview
                    BluetoothDevice newDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    foundDevicesAdapter.add(newDevice.getName());
                    Toast.makeText(context, "appareil ajouté", Toast.LENGTH_SHORT).show(); //juste pour les tests
                }
            }
        }, filter2);
        mBluetoothAdapter.startDiscovery();

        //affichage des appareils appairés
        Set<BluetoothDevice> pairedDevicesSet = mBluetoothAdapter.getBondedDevices();
        for(BluetoothDevice bt : pairedDevicesSet) {
            pairedDevicesArray.add(bt.getName());
        }
        final ArrayAdapter<String> pairedDevicesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, pairedDevicesArray);
        ListView listApp = (ListView) findViewById(R.id.ListApp);
        listApp.setAdapter(pairedDevicesAdapter);

    }

}

