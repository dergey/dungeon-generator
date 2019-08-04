package com.sergey.zhuravlev.dungeon.generator.constants;

public class CellConstant {
    
    public static final int NOTHING = 0x00000000;

    public static final int BLOCKED = 0x00000001;
    public static final int ROOM = 0x00000002;
    public static final int CORRIDOR = 0x00000004;

    public static final int PERIMETER = 0x00000010;
    public static final int ENTRANCE = 0x00000020;
    public static final int ROOM_ID = 0x0000FFC0;

    public static final int ARCH = 0x00010000;
    public static final int DOOR = 0x00020000;
    public static final int LOCKED = 0x00040000;
    public static final int TRAPPED = 0x00080000;
    public static final int SECRET = 0x00100000;
    public static final int PORTC = 0x00200000;
    public static final int STAIR_DN = 0x00400000;
    public static final int STAIR_UP = 0x00800000;

    public static final int OPENSPACE = ROOM | CORRIDOR;
    public static final int DOORSPACE = ARCH | DOOR | LOCKED | TRAPPED | SECRET | PORTC;
    public static final int ESPACE = ENTRANCE | DOORSPACE | 0xFF000000;
    public static final int STAIRS = STAIR_DN | STAIR_UP;

    public static final int BLOCK_ROOM = BLOCKED | ROOM;
    public static final int BLOCK_CORR = BLOCKED | PERIMETER | CORRIDOR;
    public static final int BLOCK_DOOR = BLOCKED | DOORSPACE;
    
}
