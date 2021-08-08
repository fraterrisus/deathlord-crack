package com.hitchhikerprod.deathlord;

import java.io.IOException;
import java.io.RandomAccessFile;

public class ApplewinSnapshot implements AppleData {

    private RandomAccessFile file;

    private ApplewinSnapshot() { }

    public static ApplewinSnapshot from(String fileName) throws IOException {
        ApplewinSnapshot snapshot = new ApplewinSnapshot();
        snapshot.file = new RandomAccessFile(fileName, "r");
        return snapshot;
    }

    private static final int NUM_BYTES = 256;

    public RandomAccessFile getFile() {
        return file;
    }

    //               0x0 -> 0xf
    // 0x13 / 0xd -> 0x4 -> 0xfd70
    // 0x13 / 0xc -> 0x6 -> 0xfe70
    public short[] readTrack(int desiredTrack, int logicalSector) {
        byte[] data = new byte[NUM_BYTES];
        try {
            int address = 0x0;
            //int address = 0xf970 + (NUM_BYTES * LOGICAL_TO_PHYSICAL_SECTOR_TABLE[logicalSector]);
            if (desiredTrack == 0x13 && logicalSector == 0xc) address = 0xfe70;
            if (desiredTrack == 0x13 && logicalSector == 0xd) address = 0xfd70;
            //System.out.printf("[I] readTrack($%02x,$%x) = $%08x\n", desiredTrack, logicalSector, address);
            file.seek(address);
            file.read(data, 0, NUM_BYTES);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        short[] shorts = new short[NUM_BYTES];
        for (int i=0; i<NUM_BYTES; i++) {
            shorts[i] = (short)(data[i] & 0xff);
        }
        return shorts;
    }
}
