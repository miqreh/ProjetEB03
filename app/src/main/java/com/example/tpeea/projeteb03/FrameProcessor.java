package com.example.tpeea.projeteb03;

/**
 * Created by Steph on 05/05/2018.
 */

public class FrameProcessor {

    public StringBuilder str=new StringBuilder("");
    // On reçoit en paramètres un tableau de byte contenan la commande et ses arguments
    public byte[] toFrame(byte[] commande) {
        // On construit la trame
        byte header = 0x05;
        byte tail = 0x04;
        // length indique la taille de la commande
        byte[] length = {0x00, (byte) commande.length};
        //byte[] payload = toEchap(commande);
        int sum =length[1] +toSumTab(commande);
        byte ctrl = (byte)(Integer.parseInt(toComplement2(Integer.toHexString(sum)),16));
        byte[] preEchap = new byte[length.length+commande.length+1];
        preEchap[0]=length[0];
        preEchap[1]=length[1];
        int j=2;
        for(byte b:commande){
            preEchap[j]=b;
            j++;
        }
        preEchap[j]=ctrl;

        byte[] payload = toEchap(preEchap);
        byte[] frame = new byte[2 + payload.length];


        // On assemble tous les éléments de la frame pour la construire
        frame[0] = header;
        int i=1;
        for(byte b:payload){
            frame[i]=b;
            i++;
        }
        frame[i]=tail;



        if (str.length()>frame.length){
            str.setLength(0);
        }
        for (byte b : frame) {
            str.append(String.format("%02X ", b));
        }

        return frame;
    }

    /* Test complément à deux...
    String toComplement(String hex) {
        int i = Integer.parseInt(hex, 16);
        String bin = Integer.toBinaryString(i);
        // Complement
        String input = bin.replaceAll("0", "a").replaceAll("1", "0").replaceAll("a", "1");
        int number = Integer.parseInt(bin, 2) + 1;
        return Integer.toHexString(number);
    }*/
    // Renvoie le complement à deux d'un hexadecimal
    String toComplement2(String hex){
        int i = Integer.parseInt(hex, 16);
        i = i%256;
        int result=256+~i +1;

        return  Integer.toHexString(result);

    }

/*    String toComplement3(String hex){
        int i = Integer.parseInt(hex, 16);
        return Integer.toHexString(255 - (i / 256) - i / 256 * 256) + 1;
    }*/
// Permet de sommer les octets contenu dans un tableau de byte
    int toSumTab(byte[] tab){
        int total = 0 ;
        for (int val : tab ) {
        total += (int) val;
        }
        return total;
    }
// permet d'inserer un caractère d'ecchappemet devant les bytes réservés , ici 0x04,0x05,0x06. Rajoute 0x06 au carcatère suivant et renvoie le tableau de byte échappé
    byte[] toEchap(byte[] b){
        int i=0;
        for (byte bi: b) {
            if (bi == 0x06 || bi == 0x04 || bi == 0x05) {
                i++;
            }
        }
        byte[] result= new byte[b.length+i];
        i=0;
        for (byte bi:b){

            if(bi== 0x06 || bi == 0x04 || bi == 0x05){
                result[i]=0x06;
                result[i+1]=(byte) (bi +0x06);
            i+=2;
            }else{
                result[i]=bi;
                i++;
            }
        }
        return result;
    }



    public static void main (String[] args){
        // Test
        byte[] b = {0x0A,0x0A};
        FrameProcessor fp =new FrameProcessor();
        byte[] result =fp.toFrame(b);
        System.out.print(result);
    }

}
