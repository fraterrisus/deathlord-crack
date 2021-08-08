package com.hitchhikerprod.deathlord;

import java.io.IOException;
import java.io.RandomAccessFile;

public class Main {

    public static void main(String[] args) {
        mainDecode(args);
    }

    public static void mainTranslate(String[] args) {
        ApplewinSnapshot disk;
        try {
            disk = ApplewinSnapshot.from(args[0]);

            RandomAccessFile file = disk.getFile();
            long pointer = 0L;
            final int blockSize = 64;
            while(pointer < file.length()) {
                byte[] data = new byte[blockSize];
                file.seek(pointer);
                file.read(data, 0, blockSize);
                for (int i = 0; i < blockSize; i++) System.out.format("%02x", data[i]);
                System.out.println();
                for (int i = 0; i < blockSize; i++) {
                    char c = Character.translateChar((short)data[i]);
                    if ((int)c >= 32 && (int)c < 127) {
                        System.out.print(c);
                        if ((data[i] & 0x80) > 0) System.out.print("/");
                        else System.out.print(" ");
                    } else {
                        System.out.print("  ");
                    }
                }
                System.out.println();
                pointer += blockSize;
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    public static void mainDecode(String[] args) {
        AppleData data = null;
        try {
            if (args[0].endsWith(".nib"))
                data = Disk.from(args[0]);
            else if (args[0].endsWith(".img"))
                data = ApplewinSnapshot.from(args[0]);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            return;
        }

        if (data == null) {
            System.out.println("Don't know what to do with this file");
            System.exit(1);
        }

        short[] trackD = data.readTrack(0x13, 0xd);
        short[] trackC = data.readTrack(0x13, 0xc);
        short[] charData = new short[512];
        System.arraycopy(trackD, 0, charData, 0, 256);
        System.arraycopy(trackC, 0, charData, 256, 256);

        System.out.print("[$0a0]  -- --");
        for (int i=0x0a2; i<0x0c0; i++)
            System.out.printf(" %02x", charData[i]);
        System.out.println();

        System.out.print("[$0e0]  -- -- -- -- -- -- -- -- -- --");
        for (int i=0x0ea; i<0x0f6; i++)
            System.out.printf(" %02x", charData[i]);
        System.out.println(" -- -- -- -- -- -- -- -- -- --");

        System.out.print("[$100]  -- --");
        for (int i=0x102; i<0x120; i++)
            System.out.printf(" %02x", charData[i]);
        System.out.println();

        for (int i = 0; i < 6; i++) {
            Character c = new Character(charData, i);
            if (i == 0) System.out.println(c.headerString());
            System.out.println(c.toString());
        }
    }

}
