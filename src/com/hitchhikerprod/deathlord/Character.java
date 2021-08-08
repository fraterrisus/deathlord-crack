package com.hitchhikerprod.deathlord;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by bcordes on 5/1/17.
 */
public class Character {
    private int charnum;
    private Map<Integer, Short> unknown;

    private char[] name;
    private int maxHealth;
    private int curHealth;
    private int armorClass;
    private int level;
    private int level_up;
    private int experience;
    private int strength;
    private int constitution;
    private int size;
    private int intelligence;
    private int dexterity;
    private int charisma;
    private int maxPower;
    private int curPower;
    private int age;
    private int food;
    private int torches;
    private int gold;
    private CharacterClass charClass;
    private Race race;
    private Alignment alignment;
    private Gender gender;
    private Set<Status> status;
    private Equipment equipment;
    
    public Character(short[] data, int charNum) {
        charnum = charNum;

        level           = getByte(data, charNum, IDX_LEVEL);
        level_up        = getByte(data, charNum, IDX_LEVEL_UP);
        experience      = getTwoBytes(data, charNum, IDX_EXP_HI, IDX_EXP_LO);
        strength        = getByte(data, charNum, IDX_STR);
        constitution    = getByte(data, charNum, IDX_CON);
        size            = getByte(data, charNum, IDX_SIZ);
        intelligence    = getByte(data, charNum, IDX_INT);
        dexterity       = getByte(data, charNum, IDX_DEX);
        charisma        = getByte(data, charNum, IDX_CHR);
        maxHealth       = getTwoBytes(data, charNum, IDX_MHP_HI, IDX_MHP_LO);
        curHealth       = getTwoBytes(data, charNum, IDX_CHP_HI, IDX_CHP_LO);
        maxPower        = getByte(data, charNum, IDX_MPOW);
        curPower        = getByte(data, charNum, IDX_CPOW);
        armorClass      = getByte(data, charNum, IDX_AC);
        age             = getByte(data, charNum, IDX_AGE);
        food            = getByte(data, charNum, IDX_FOOD);
        torches         = getByte(data, charNum, IDX_TORCHES);
        gold            = getTwoBytes(data, charNum, IDX_GOLD_HI, IDX_GOLD_LO);
        charClass       = CharacterClass.from(getByte(data, charNum, IDX_CLASS));
        race            = Race.from(getByte(data, charNum, IDX_RACE));
        gender          = Gender.from(getByte(data, charNum, IDX_GENDER));
        alignment       = Alignment.from(getByte(data, charNum, IDX_ALIGNMENT));
        status          = Status.from(getByte(data, charNum, IDX_STATUS));

        short[] nameData = getWord(data, charNum, IDX_NAME, 0x9);
        name = new char[9];
        for (int i=0; i<9; i++) name[i] = translateChar(nameData[i]);

        short[] equipData = getWord(data, charNum, IDX_EQUIP, 0x20);
        equipment = new Equipment(equipData);

        unknown = new HashMap<>();
        /*
        List<Integer> unknownBytes = Arrays.asList(0xA2, 0xA8, 0xAE, 0xB4, 0xBA, 0xEA, 0xF0, 0x102, 0x108, 0x10e,
            0x115, 0x10a);
        for (int idx : unknownBytes) {
            unknown.put(idx, data[idx+charNum]);
        }
        */
    }

    public static short[] getWord(short[] data, int charNum, int offset, int length) {
        short[] equipData = new short[length];
        System.arraycopy(data, offset + (length * charNum), equipData, 0, length);
        return equipData;
    }

    private static int getByte(short[] data, int charNum, int field) {
        return (int)data[field + charNum];
    }
    
    private static int getTwoBytes(short[] data, int charNum, int highByte, int lowByte) {
        return (data[highByte + charNum] << 8) | data[lowByte + charNum];
    }

    enum CharacterClass {
        SENSHI(0), KISHI(1), RYOSHI(2), YABANJIN(3), KICHIGAI(4), SAMURAI(5), RONIN(6), YAKUZA(7), ANSATSUSHA(8),
        NINJA(9), SHUKENJA(10), SHISAI(11), SHIZEN(12), MAHOTSUKAI(13), GENKAI(14), KOSAKU(15);
        private int diskValue;

        CharacterClass(int diskValue) { this.diskValue = diskValue; }

        private static CharacterClass from(int val) {
            return EnumSet.allOf(CharacterClass.class).stream()
                .filter(a -> a.diskValue == val)
                .findFirst().orElseThrow(IllegalArgumentException::new);
        }

        private int toInt() { return diskValue; }

        private String toAbbr() {
            if (this == SHIZEN) return "SHZ";
            else return name().substring(0,3);
        }
    }

    enum Race {
        HUMAN(0), TOSHI(1), NINTOSHI(2), KOBITO(3), GNOME(4), OBAKE(5), TROLL(6), OGRE(7);
        private int diskValue;

        Race(int diskValue) { this.diskValue = diskValue; }

        private static Race from(int val) {
            return EnumSet.allOf(Race.class).stream()
                .filter(a -> a.diskValue == val)
                .findFirst().orElseThrow(IllegalArgumentException::new);
        }

        private int toInt() { return diskValue; }

        public String toAbbr() {
            return name().substring(0,3);
        }
    }

    enum Alignment {
        GOOD(0), NEUTRAL(1), EVIL(2);
        private int diskValue;

        Alignment(int diskValue) { this.diskValue = diskValue; }

        private static Alignment from(int val) {
            return EnumSet.allOf(Alignment.class).stream()
                .filter(a -> a.diskValue == val)
                .findFirst().orElseThrow(IllegalArgumentException::new);
        }

        private int toInt() { return diskValue; }
    }

    enum Gender {
        MALE(0), FEMALE(1);
        private int diskValue;

        Gender(int diskValue) { this.diskValue = diskValue; }

        private static Gender from(int val) {
            return EnumSet.allOf(Gender.class).stream()
                .filter(g -> g.diskValue == val)
                .findFirst().orElseThrow(IllegalArgumentException::new);
        }

        private int toInt() { return diskValue; }
    }
    
    enum Status {
        // RIP STN PAR ILL TOX STV
        BIT0(0x01), STARVING(0x02), POISONED(0x04), DISEASED(0x08),
        PARALYZED(0x10), PETRIFIED(0x20), DEAD6(0x40), DEAD7(0x80);
        private int diskValue;
        
        Status(int diskValue) { this.diskValue = diskValue; }
        
        private static Set<Status> from(int val) {
            return EnumSet.allOf(Status.class).stream()
                .filter(g -> (val & g.diskValue) > 0)
                .collect(Collectors.toSet());
        }

        private int toInt() { return diskValue; }
    }

    public static char translateChar(short chr) {
        return (char)((chr & 0x7f) ^ 0x65);
    }

/*
    public static char translateChar(short chr) {
        int c = (chr & 0x7f);
        switch (c) {
            case 0x24: return 'A';
            case 0x27: return 'B';
            case 0x26: return 'C';
            case 0x21: return 'D';
            case 0x20: return 'E';
            case 0x23: return 'F';
            case 0x22: return 'G';
            case 0x2D: return 'H';
            case 0x2C: return 'I';
            case 0x2F: return 'J';
            case 0x2E: return 'K';
            case 0x29: return 'L';
            case 0x28: return 'M';
            case 0x2B: return 'N';
            case 0x2A: return 'O';
            case 0x35: return 'P';
            case 0x34: return 'Q';
            case 0x37: return 'R';
            case 0x36: return 'S';
            case 0x31: return 'T';
            case 0x30: return 'U';
            case 0x33: return 'V';
            case 0x32: return 'W';
            case 0x3D: return 'X';
            case 0x3C: return 'Y';
            case 0x3F: return 'Z';
            case 0x42: return '\'';
            case 0x44: return '!';
            case 0x45: return ' ';
            case 0x48: return '-';
            case 0x4E: return '+';
            case 0x54: return '1';
            case 0x57: return '2';
            case 0x5a: return '?';
            case 0x68: return ',';
            default: return '_';
        }
    }
*/

    public String headerString() {
        StringBuilder buffer = new StringBuilder();
        Formatter fmt = new Formatter(buffer);
        buffer.append("    ");
        fmt.format("%-9s ", "Name");
        fmt.format("%1s ", "G");
        fmt.format("%1s ", "A");
        fmt.format("%3s ", "Rac");
        fmt.format("%3s ", "Cls");
        fmt.format("%3s ", "Age");
        fmt.format("%5s ", "Level");
        fmt.format("%4s ", "XP");
        fmt.format("%2s ", "ST");
        fmt.format("%2s ", "CN");
        fmt.format("%2s ", "SZ");
        fmt.format("%2s ", "IQ");
        fmt.format("%2s ", "DX");
        fmt.format("%2s ", "CH");
        fmt.format("%7s ", "Power");
        fmt.format("%9s ", "Health");
        fmt.format("%3s ", "AC");
        fmt.format("%5s ", "Gold");
        fmt.format("%2s ", "Tr");
        fmt.format("%3s ", "Fd");
        //buffer.append("Statuses");
        unknown.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEachOrdered(e -> fmt.format(" %03x", e.getKey()));
        return buffer.toString();
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        Formatter fmt = new Formatter(buffer);
        fmt.format("#%1d: ", charnum+1);
        fmt.format("%9s ", String.valueOf(name));
        fmt.format("%1s ", gender.toString().substring(0,1));
        fmt.format("%1s ", alignment.toString().substring(0,1));
        fmt.format("%3s ", race.toAbbr());
        fmt.format("%3s ", charClass.toAbbr());
        fmt.format("%03d ", age);
        fmt.format("%02d+%02d ", level, level_up);
        fmt.format("%04d ", experience);
        fmt.format("%02d ", strength);
        fmt.format("%02d ", constitution);
        fmt.format("%02d ", size);
        fmt.format("%02d ", intelligence);
        fmt.format("%02d ", dexterity);
        fmt.format("%02d ", charisma);
        fmt.format("%03d/%03d ", curPower, maxPower);
        fmt.format("%04d/%04d ", curHealth, maxHealth);
        fmt.format("%+03d ", 10 - armorClass);
        fmt.format("%5d ", gold);
        fmt.format("%02d ", torches);
        fmt.format("%03d ", food);
        buffer.append(status.stream().map(Status::toString).collect(Collectors.joining(",")));
/*
        unknown.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEachOrdered(e -> fmt.format(" %03d", e.getValue()));
*/
        buffer.append("\n");
        buffer.append(equipment.toString());
        return buffer.toString();
    }

    // just prior: macro 1
    // just prior: macro 2
    // just prior: macro 3
    // just prior: macro 4
    // just prior: team name

    // linapple snapshot: 0xFD70

    private static final int IDX_NAME     = 0x000; // 36, 42, 46, 44, 69, 69, 69, 69, 197  AOKI----
    //private static final int IDX_NAME_1   = 0x009; // 49, 42, 33, 36, 69, 69, 69, 69, 197  TODA----
    //private static final int IDX_NAME_2   = 0x012; // 40, 44, 46, 48, 40, 42, 69, 69, 197  MIKUMO--
    //private static final int IDX_NAME_3   = 0x01B; // 46, 48, 54, 45, 44, 63, 48, 69, 197  KUSHIZA-
    //private static final int IDX_NAME_4   = 0x024; // 39, 44, 49, 42, 69, 69, 69, 69, 197  BITO----
    //private static final int IDX_NAME_5   = 0x02D; // 44, 54, 45, 44, 43, 42, 69, 69, 197  ISHINO--

    private static final int IDX_STATUS   = 0x036;
    private static final int IDX_AC       = 0x03C; // 10 - $3c = armor class
    private static final int IDX_MHP_LO   = 0x042;
    private static final int IDX_MHP_HI   = 0x048;
    private static final int IDX_CHP_LO   = 0x04E;
    private static final int IDX_CHP_HI   = 0x054;
    private static final int IDX_RACE     = 0x05a;
    private static final int IDX_CLASS    = 0x060;
    private static final int IDX_LEVEL    = 0x066;
    private static final int IDX_LEVEL_UP = 0x06C;
    private static final int IDX_STR      = 0x072;
    private static final int IDX_CON      = 0x078;
    private static final int IDX_SIZ      = 0x07E;
    private static final int IDX_INT      = 0x084;
    private static final int IDX_DEX      = 0x08A;
    private static final int IDX_CHR      = 0x090;
    private static final int IDX_CPOW     = 0x096;
    private static final int IDX_MPOW     = 0x09C;
    //private static final int IDX          = 0x0A2; // follows party reordering
    //private static final int IDX          = 0x0A8; // follows party reordering
    //private static final int IDX          = 0x0AE; // follows party reordering
    //private static final int IDX          = 0x0B4; // follows party reordering
    //private static final int IDX          = 0x0BA; // follows party reordering
    private static final int IDX_GENDER   = 0x0C0;
    private static final int IDX_ALIGNMENT= 0x0C6;
    private static final int IDX_AGE      = 0x0CC;
    private static final int IDX_FOOD     = 0x0D2;
    private static final int IDX_GOLD_HI  = 0x0DE;
    private static final int IDX_GOLD_LO  = 0x0D8;
    private static final int IDX_TORCHES  = 0x0E4;
    //private static final int IDX          = 0x0EA; // ?????
    //private static final int IDX          = 0x0F0; // weapon class? 0-fighter 1-shi/shz 2-mah/gen
    private static final int IDX_EXP_LO   = 0x0F6;
    private static final int IDX_EXP_HI   = 0x0FC;
    //private static final int IDX          = 0x102; // all zeroes, can't tell
    //private static final int IDX          = 0x108; // follows party reordering
    //private static final int IDX          = 0x10E; // all zeroes, can't tell
    //private static final int IDX          = 0x115; // mirrors $0ae
    //private static final int IDX          = 0x11A; // follows party reordering
    private static final int IDX_EQUIP    = 0x120;
    //private static final int IDX_EQUIP_1  = 0x140;
    //private static final int IDX_EQUIP_2  = 0x160;
    //private static final int IDX_EQUIP_3  = 0x180;
    //private static final int IDX_EQUIP_4  = 0x1A0;
    //private static final int IDX_EQUIP_5  = 0x1C0;
    //private static final int IDX          = 0x1E0; // doesn't seem to be per-character
}
