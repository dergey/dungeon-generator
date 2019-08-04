package com.sergey.zhuravlev.dungeon.generator.enums;

public enum Mask {

    NONE,
    BOX,
    CROSS,
    ROUND;

    private static final int[][] BOX_DATA = {{1, 1, 1}, {1, 0, 1}, {1, 1, 1}};
    private static final int[][] CROSS_DATA = {{0, 1, 0}, {1, 1, 1}, {0, 1, 0}};

    public int[][] getData() {
        switch (this) {
            case BOX:
                return BOX_DATA;
            case CROSS:
                return CROSS_DATA;
            default:
                return null;
        }
    }

}
