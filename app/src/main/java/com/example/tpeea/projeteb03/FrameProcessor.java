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
        int sumPayload= toSumTab(payload);
        int sum = header + length[1] + sumPayload;
        byte ctrl = new Byte( toComplement(Integer.toHexString(sum)));

        byte[] frame = null;
        return frame;
    }

    // Permet d'obtenir le complément à deux d'un hexa, retourne un hexa
    String toComplement(String hex) {
        int i = Integer.parseInt(hex, 16);
        String bin = Integer.toBinaryString(i);
        // Complement
        String input = bin.replaceAll("0", "a").replaceAll("1", "0").replaceAll("a", "1");
        int number = Integer.parseInt(bin, 2) + 1;
        return Integer.toHexString(number);


    }
    int toComplement2(String hex){
        int i = Integer.parseInt(hex, 16);
        i = i%256;

        return 256 - i;
    }

    int toSumTab(byte[] tab){
        int total = 0 ;
        for (int val : tab ) {
        total += (int) val;
        }
        return total;
    }


}
