package com.sergey.zhuravlev.dungeon.generator.constants;

import com.sergey.zhuravlev.dungeon.generator.enums.Direction;

public class StairEnd {

    private static int[][] NORTH_WALLED_END = {{1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}, {1, 1}};
    private static int[][] NORTH_CORRIDOR_END = {{0, 0}, {1, 0}, {2, 0}};
    private static int[] NORTH_STAIR_END = {0, 0};
    private static int[] NORTH_NEXT_END = {1, 0};

    private static int[][] SOUTH_WALLED_END = {{-1, -1}, {0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}};
    private static int[][] SOUTH_CORRIDOR_END = {{0, 0}, {-1, 0}, {-2, 0}};
    private static int[] SOUTH_STAIR_END = {0, 0};
    private static int[] SOUTH_NEXT_END = {-1, 0};

    private static int[][] WEST_WALLED_END = {{-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}, {1, 0}, {1, 1}};
    private static int[][] WEST_CORRIDOR_END = {{0, 0}, {0, 1}, {0, 2}};
    private static int[] WEST_STAIR_END = {0, 0};
    private static int[] WEST_NEXT_END = {0, 1};

    private static int[][] EAST_WALLED_END = {{-1, -1}, {-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}};
    private static int[][] EAST_CORRIDOR_END = {{0, 0}, {0, -1}, {0, -2}};
    private static int[] EAST_STAIR_END = {0, 0};
    private static int[] EAST_NEXT_END = {0, -1};

    public static int[][] getWalledEnd(Direction direction) {
        switch (direction) {
            case NORTH:
                return NORTH_WALLED_END;
            case SOUTH:
                return SOUTH_WALLED_END;
            case WEST:
                return WEST_WALLED_END;
            case EAST:
                return EAST_WALLED_END;
            default:
                throw new IllegalArgumentException(direction.name());
        }
    }

    public static int[][] getCorridorEnd(Direction direction) {
        switch (direction) {
            case NORTH:
                return NORTH_CORRIDOR_END;
            case SOUTH:
                return SOUTH_CORRIDOR_END;
            case WEST:
                return WEST_CORRIDOR_END;
            case EAST:
                return EAST_CORRIDOR_END;
            default:
                throw new IllegalArgumentException(direction.name());
        }
    }

    public static int[] getStairEnd(Direction direction) {
        switch (direction) {
            case NORTH:
                return NORTH_STAIR_END;
            case SOUTH:
                return SOUTH_STAIR_END;
            case WEST:
                return WEST_STAIR_END;
            case EAST:
                return EAST_STAIR_END;
            default:
                throw new IllegalArgumentException(direction.name());
        }
    }

    public static int[] getNextEnd(Direction direction) {
        switch (direction) {
            case NORTH:
                return NORTH_NEXT_END;
            case SOUTH:
                return SOUTH_NEXT_END;
            case WEST:
                return WEST_NEXT_END;
            case EAST:
                return EAST_NEXT_END;
            default:
                throw new IllegalArgumentException(direction.name());
        }
    }

}
