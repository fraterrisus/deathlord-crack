package com.hitchhikerprod.deathlord;

/**
 * Created by bcordes on 5/18/17.
 */
public interface AppleData {
    int[] PHYSICAL_TO_LOGICAL_SECTOR_TABLE = new int[] {
        0x0, 0x7, 0xE, 0x6, 0xD, 0x5, 0xC, 0x4, 0xB, 0x3, 0xA, 0x2, 0x9, 0x1, 0x8, 0xF
    };

    int[] LOGICAL_TO_PHYSICAL_SECTOR_TABLE = new int[] {
        0x0, 0x0, 0x9, 0x8, 0x7, 0x5, 0x3, 0x1, 0xE, 0xC, 0xA, 0x8, 0x6, 0x4, 0x2, 0xF
    };

    short[] readTrack(int track, int sector);
}
