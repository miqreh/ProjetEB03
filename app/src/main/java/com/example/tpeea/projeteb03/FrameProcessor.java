package com.example.tpeea.projeteb03;

/**
 * Created by Steph on 05/05/2018.
 */

public class FrameProcessor {
// On enverra un tableau de bytes brut contenant la commande et les argument, et il faudra renvoyer une trame
    public byte[] toFrame(byte[] commandes){
        byte  identifiant = commandes[0];
        byte arguments[] =  null;
        int i = 1;
        // On lit le tableau à partir de la deuxième entrée jusqu'à trovuer le caractère d'échappement,. On essaye d'obtenir tus les arguments
        while (commandes[i] !=  0x06 && commandes[i] < commandes.length) {

        }
        byte[] frame = null;
     return frame;
    }
}
