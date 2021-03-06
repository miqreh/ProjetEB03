package com.example.tpeea.projeteb03;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import static android.support.v4.content.PermissionChecker.PERMISSION_DENIED;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private OscilloManager mOscilloManager;
    private FrameProcessor mFrameProcessor;
    private Slider mSlider;
    private Handler mHandler;
    private TextView mTextViewValue;
    private TextView mTextViewString;
    private final static int NO_ADAPTER = 0;
    private final static String[] PERMISSIONS = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION};
    private final int PERMISSIONS_REQUEST_CODE = 1;
    private final int USER_REQUEST = 2;
    private final int PERMISSION_GRANTED = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.mSlider = findViewById(R.id.mSlider);
        this.mHandler = new Handler();
        this.mBluetoothManager = new BluetoothManager(this, mHandler);
        this.mOscilloManager= OscilloManager.getOscilloManager();
        this.mFrameProcessor= new FrameProcessor();
        //mTextViewValue = findViewById(R.id.valueSlider);
        mTextViewString = findViewById(R.id.stringSlider);




        mSlider.setSliderListener(new Slider.SliderListener() {
            @Override
            public void onValueChanged(View view, float value) {
                if(view.getId()==R.id.mSlider){
                    //envoyer la commande
                    //mTextViewValue.setText(String.valueOf((int)value));

                    if(mBluetoothManager.getBluetoothState()==mBluetoothManager.STATE_CONNECTED){
                        byte[] trame=mFrameProcessor.toFrame(mOscilloManager.setCalibrationDutyCycle(value));
                        mTextViewString.setText(mFrameProcessor.str);
                        mBluetoothManager.write(trame);
                        Toast.makeText(MainActivity.this,"envoi de la trame",Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onDoubleClick(View view,float value) {
                Toast.makeText(MainActivity.this,"Double Click",Toast.LENGTH_SHORT).show();
                if(view.getId()==R.id.mSlider){
                    //mTextViewValue.setText(String.valueOf((int)value));
                }
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.connect:
                //Toast.makeText(this, "ultracoolos", Toast.LENGTH_SHORT).show();
                switch (BluetoothRights()) {
                    case NO_ADAPTER: {
                        Toast.makeText(this, "Il faut un adaptateur Bluetooth", Toast.LENGTH_SHORT).show();
                    }
                    break;
                    case PERMISSION_GRANTED: { //si toutes les permissions sont filées, on check si le bt est activé et on crée une nouvelle activité
                        if (!mBluetoothAdapter.isEnabled()) {
                            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 1);
                        } else {
                            startActivityForResult(new Intent(this, BluetoothConnectActivity.class), 2);
                        }
                    }
                    break;
                }

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private int BluetoothRights() {
        if (BluetoothAdapter.getDefaultAdapter() == null) {
            return NO_ADAPTER;
        }
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkMultiplePermissions(PERMISSIONS)) {
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
                return USER_REQUEST;
            }
        }
        return PERMISSION_GRANTED;
    }

    private boolean checkMultiplePermissions(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) == PERMISSION_DENIED) {
                return false;
            }
        }
        return true;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {
                BluetoothDevice btDevice = data.getParcelableExtra("btDevice");
                Toast.makeText(this, btDevice.getName(), Toast.LENGTH_SHORT).show();
                //connection à btdevice:
                mBluetoothManager.connect(btDevice);
                //lancement du connectedThread
                if(mBluetoothManager.getBluetoothState()==mBluetoothManager.STATE_CONNECTING){
                    mBluetoothManager.connected(mBluetoothManager.getmConnectThread().gettSocket());
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "retour cancelled", Toast.LENGTH_SHORT).show();
            }
        }

    }
}