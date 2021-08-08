package com.hitchhikerprod.deathlord;

import com.google.common.base.CaseFormat;

import java.util.EnumSet;
import java.util.Formatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Equipment {

    private Optional<Item> handWeapon;
    private Optional<Item> missileWeapon;
    private Optional<Item> bodyArmor;
    private Optional<Item> shield;
    private Optional<Item> miscArmor;
    private Optional<Item> miscMagic;
    private Optional<Item> tool;
    private Optional<Item> scroll;
    
    private short[] bytes;
    
    public Equipment(short[] data) {
        bytes = data;

        handWeapon      = Item.fromDisk(data[IDX_HAND_WEAPON],    data[IDX_HAND_WEAPON + 0x8]);
        missileWeapon   = Item.fromDisk(data[IDX_MISSILE_WEAPON], data[IDX_MISSILE_WEAPON + 0x8]);
        bodyArmor       = Item.fromDisk(data[IDX_BODY_ARMOR],     data[IDX_BODY_ARMOR + 0x8]);
        shield          = Item.fromDisk(data[IDX_SHIELD],         data[IDX_SHIELD + 0x8]);
        miscArmor       = Item.fromDisk(data[IDX_MISC_ARMOR],     data[IDX_MISC_ARMOR + 0x8]);
        miscMagic       = Item.fromDisk(data[IDX_MISC_MAGIC],     data[IDX_MISC_MAGIC + 0x8]);
        tool            = Item.fromDisk(data[IDX_TOOL],           data[IDX_TOOL + 0x8]);
        scroll          = Item.fromDisk(data[IDX_SCROLL],         data[IDX_SCROLL + 0x8]);
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        Formatter fmt = new Formatter(buffer);

        String equipment = "  Equip: " +
            Stream.of(handWeapon, missileWeapon, bodyArmor, shield, miscArmor, tool, miscMagic, scroll)
                .filter(Optional::isPresent)
                .map(o -> o.get().toString())
                .collect(Collectors.joining(", "));
        buffer.append(equipment);

        /*
        handWeapon.ifPresent    (i -> buffer.append("  Hand:").append(i.toString()));
        missileWeapon.ifPresent (i -> buffer.append("  Missile:").append(i.toString()));
        bodyArmor.ifPresent     (i -> buffer.append("  Armor:").append(i.toString()));
        shield.ifPresent        (i -> buffer.append("  Shield:").append(i.toString()));
        miscArmor.ifPresent     (i -> buffer.append("  Helm:").append(i.toString()));
        tool.ifPresent          (i -> buffer.append("  Tool:").append(i.toString()));
        miscMagic.ifPresent     (i -> buffer.append("  Misc:").append(i.toString()));
        scroll.ifPresent        (i -> buffer.append("  Scroll:").append(i.toString()));
        */

        if (bytes[0x0f] == 0) buffer.append(", Boat");

        for (int i = 0x10; i < 0x20; i++) {
            if (bytes[i] != 0xff) fmt.format("  $%02x:%2x", i, bytes[i]);
        }

        return buffer.toString();
    }
    
    enum Slot {
        HAND_WEAPON(0x0), MISSILE_WEAPON(0x1), BODY_ARMOR(0x2), SHIELD(0x3), MISC_ARMOR(0x4), MISC_MAGIC(0x5),
        TOOL(0x6), SCROLL(0x7);
        
        private int diskValue;
        
        Slot(int diskValue) { this.diskValue = diskValue; }
        
        private static Optional<Slot> from(int val) {
            return EnumSet.allOf(Slot.class).stream()
                .filter(a -> a.diskValue == val)
                .findFirst();
        }
        
        private int toInt() { return diskValue; }
    }
    
    enum CreatureType {
        DRAGON(0x0), GIANT(0x01), DEMON(0x02), UNDEAD(0x03), DEATHLORD(0x04), UNIQUE(0x05);

        private int diskValue;

        CreatureType(int diskValue) { this.diskValue = diskValue; }

        private static Optional<CreatureType> from(int val) {
            return EnumSet.allOf(CreatureType.class).stream()
                .filter(a -> a.diskValue == val)
                .findFirst();
        }

        private int toInt() { return diskValue; }
    }

    enum Effect {
        INOCHI(0x00), ALNASU(0x01), ZUMA(0x02), KOROSU(0x03), MOINOCHI(0x04), TSUIHO(0x05), KAKUSU(0x06),
        HOHYO(0x07), MOAKARI(0x08), DONASU(0x09), HITATE(0x0a), SANTATE(0x0b), PASSWALL(0x0c), LIGHT(0x0d), E(0x0e);

        private int diskValue;

        Effect(int diskValue) { this.diskValue = diskValue; }

        private static Optional<Effect> from(int val) {
            return EnumSet.allOf(Effect.class).stream()
                .filter(a -> a.diskValue == val)
                .findFirst();
        }

        private int toInt() { return diskValue; }
    }

    enum Item {
        TANTO               (0x00, Slot.HAND_WEAPON,    0x01, 0, 2, 3,-2,-1), 
        CLOAK               (0x01, Slot.BODY_ARMOR,     0x02, 0, 0, 0, 0, 1),
        JO_STICK            (0x02, Slot.HAND_WEAPON,    0x03, 1, 1, 7,-1, 0),
        HARA_ATE            (0x03, Slot.BODY_ARMOR,     0x06, 1, 0, 0, 0, 2),
        SAI                 (0x04, Slot.HAND_WEAPON,    0x05, 2, 2, 4, 0, 0),
        GLOVES              (0x05, Slot.MISC_ARMOR,     0x01, 0, 0, 0, 0, 1),
        SMALL_SHIELD        (0x06, Slot.SHIELD,         0x03, 1, 0, 0, 0, 1),
        HARAME_DO           (0x07, Slot.BODY_ARMOR,     0x0b, 1, 0, 0, 0, 3),
        SLING               (0x08, Slot.MISSILE_WEAPON, 0x03, 1, 1, 6,-2, 1),
        GAUNTLETS           (0x09, Slot.MISC_ARMOR,     0x07, 2, 0, 0, 0, 2),
        BO_STAFF            (0x0a, Slot.HAND_WEAPON,    0x02, 0, 1, 7, 0,-1),
        MEDIUM_SHIELD       (0x0b, Slot.SHIELD,         0x08, 2, 0, 0, 0, 2),
        HARAMAKIDO          (0x0c, Slot.BODY_ARMOR,     0x15, 2, 0, 0, 0, 4),
        LIGHT_BOW           (0x0d, Slot.MISSILE_WEAPON, 0x0b, 2, 2, 4,-1, 1),
        JINGASA             (0x0e, Slot.MISC_ARMOR,     0x0a, 2, 0, 0, 0, 1),
        LOCK_PICK           (0x0f, Slot.TOOL,           0x08,-1, 0, 0, 0, 0),
        MASAKARI            (0x10, Slot.HAND_WEAPON,    0x08, 3, 1, 8, 0, 0),
        GREAT_SHIELD        (0x11, Slot.SHIELD,         0x0c, 3, 0, 0,-1, 3),
        CROSSBOW            (0x12, Slot.MISSILE_WEAPON, 0x0f, 2, 1,10,+1, 1),
        GLAIVE              (0x13, Slot.HAND_WEAPON,    0x0c, 3, 1, 9, 0, 0),
        KABUTO              (0x14, Slot.MISC_ARMOR,     0x12, 3, 0, 0, 0, 2),
        HEAVY_BOW           (0x15, Slot.MISSILE_WEAPON, 0x15, 3, 2, 6, 0, 1),
        HOLY_SYMBOL         (0x16, Slot.TOOL,           0x0a,-1, 0, 0, 0, 0, null, null, Character.CharacterClass.SHISAI, 12, Effect.TSUIHO),
        DO_MARU             (0x17, Slot.BODY_ARMOR,     0x29, 3, 0, 0,-1, 5),
        SHURIKEN            (0x18, Slot.MISSILE_WEAPON, 0x07, 2, 3, 3, 0, 1, null, null, Character.CharacterClass.NINJA, null, null),
        NAGINATA            (0x19, Slot.HAND_WEAPON,    0x19, 3, 1,10,+1, 1),
        GREAT_BOW           (0x1a, Slot.MISSILE_WEAPON, 0x1e, 3, 2, 8,+1, 1),
        YOROI               (0x1b, Slot.BODY_ARMOR,     0x3d, 3, 0, 0,-2, 6),
        NUNCHAKU            (0x1c, Slot.HAND_WEAPON,    0x0a, 2, 2, 9,+3, 0, null, null, Character.CharacterClass.NINJA, null, null),
        WAKIZASHI           (0x1d, Slot.HAND_WEAPON,    0x28, 2, 2,10,+1, 0, null, null, Character.CharacterClass.SAMURAI, null, null),
        KATANA              (0x1e, Slot.HAND_WEAPON,    0x3c, 3, 2,14,+1, 1, null, null, Character.CharacterClass.SAMURAI, null, null),
        SCROLL_HEAL         (0x1f, Slot.SCROLL,         0x00,-1, 0, 0, 0, 0, null, null, Character.CharacterClass.SHISAI, 1, Effect.ALNASU),
        TOSHI_CLOAK         (0x20, Slot.BODY_ARMOR,     0x00, 0, 0, 0, 0, 2, null, null, null, 16, Effect.KAKUSU),
        HARA_ATE_PLUS_ONE   (0x21, Slot.BODY_ARMOR,     0x00, 1, 0, 0, 0, 3),
        HARAME_DO_PLUS_ONE  (0x22, Slot.BODY_ARMOR,     0x00, 1, 0, 0, 0, 4),
        BRONZE_SHIELD       (0x23, Slot.SHIELD,         0x00, 1, 0, 0, 0, 3),
        GOLD_JINGASA        (0x24, Slot.MISC_ARMOR,     0x00, 1, 0, 0, 0, 2),
        SCROLL_WARD         (0x25, Slot.SCROLL,         0x00,-1, 0, 0, 0, 0, null, null, Character.CharacterClass.MAHOTSUKAI, 1, Effect.HOHYO),
        ROD_OF_LIGHT        (0x26, Slot.HAND_WEAPON,    0x00, 0, 1, 8,+1, 0, null, null, null, 0x18, Effect.MOAKARI),
        DRAGONSLAYER        (0x27, Slot.HAND_WEAPON,    0x00, 2, 1,12,+1, 1, CreatureType.DRAGON, null, null, null, null),
        GIANTSLAYER         (0x28, Slot.HAND_WEAPON,    0x00, 2, 1,12,+1, 1, CreatureType.GIANT, null, null, null, null),
        DEFENDER            (0x29, Slot.HAND_WEAPON,    0x00, 2, 1,10,+1, 4, null, null, null, 10, Effect.HOHYO),
        SCROLL_CURE         (0x2a, Slot.SCROLL,         0x00,-1, 0, 0, 0, 0, null, null, Character.CharacterClass.SHIZEN, 1, Effect.DONASU),
        HARAMAKIDO_PLUS_ONE (0x2b, Slot.BODY_ARMOR,     0x00, 2, 0, 0, 0, 5),
        BERZERK_SWORD       (0x2c, Slot.HAND_WEAPON,    0x00, 3, 4, 8,+1, 0, null, null, Character.CharacterClass.KICHIGAI, null, null),
        TOSHI_BOW           (0x2d, Slot.MISSILE_WEAPON, 0x00, 2, 3, 8,+3, 2, null, Character.Race.TOSHI, null, null, null),
        SABLE_CLOAK         (0x2e, Slot.BODY_ARMOR,     0x00, 0, 0, 0, 0, 3),
        KOBITO_HAMMER       (0x2f, Slot.MISSILE_WEAPON, 0x00, 1, 1,16,+1, 2, null, Character.Race.KOBITO, null, null, null),
        SILVER_SHIELD       (0x30, Slot.SHIELD,         0x00, 2, 0, 0, 0, 4),
        SWORD_OF_FIRE       (0x31, Slot.HAND_WEAPON,    0x00, 2, 1,12,+1, 1, CreatureType.UNDEAD, null, null, 10, Effect.HITATE),
        ROD_OF_DEATH        (0x32, Slot.HAND_WEAPON,    0x00, 1, 2,10,+2, 1),
        THUNDERBLADE        (0x33, Slot.HAND_WEAPON,    0x00, 3, 1,14,+2, 1, null, null, null, 8, Effect.ZUMA),
        DO_MARU_PLUS_ONE    (0x34, Slot.BODY_ARMOR,     0x00, 2, 0, 0, 0, 6),
        YOROI_PLUS_ONE      (0x35, Slot.BODY_ARMOR,     0x00, 3, 0, 0,-1, 7),
        SCROLL_RAISE        (0x36, Slot.SCROLL,         0x00,-1, 0, 0, 0, 0, null, null, Character.CharacterClass.SHISAI, 1, Effect.INOCHI),
        UNHOLY_BLADE        (0x37, Slot.HAND_WEAPON,    0x00, 3, 1,16,+3, 2, null, null, Character.CharacterClass.RONIN, 6, Effect.KOROSU),
        HOLY_BLADE          (0x38, Slot.HAND_WEAPON,    0x00, 3, 1,15,+3, 2, CreatureType.DEMON, null, Character.CharacterClass.KISHI, 6, Effect.ALNASU),
        GOLDEN_CROWN        (0x39, Slot.MISC_ARMOR,     0x00, 2, 0, 0, 0, 3),
        DO_MARU_PLUS_TWO    (0x3a, Slot.BODY_ARMOR,     0x00, 2, 0, 0, 0, 7),
        POWERSTAFF          (0x3b, Slot.HAND_WEAPON,    0x00, 0, 1,12,+2, 4, null, null, Character.CharacterClass.MAHOTSUKAI, 0x14, Effect.ZUMA),
        RUNEBLADE           (0x3c, Slot.HAND_WEAPON,    0x00, 3, 1,15,+2, 2, null, null, null, 6, Effect.KOROSU),
        GOLDEN_GLOVES       (0x3d, Slot.MISC_ARMOR,     0x00, 3, 0, 0,+2, 3, null, null, null, null, null),
        FALCON_HELM         (0x3e, Slot.MISC_ARMOR,     0x00, 3, 0, 0, 0, 4),
        RING_OF_LIFE        (0x3f, Slot.MISC_MAGIC,     0x00,-1, 0, 0, 0, 1, null, null, Character.CharacterClass.SHISAI, 6, Effect.MOINOCHI),
        GOLDEN_YOROI        (0x40, Slot.BODY_ARMOR,     0x00, 3, 0, 0, 0,10),
        GOLDEN_SHIELD       (0x41, Slot.SHIELD,         0x00, 2, 0, 0, 0, 5),
        SKULL_SHIELD        (0x42, Slot.SHIELD,         0x00, 3, 0, 0, 0, 6, null, null, Character.CharacterClass.RONIN, null, null),
        LANTERN             (0x43, Slot.TOOL,           0x00,-1, 0, 0, 0, 0, null, null, null, 0, Effect.LIGHT),
        SHARKTOOTH          (0x44, Slot.MISC_MAGIC,     0x00,-1, 0, 0, 0, 0, null, null, null, 0, null),
        BLUE_CRYSTAL        (0x45, Slot.MISC_MAGIC,     0x00,-1, 0, 0, 0, 0, null, null, null, 0, Effect.PASSWALL),
        SUNSPEAR            (0x46, Slot.HAND_WEAPON,    0x00, 0, 1,24,+3, 3, CreatureType.DEATHLORD, null, null, 0, null),
        DOCUMENT            (0x47, Slot.SCROLL,         0x00,-1, 0, 0, 0, 0, null, null, null, 0, Effect.E),
        RUBY_RING           (0x48, Slot.MISC_MAGIC,     0x00,-1, 0, 0, 0, 2, null, null, null, 0, Effect.HITATE),
        EMERALD_ROD         (0x49, Slot.HAND_WEAPON,    0x00, 1, 1,16,+4, 6, CreatureType.DEMON, null, null, 0, Effect.SANTATE),
        BLACK_ORB           (0x4a, Slot.MISC_MAGIC,     0x00,-1, 0, 0, 0, 0, null, null, null, 0, null);

        public int diskValue;
        public Slot slot;
        public int unknownAttribute;
        public int weightClass;
        public int numAttacks;
        public int maxDamage;
        public int attackBonus;
        public int armorClassBonus;
        public Optional<CreatureType> slays;
        public Optional<Character.Race> raceRestriction;
        public Optional<Character.CharacterClass> classRestriction;
        public Optional<Integer> maxCharges;
        public int charges;
        public Optional<Effect> specialEffect;

        Item(int diskValue, Slot slot, int unknownAttribute, int weightClass, int numAttacks, int maxDamage,
             int attackBonus, int armorClassBonus) {
            this.diskValue = diskValue;
            this.slot = slot;
            this.unknownAttribute = unknownAttribute;
            this.weightClass = weightClass;
            this.numAttacks = numAttacks;
            this.maxDamage = maxDamage;
            this.attackBonus = attackBonus;
            this.armorClassBonus = armorClassBonus;
            this.slays = Optional.empty();
            this.raceRestriction = Optional.empty();
            this.classRestriction = Optional.empty();
            this.maxCharges = Optional.empty();
            this.specialEffect = Optional.empty();
        }
        
        Item(int diskValue, Slot slot, int unknownAttribute, int weightClass, int numAttacks, int maxDamage, 
            int attackBonus, int armorClassBonus, CreatureType slays, Character.Race raceRestriction,
            Character.CharacterClass classRestriction, Integer maxCharges, Effect specialEffect)
        {
            this.diskValue = diskValue;
            this.slot = slot;
            this.unknownAttribute = unknownAttribute;
            this.weightClass = weightClass;
            this.numAttacks = numAttacks;
            this.maxDamage = maxDamage;
            this.attackBonus = attackBonus;
            this.armorClassBonus = armorClassBonus;
            this.slays = Optional.ofNullable(slays);
            this.raceRestriction = Optional.ofNullable(raceRestriction);
            this.classRestriction = Optional.ofNullable(classRestriction);
            this.maxCharges = Optional.ofNullable(maxCharges);
            this.specialEffect = Optional.ofNullable(specialEffect);
        }
        
        public static Optional<Item> fromDisk(short diskValue, short charges) {
            Optional<Item> item = EnumSet.allOf(Item.class).stream()
                .filter(i -> i.diskValue == (int) diskValue)
                .findFirst();
            item.ifPresent(i -> {
                if (i.maxCharges.isPresent()) i.charges = charges;
            });
            return item;
        }

        public String unenumify() {
            return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name());
        }

        public String toString() {
            String camelCase;
            if (this.maxCharges.isPresent())
                camelCase = this.unenumify() + "(" + this.charges + ")";
            else
                camelCase = this.unenumify();
            camelCase = camelCase.replace("PlusOne", "+1");
            camelCase = camelCase.replace("PlusTwo", "+2");
            return camelCase;
        }
    }
    
    // base index = 0x20, 0x40, 0x60, 0x80, 0xa0, 0xc0
    private static final int IDX_HAND_WEAPON     = 0x00;
    private static final int IDX_MISSILE_WEAPON  = 0x01;
    private static final int IDX_BODY_ARMOR      = 0x02;
    private static final int IDX_SHIELD          = 0x03;
    private static final int IDX_MISC_ARMOR      = 0x04;
    private static final int IDX_MISC_MAGIC      = 0x05;
    private static final int IDX_TOOL            = 0x06;
    private static final int IDX_SCROLL          = 0x07;
}
