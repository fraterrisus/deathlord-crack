package com.hitchhikerprod.deathlord;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Disk implements AppleData {

    private byte[] nibbles;
    private int[][] tracks;

    private Disk() {
        nibbles = new byte[NUM_DISK_BYTES];
        tracks = new int[NUM_TRACKS][NUM_SECTORS];
        for (int t = 0; t < NUM_TRACKS; t++) {
            for (int s = 0; s < NUM_SECTORS; s++) {
                tracks[t][s] = 0;
            }
        }
    }

    public static Disk from(String fileName) throws IOException {
        Disk newDisk = new Disk();

        try (FileInputStream file = new FileInputStream(fileName)) {
            int rval = file.read(newDisk.nibbles);
            if (rval != NUM_DISK_BYTES) {
                System.err.println("Expected " + NUM_DISK_BYTES + " bytes, got " + rval);
            }
        } catch (FileNotFoundException e) {
            System.err.println(fileName + " not found");
            throw e;
        } catch (IOException e) {
            System.err.println("Error reading " + fileName + ": " + e.getMessage());
            throw e;
        }

        newDisk.initializeTracks();

        return newDisk;
    }

    private void initializeTracks() {
        int cursor = 0;
        int volumeNumber = -1;
        while (cursor < NUM_DISK_BYTES-12) {
            cursor++;
            if ((nibbles[cursor-1] == ALIGNMENT_BYTE) &&   // previous byte = FF
                (nibbles[cursor] == HEADER_PROLOGUE) &&    // prologue = D5 AA 96, but not always
                // 8 bytes of header
                (nibbles[cursor+11] == HEADER_EPILOGUE)    // epilogue = DE B7 EB, but not always
                ) {
                final int volume   = decodeFourAndFour(nibbles[cursor+3], nibbles[cursor+4]);
                final int track    = decodeFourAndFour(nibbles[cursor+5], nibbles[cursor+6]);
                final int sector   = decodeFourAndFour(nibbles[cursor+7], nibbles[cursor+8]);
                final int checksum = decodeFourAndFour(nibbles[cursor+9], nibbles[cursor+10]);

                Boolean saveThisCursor = true;

/*
                if ((nibbles[cursor+1] != HEADER_PROLOGUE_2) ||
                    (nibbles[cursor+2] != HEADER_PROLOGUE_3))
                    System.out.printf("[%08x] [W] Expected header prologue %02X %02X %02X, got %02X %02X %02X\n",
                        cursor,
                        HEADER_PROLOGUE, HEADER_PROLOGUE_2, HEADER_PROLOGUE_3,
                        nibbles[cursor], nibbles[cursor+1], nibbles[cursor+2]);

                if ((nibbles[cursor+12] != HEADER_EPILOGUE_2) ||
                    (nibbles[cursor+13] != HEADER_EPILOGUE_3))
                    System.out.printf("[%08x] [W] Expected header epilogue %02X %02X %02X, got %02X %02X %02X\n",
                        cursor,
                        HEADER_EPILOGUE, HEADER_EPILOGUE_2, HEADER_EPILOGUE_3,
                        nibbles[cursor+11], nibbles[cursor+12], nibbles[cursor+13]);
*/

                if (track >= NUM_TRACKS) {
//                    System.out.printf("[%08x] [E] Track number %d is illegal\n", cursor, track);
                    saveThisCursor = false;
                }

                if (sector >= NUM_TRACKS) {
//                    System.out.printf("[%08x] [E] Sector number %d is illegal\n", cursor, sector);
                    saveThisCursor = false;
                }

                if (volumeNumber == -1)
                    volumeNumber = volume;
                else if (volumeNumber != volume) {
//                    System.out.printf("[%08x] [E] Header volume number %d doesn't match expected %d\n",
//                        cursor, volume, volumeNumber);
                    saveThisCursor = false;
                }

                int check = volume ^ track ^ sector;
/*
                if (check != checksum) {
                    System.out.printf("[%08x] [W] Header checksum %02x doesn't match expected %02x+%02x+%02x=%02x\n",
                        cursor, checksum, volume, track, sector, check);
                }
*/

                if (saveThisCursor) {
                    int logicalSector = PHYSICAL_TO_LOGICAL_SECTOR_TABLE[sector];
                    tracks[track][logicalSector] = cursor;
/*
                    System.out.printf("[%08x] [I] Volume %2d Track $%02x Sector P$%1x L$%1x Checksum $%02x\n",
                        cursor, volume, track, sector, logicalSector, checksum);
                } else {
                    System.out.printf("[%08x] [I] Skipping bad sector data\n", cursor);
*/
                }
            }
        }

        System.out.println();
        for (int t = 0; t < NUM_TRACKS; t++) {
            for (int s = 0; s < NUM_SECTORS; s++) {
                if (tracks[t][s] == 0) {
                    System.out.printf("[E] Track %02d Sector %02d was not found\n", t, s);
                }
            }
        }
    }

    public short[] readTrack(int desiredTrack, int logicalSector) {
        System.out.printf("[I] readTrack($%02x,$%01x)\n", desiredTrack, logicalSector);
        short[] data = null;

        final int cursor = tracks[desiredTrack][logicalSector];

        final int volume   = decodeFourAndFour(nibbles[cursor+3], nibbles[cursor+4]);
        final int track    = decodeFourAndFour(nibbles[cursor+5], nibbles[cursor+6]);
        final int sector   = decodeFourAndFour(nibbles[cursor+7], nibbles[cursor+8]);
        final int checksum = decodeFourAndFour(nibbles[cursor+9], nibbles[cursor+10]);

        if (track != desiredTrack) {
            System.out.printf("[E] Header track number $%02x doesn't match expected $%02x\n", track, desiredTrack);
            return null;
        }

        if (PHYSICAL_TO_LOGICAL_SECTOR_TABLE[sector] != logicalSector) {
            System.out.printf("[E] Header sector number $%01d doesn't match expected $%01d\n", sector, logicalSector);
            return null;
        }

        int check = volume ^ track ^ sector;
        if (check != checksum) {
            System.out.printf("[E] Header checksum %02x doesn't match expected %02x+%02x+%02x=%02x\n",
                checksum, volume, track, sector, check);
            return null;
        }

        int dataCursor = cursor + 12;
        while (dataCursor < cursor + 50) { // a random length to look
            dataCursor++;
            if ((nibbles[dataCursor-1] == ALIGNMENT_BYTE) &&
                (nibbles[dataCursor] == DATA_PROLOGUE) &&
                (nibbles[dataCursor+1] == DATA_PROLOGUE_2) &&
                (nibbles[dataCursor+2] == DATA_PROLOGUE_3)
            ) {
                System.out.printf("[I] Found data header at 0x%08x\n", dataCursor);
                data = decodeSixAndTwo(dataCursor+3);
                break;
            }
        }
        if (data == null)
            System.out.println("[E] Couldn't find data header");
        return data;
    }

    private short[] decodeSixAndTwo(int head) {
/*
        System.out.print("Nibbles:");
        for (int i = 0; i < NUM_6B_DATA; i++)
            System.out.printf(" %02x", nibbles[head+i]);
        System.out.printf(" [%02x]", nibbles[head+NUM_6B_DATA]); // checksum
        System.out.println();
*/

        int[] decodedDiskData = decodeDiskData(head);

/*
        System.out.print("Decoded:");
        for (int i = 0; i < NUM_6B_DATA; i++)
            System.out.printf(" %02x", decodedDiskData[i]);
        System.out.println();
*/

        Short checksum = decodeDiskByte(nibbles[head + NUM_6B_DATA]);
        int[] sixBitData = unchecksumDataInterleave(decodedDiskData, checksum);

/*
        System.out.print("Unchecksummed:");
        for (int i = 0; i < NUM_6B_DATA; i++)
            System.out.printf(" %02x", sixBitData[i]);
        System.out.println();
*/

        short[] eightBitData = reconstruct8bData(sixBitData);

/*
        System.out.print("Reconstructed:");
        for (int i = 0; i < NUM_8B_DATA; i++)
            System.out.printf(" %02x", eightBitData[i]);
        System.out.println();
*/

        return eightBitData;
    }

    private int[] decodeDiskData(int basePointer) {
        int[] decodedDiskData = new int[NUM_6B_DATA];
        for (int i = 0; i < NUM_6B_DATA; i++) {
            Short value;
            try {
                value = decodeDiskByte(nibbles[basePointer + i]);
            } catch (IndexOutOfBoundsException e) {
                System.out.printf("[E] Couldn't translate data byte 0x%08x %02x\n",
                    basePointer +i, nibbles[basePointer + i]);
                value = 0;
            }
            decodedDiskData[i] = value.intValue();
        }
        return decodedDiskData;
    }

    private int[] unchecksumDataInterleave(int[] inputData, Short checksum) {
        int[] outputData = new int[NUM_6B_DATA];
        int workingChecksum = 0x0;
        int newChecksum;

        int inputIndex = 0;
        int outputIndex;
        for (outputIndex = 0x155; outputIndex >= 0x100; outputIndex--) {
            newChecksum = workingChecksum ^ inputData[inputIndex];
            outputData[outputIndex] = newChecksum;
//            System.out.printf("  %03d -> %03x ^ %03x = %03x -> $%03x\n",
//                inputIndex, workingChecksum, inputData[inputIndex], outputData[outputIndex], outputIndex);
            workingChecksum = newChecksum;
            inputIndex++;
        }
        for (outputIndex = 0x0; outputIndex < 0x100; outputIndex++) {
            newChecksum = workingChecksum ^ inputData[inputIndex];
            outputData[outputIndex] = newChecksum;
//            System.out.printf("  %03d -> %03x ^ %03x = %03x -> $%03x\n",
//                inputIndex, workingChecksum, inputData[inputIndex], outputData[outputIndex], outputIndex);
            workingChecksum = newChecksum;
            inputIndex++;
        }

        // workingChecksum XOR byte 342 == 0, or in other words, workingChecksum == byte 342
        if (checksum.intValue() != workingChecksum)
            System.out.printf("[W] Checksum mismatch: Read %02x Computed %02x\n", checksum, workingChecksum);
        return outputData;
    }

    private short[] reconstruct8bData(int[] inputData) {
        short[] outputData = new short[NUM_8B_DATA];

        for (int i = 0; i < NUM_8B_DATA; i++) {
            int highBits = inputData[i] << 2;

            int bit0Shift = 1;
            int bit1Shift = 0;
            int lowIndex = 0x155 - i;
            if (lowIndex < 0x100) { lowIndex += 0x56; bit0Shift += 2; bit1Shift += 2; }
            if (lowIndex < 0x100) { lowIndex += 0x56; bit0Shift += 2; bit1Shift += 2; }

            int bit1 = (inputData[lowIndex] >> bit1Shift) & 0x1;
            int bit0 = (inputData[lowIndex] >> bit0Shift) & 0x1;

            int outputByte = highBits | (bit1 << 1) | bit0;
            outputData[i] = (short)outputByte;
        }

        return outputData;
    }

    private static byte encodeSixAndTwo(short a) {
        if (a < 0 || a > ENCODE_SIX_AND_TWO_TABLE.length)
            throw new IndexOutOfBoundsException();
        return ENCODE_SIX_AND_TWO_TABLE[a];
    }

    private static Short decodeDiskByte(byte a) {
        Short decoded = DECODE_SIX_AND_TWO_TABLE.get(a);
        if (decoded == null)
            throw new IndexOutOfBoundsException();
        return decoded;
    }

    private static int decodeFourAndFour(int a, int b) {
        return ((a & 0x55) << 1) | (b & 0x55);
    }

    // 6656 nibbles (6b) = 4992 bytes (8b)
    private final static byte[] ENCODE_SIX_AND_TWO_TABLE = new byte[] {
        (byte)0x96, (byte)0x97, (byte)0x9A, (byte)0x9B, (byte)0x9D, (byte)0x9E, (byte)0x9F, (byte)0xA6,
        (byte)0xA7, (byte)0xAB, (byte)0xAC, (byte)0xAD, (byte)0xAE, (byte)0xAF, (byte)0xB2, (byte)0xB3,
        (byte)0xB4, (byte)0xB5, (byte)0xB6, (byte)0xB7, (byte)0xB9, (byte)0xBA, (byte)0xBB, (byte)0xBC,
        (byte)0xBD, (byte)0xBE, (byte)0xBF, (byte)0xCB, (byte)0xCD, (byte)0xCE, (byte)0xCF, (byte)0xD3,
        (byte)0xD6, (byte)0xD7, (byte)0xD9, (byte)0xDA, (byte)0xDB, (byte)0xDC, (byte)0xDD, (byte)0xDE,
        (byte)0xDF, (byte)0xE5, (byte)0xE6, (byte)0xE7, (byte)0xE9, (byte)0xEA, (byte)0xEB, (byte)0xEC,
        (byte)0xED, (byte)0xEE, (byte)0xEF, (byte)0xF2, (byte)0xF3, (byte)0xF4, (byte)0xF5, (byte)0xF6,
        (byte)0xF7, (byte)0xF9, (byte)0xFA, (byte)0xFB, (byte)0xFC, (byte)0xFD, (byte)0xFE, (byte)0xFF
    };

    private final static Map<Byte,Short> DECODE_SIX_AND_TWO_TABLE;
    static {
        DECODE_SIX_AND_TWO_TABLE = new HashMap<>();
        for (short i = 0; i < ENCODE_SIX_AND_TWO_TABLE.length; i++) {
            DECODE_SIX_AND_TWO_TABLE.put(ENCODE_SIX_AND_TWO_TABLE[i], i);
        }
    }


    private final static int NUM_DISK_BYTES = 232960;
    public final static int NUM_TRACKS = 35;
    public final static int NUM_SECTORS = 16;

    private final static int NUM_NIBBLES = 6656;
    private final static int NUM_6B_DATA = 342;
    private final static int NUM_8B_DATA = 256;

    private final static byte ALIGNMENT_BYTE = (byte)0xFF;

    private final static byte HEADER_PROLOGUE   = (byte)0xD5;
    private final static byte HEADER_PROLOGUE_2 = (byte)0xAA;
    private final static byte HEADER_PROLOGUE_3 = (byte)0xD6; // 96 = 1001 0110 ; D6 = 1101 0110

    private final static byte HEADER_EPILOGUE   = (byte)0xDE;
    private final static byte HEADER_EPILOGUE_2 = (byte)0xB7;
    private final static byte HEADER_EPILOGUE_3 = (byte)0xEB;

    private final static byte DATA_PROLOGUE   = (byte)0xD5;
    private final static byte DATA_PROLOGUE_2 = (byte)0xAE; // 0xAA
    private final static byte DATA_PROLOGUE_3 = (byte)0xAD; // indicates 6-and-2; 0x96 = 4-and-4?
    
}
