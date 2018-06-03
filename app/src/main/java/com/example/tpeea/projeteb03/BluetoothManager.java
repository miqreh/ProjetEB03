package com.example.tpeea.projeteb03;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class BluetoothManager{

    private BluetoothDevice bluetoothDevice;
    private BluetoothAdapter mBluetoothAdapter;
    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String TAG = "BLUETOOTHMANAGER";
    private Handler mHandler;
    private ConnectedThread mConnectedThread;
    private ConnectThread mConnectThread;
    public static final int STATE_NONE = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    private int bluetoothState;
    //private FrameProcessor mFp;

    private Context mContext;

    //Constantes liées aux messages entre l'interface utilisateur et le device
    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;
    }


    public BluetoothManager(Context context, Handler handler){
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.bluetoothState=STATE_NONE;
        this.mHandler = handler;
        this.mContext=context;
    }

    public synchronized void connect(BluetoothDevice bd){
        this.bluetoothDevice=bd;
        if (this.bluetoothState == STATE_CONNECTING) {
            if (this.mConnectThread != null) {
                this.mConnectThread.cancel();
                this.mConnectThread = null;
            }
        }

        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }

        this.mConnectThread = new ConnectThread(bluetoothDevice);
        this.mConnectThread.start();

        this.setBluetoothState(STATE_CONNECTING);
        if(mConnectThread.isConnected==true){
            Toast.makeText(mContext, "très cool", Toast.LENGTH_SHORT).show();
        }
    }

    public synchronized void connected(BluetoothSocket socket){
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }

        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }

        this.mConnectedThread = new ConnectedThread(socket);
        this.mConnectedThread.start();

        this.setBluetoothState(STATE_CONNECTED);


    }

    public synchronized void stopConnection(){
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }

        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }

        this.setBluetoothState(STATE_NONE);
    }

    public void write(byte[] out) {
        synchronized (this) {
            if (this.bluetoothState != STATE_CONNECTED) {
                return;
            }
        }
        mConnectedThread.write(out);
    }



    public synchronized int getBluetoothState() {
        return this.bluetoothState;
    }

    private synchronized void setBluetoothState(int bluetoothState) {
        this.bluetoothState = bluetoothState;
    }

    class ConnectThread extends Thread{
        private final BluetoothSocket tSocket;
        private final BluetoothDevice tDevice;
        public boolean isConnected = false;
        public ConnectThread(BluetoothDevice bd){
            BluetoothSocket tmp = null;
            tDevice = bd;
            try {
                // Connexion du socket avec le device à partir de son uuid
                tmp = tDevice.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                Log.e(TAG, "Erreur dans la méthode create() du socket", e);
            }
            tSocket = tmp;
        }

        public void run() {
            mBluetoothAdapter.cancelDiscovery();
            try {
                // Connexion à travers le socket, bloquante jusqu'à sa réussite ou une exception
                tSocket.connect();
            } catch (IOException connectException) {
                // S'il est impossible de se connecter, fermer le socket
                try {
                    tSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Fermeture du socket impossible", closeException);
                }
                return;
            }

            //Connexion réussie
            isConnected=true;

        }

        public void cancel() {
            try {
                tSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Fermeture du socket impossible", e);
            }
        }

    };

    class ConnectedThread extends Thread{
        private final BluetoothSocket tSocket;
        private final InputStream tInStream;
        private final OutputStream tOutStream;
        private byte[] tBuffer;

        public ConnectedThread(BluetoothSocket bs){
            this.tSocket=bs;
            InputStream tmpIn = null;
            OutputStream tmpOut=null;
            try {
                tmpIn = tSocket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Erreur dans la création de l'InputStream", e);
            }
            try {
                tmpOut = tSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Erreur dans la création de l'OutputStream", e);
            }

            tInStream = tmpIn;
            tOutStream = tmpOut;
        }

        public void run() {
            tBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = tInStream.read(tBuffer);
                    // Send the obtained bytes to the UI activity.
                    Message readMsg = mHandler.obtainMessage(MessageConstants.MESSAGE_READ, numBytes, -1, tBuffer);
                    readMsg.sendToTarget();
                } catch (IOException e) {
                    Log.d(TAG, "Input stream déconnecté", e);
                    break;
                }
            }
        }

        // Appellée pour envoyer des données vers l'oscillo
        public void write(byte[] bytes) {
            try {
                tOutStream.write(bytes);

                Message writtenMsg = mHandler.obtainMessage(MessageConstants.MESSAGE_WRITE, -1, -1, tBuffer);
                writtenMsg.sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Erreur lors de l'envoi de données");

                Message writeErrorMsg = mHandler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast", "Envoi impossible");
                writeErrorMsg.setData(bundle);
                mHandler.sendMessage(writeErrorMsg);
            }
        }

        // Appellée pour fermer la connection
        public void cancel() {
            try {
                tSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Erreur dans la fermeture du socket", e);
            }
        }


    };

    /*public void attachFrameProcessor(FrameProcessor frameProcessor){
        mFp=frameProcessor;
    }
    public void detachFrameProcessor(){
        mFp=null;
    }*/

}
