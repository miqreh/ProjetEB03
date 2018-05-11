package com.example.tpeea.projeteb03;

/**
 * Created by Steph on 05/05/2018.
 */

public class FrameProcessor {
    // On enverra un tableau de bytes brut contenant la commande et les argument (payload), et il faudra renvoyer une trame
    public byte[] toFrame(byte[] payload) {
        byte header = 0x05;
        byte tail = 0x04;
        byte[] length = {0x00, (byte) payload.length};
        byte ctrl;

        byte[] frame = null;
        return frame;
    }

    // Permet d'obtenir le complément à deux d'un hexa
    int toComplement(String hex) {
        int i = Integer.parseInt(hex, 16);
        String bin = Integer.toBinaryString(i);
        // Complement
        String input = bin.replaceAll("0", "a").replaceAll("1", "0").replaceAll("a", "1");
        int number = Integer.parseInt(bin, 2) + 1;
        return number;


    }
}
