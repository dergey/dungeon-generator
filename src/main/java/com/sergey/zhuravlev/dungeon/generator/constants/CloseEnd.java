package com.sergey.zhuravlev.dungeon.generator.constants;

import com.sergey.zhuravlev.dungeon.generator.enums.Direction;

public class CloseEnd {

    private static int[][] NORTH_WALLED_END = {{0,-1},{1,-1},{1,0},{1,1},{0,1}};
    private static int[][] NORTH_CLOSE_END = {{0, 0}};
    private static int[] NORTH_RECURSE_END = {-1, 0};

    private static int[][] SOUTH_WALLED_END = {{0,-1},{-1,-1},{-1,0},{-1,1},{0,1}};
    private static int[][] SOUTH_CLOSE_END = {{0,0}};
    private static int[] SOUTH_RECURSE_END = {1,0};

    private static int[][] WEST_WALLED_END = {{-1,0},{-1,1},{0,1},{1,1},{1,0}};
    private static int[][] WEST_CLOSE_END = {{0,0}};
    private static int[] WEST_RECURSE_END = {0,-1};

    private static int[][] EAST_WALLED_END = {{-1,0},{-1,-1},{0,-1},{1,-1},{1,0}};
    private static int[][] EAST_CLOSE_END = {{0,0}};
    private static int[] EAST_RECURSE_END = {0,1};

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
                return null;
        }
    }

    public static int[][] getCloseEnd(Direction direction) {
        switch (direction) {
            case NORTH:
                return NORTH_CLOSE_END;
            case SOUTH:
                return SOUTH_CLOSE_END;
            case WEST:
                return WEST_CLOSE_END;
            case EAST:
                return EAST_CLOSE_END;
            default:
                return null;
        }
    }

    public static int[] getRecurseEnd(Direction direction) {
        switch (direction) {
            case NORTH:
                return NORTH_RECURSE_END;
            case SOUTH:
                return SOUTH_RECURSE_END;
            case WEST:
                return WEST_RECURSE_END;
            case EAST:
                return EAST_RECURSE_END;
            default:
                return null;
        }
    }

}
