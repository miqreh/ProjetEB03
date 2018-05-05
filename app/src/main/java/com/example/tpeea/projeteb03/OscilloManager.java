package com.example.tpeea.projeteb03;

/**
 * Created by Steph on 05/05/2018.
 */

public class OscilloManager implements Transceiver.TransceiverDataListener, Transceiver.TransceiverEventListener{
    private OscilloManager instance =null;

    private OscilloManager(){

    }

    //Singleton
    public getOscilloManager(){
        if(instance == null){
            instance = OscilloManager();
        }
        return instance;
    }

    interface OscilloEventListener{

    }
}
