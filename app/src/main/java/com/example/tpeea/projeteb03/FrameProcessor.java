package com.example.tpeea.projeteb03;

/**
 * Created by Steph on 05/05/2018.
 */

public class FrameProcessor {
    // On enverra un tableau de bytes brut contenant la commande et les argument (payload), et il faudra renvoyer une trame
    public byte[] toFrame(byte[] commande) {
        byte header = 0x05;
        byte tail = 0x04;
        byte[] length = {0x00, (byte) commande.length};
        int sumPayload= toSumTab(commande);
        int sum = header + length[1] + sumPayload;
        byte ctrl = (byte)Integer.parseInt(toComplement2(Integer.toHexString(sum)),16);
        byte[] payload = toEchap(commande);

        byte[] frame = new byte[3 + length.length + payload.length];
        int i=0;
        frame[i] = header;
        i++;
        frame[i]=length[0];
        i++;
        frame[i]=length[1];
        i++;
        for(byte b:payload){
            frame[i]=b;
            i++;
        }
        frame[i]=ctrl;
        i++;
        frame[i]=tail;

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
    String toComplement2(String hex){
        int i = Integer.parseInt(hex, 16);
        i = i%256;
        int result=256-i;

        return  Integer.toString(result);
    }

    int toSumTab(byte[] tab){
        int total = 0 ;
        for (int val : tab ) {
        total += (int) val;
        }
        return total;
    }

    byte[] toEchap(byte[] b){
        int i=0;
        for (byte bi: b) {
            if (bi == 0x06) {
                i++;
            }
        }
        byte[] result= new byte[b.length+i];
        i=0;
        for (byte bi:b){
            result[i]=bi;
            i++;
            if(bi==0x06){
                result[i]=0x0C;
            }

        }
        return result;
    }

    public static void main (String[] args){
        byte[] b = {0x07,0x06,0x0c};
        FrameProcessor fp =new FrameProcessor();
        System.out.print(fp.toFrame(b));
    }

}
