package com.sergey.zhuravlev.dungeon.generator;

import com.sergey.zhuravlev.dungeon.generator.constants.CellConstant;
import com.sergey.zhuravlev.dungeon.generator.constants.CloseEnd;
import com.sergey.zhuravlev.dungeon.generator.constants.StairEnd;
import com.sergey.zhuravlev.dungeon.generator.enums.*;
import com.sergey.zhuravlev.dungeon.generator.models.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DungeonFactory {

    @Getter
    @RequiredArgsConstructor
    public static class Sill {

        private final Integer row;
        private final Integer column;
        private final Direction direction;
        private final Integer doorRow;
        private final Integer doorColumn;
        private final int outId;

    }

    private final Integer rows;
    private final Integer columns;

    private final Mask layout;

    private final Integer minRoomSize;
    private final Integer maxRoomSize;
    private final RoomLayout roomLayout;
    private final CorridorLayout corridorLayout;

    private final Integer removeDeadendsPercentage;
    private final Integer stairs;

    private Integer halfRows;
    private Integer halfColumns;
    private Integer maxRow;
    private Integer maxColumn;
    private Integer rooms;

    private Integer roomBase;
    private Integer roomRadix;

    private Random random;

    public Dungeon createDungeon(Long seed) {
        Dungeon dungeon = new Dungeon(rows, columns);
        random = new Random(seed);
        rooms = 0;
        halfRows = rows / 2;
        halfColumns = columns / 2;
        maxRow = rows - 1;
        maxColumn = columns - 1;
        assert (rows == halfRows * 2);
        assert (columns == halfColumns * 2);

        roomBase = (maxRoomSize + 1) / 2;
        roomRadix = (maxRoomSize - minRoomSize) / 2 + 1;

        dungeon = initCells(dungeon);
        dungeon = emplaceRooms(dungeon);
        dungeon = openRooms(dungeon);
        dungeon = corridors(dungeon);
        if (stairs > 0) {
            dungeon = emplaceStairs(dungeon);
        }
        dungeon = cleanDungeon(dungeon);

        return dungeon;
    }

    private Dungeon initCells(Dungeon dungeon) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                dungeon.getCells()[r][c] = CellConstant.NOTHING;
            }
        }
        if (layout.getData() != null) {
            dungeon = maskCells(dungeon, layout.getData());
        } else if (layout.equals(Mask.ROUND)) {
            dungeon = roundMask(dungeon);
        }
        return dungeon;
    }

    private Dungeon maskCells(Dungeon dungeon, int[][] mask) {
        double rX = mask.length * 1.0 / rows;
        double cX = mask[0].length * 1.0 / columns;

        int[][] cell = dungeon.getCells();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < rows; c++) {
                if (mask[(int) (r * rX)][(int) (c * cX)] == 0) {
                    cell[r][c] = CellConstant.BLOCKED;
                }
            }
        }
        return dungeon;
    }

    private Dungeon roundMask(Dungeon dungeon) {
        int centerR = rows / 2;
        int centerC = columns / 2;

        int[][] cell = dungeon.getCells();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                double d = Math.sqrt(Math.pow(r - centerR, 2) + Math.pow(c - centerC, 2));
                if (d > centerC) {
                    cell[r][c] = CellConstant.BLOCKED;
                }
            }
        }
        return dungeon;
    }

    private Dungeon emplaceRooms(Dungeon dungeon) {
        switch (roomLayout) {
            case PACKED:
                dungeon = packRooms(dungeon);
                break;
            case SCATTERED:
                dungeon = scatterRooms(dungeon);
                break;
            default:
                throw new IllegalArgumentException(roomLayout.name());
        }
        return dungeon;
    }

    private Dungeon packRooms(Dungeon dungeon) {
        int[][] cell = dungeon.getCells();

        for (int y = 0; y < halfRows; y++) {
            int r = (y * 2) + 1;
            for (int x = 0; x < halfColumns; x++) {
                int c = (x * 2) + 1;

                if ((cell[r][c] & CellConstant.ROOM) != 0) {
                    continue;
                }

                if ((y == 0 || x == 0) && random.nextBoolean()) {
                    continue;
                }

                dungeon = emplaceRoom(dungeon, new Rectangle(x, y, null, null));
            }
        }
        return dungeon;
    }

    private Dungeon scatterRooms(Dungeon dungeon) {
        int rooms = allocRooms(dungeon);

        for (int i = 0; i < rooms; i++) {
            dungeon = emplaceRoom(dungeon, new Rectangle());
        }
        return dungeon;
    }

    private int allocRooms(Dungeon dungeon) {
        int dungeonArea = columns * rows;
        int roomArea = maxRoomSize * maxRoomSize;
        int rooms = dungeonArea / roomArea;
        return rooms;
    }

    private Dungeon emplaceRoom(Dungeon dungeon, Rectangle proto) {
        if (rooms == 999) {
            return dungeon;
        }

        int[][] cell = dungeon.getCells();

        proto = setRoom(dungeon, proto, roomBase, roomRadix);

        int rowStart = proto.getY() * 2 + 1;
        int columnStart = proto.getX() * 2 + 1;
        int rowEnd = (proto.getY() + proto.getHeight()) * 2 - 1;
        int columnEnd = (proto.getX() + proto.getWidth()) * 2 - 1;

        if (rowStart < 1 || rowEnd > maxRow || rowStart > rowEnd) {
            return dungeon;
        }
        if (columnStart < 1 || columnEnd > maxColumn || columnStart > columnEnd) {
            return dungeon;
        }

        // check for collisions with existing rooms
        Map<Integer, Integer> hit = soundRoom(dungeon, rowStart, columnStart, rowEnd, columnEnd);
        if (hit == null) {
            return dungeon;
        }
        int hitsCount = hit.size();
        int roomId;

        if (hitsCount == 0) {
            roomId = rooms + 1;
            rooms = roomId;
        } else {
            return dungeon;
        }

        // emplace room
        for (int r = rowStart; r <= rowEnd; r++) {
            for (int c = columnStart; c <= columnEnd; c++) {
                if ((cell[r][c] & CellConstant.ENTRANCE) != 0) {
                    cell[r][c] &= ~CellConstant.ENTRANCE;
                } else if ((cell[r][c] & CellConstant.PERIMETER) != 0) {
                    cell[r][c] &= ~CellConstant.PERIMETER;
                }
                cell[r][c] |= CellConstant.ROOM | (roomId << 6);
            }
        }
        int height = ((rowEnd - rowStart) + 1) * 10;
        int width = ((columnEnd - columnStart) + 1) * 10;

        Room room = new Room(roomId,
                rowStart, columnStart,
                rowStart, rowEnd,
                columnStart, columnEnd,
                height, width,
                height * width,
                new HashMap<>());
        dungeon.getRooms().put(roomId, room);

        for (int r = rowStart - 1; r <= rowEnd + 1; r++) {
            if ((cell[r][columnStart - 1] & (CellConstant.ROOM | CellConstant.ENTRANCE)) == 0) {
                cell[r][columnStart - 1] |= CellConstant.PERIMETER;
            }
            if ((cell[r][columnEnd + 1] & (CellConstant.ROOM | CellConstant.ENTRANCE)) == 0) {
                cell[r][columnEnd + 1] |= CellConstant.PERIMETER;
            }
        }
        for (int c = columnStart - 1; c <= columnEnd + 1; c++) {
            if ((cell[rowStart - 1][c] & (CellConstant.ROOM | CellConstant.ENTRANCE)) == 0) {
                cell[rowStart - 1][c] |= CellConstant.PERIMETER;
            }
            if ((cell[rowEnd + 1][c] & (CellConstant.ROOM | CellConstant.ENTRANCE)) == 0) {
                cell[rowEnd + 1][c] |= CellConstant.PERIMETER;
            }
        }

        return dungeon;
    }

    private Rectangle setRoom(Dungeon dungeon, Rectangle proto, Integer base, Integer radix) {
        if (proto.getHeight() == null) {
            if (proto.getY() != null) {
                int a = halfRows - base - proto.getY();
                a = Math.max(a, 0);
                int r = (a < radix) ? a : radix;

                proto.setHeight((r <= 0) ? 0 : random.nextInt(r) + base);
            } else {
                proto.setHeight((radix <= 0) ? 0 : random.nextInt(radix) + base);
            }
        }
        if (proto.getWidth() == null) {
            if (proto.getX() != null) {
                int a = halfColumns - base - proto.getX();
                a = Math.max(a, 0);
                int r = (a < radix) ? a : radix;

                proto.setWidth((r <= 0) ? 0 : random.nextInt(r) + base);
            } else {
                proto.setWidth((radix <= 0) ? 0 : random.nextInt(radix) + base);
            }
        }
        if (proto.getY() == null) {
            proto.setY(random.nextInt(halfRows - proto.getHeight()));
        }

        if (proto.getX() == null) {
            proto.setX(random.nextInt(halfColumns - proto.getWidth()));
        }
        return proto;
    }

    private Map<Integer, Integer> soundRoom(Dungeon dungeon, int rowStart, int columnStart, int rowEnd, int columnEnd) {
        int[][] cell = dungeon.getCells();
        Map<Integer, Integer> hit = new HashMap<>();

        for (int r = rowStart; r <= rowEnd; r++) {
            for (int c = columnStart; c <= columnEnd; c++) {
                if ((cell[r][c] & CellConstant.BLOCKED) != 0) {
                    return null;
                }
                if ((cell[r][c] & CellConstant.ROOM) != 0) {
                    int id = (cell[r][c] & CellConstant.ROOM_ID) >> 6;
                    hit.merge(id, 1, Integer::sum);
                }
            }
        }
        return hit;
    }

    private Dungeon openRooms(Dungeon dungeon) {
        for (int id = 1; id <= rooms; id++) {
            dungeon = openRoom(dungeon, dungeon.getRooms().get(id));
        }
        return dungeon;
    }

    private Dungeon openRoom(Dungeon dungeon, Room room) {
        List<Sill> sills = doorSills(dungeon, room);
        if (sills.isEmpty()) {
            return dungeon;
        }
        int opens = allocOpens(dungeon, room);
        int[][] cell = dungeon.getCells();

        int i = 0;
        while (i < opens) {
            Sill sill = null;
            if (sills.size() > 0) {
                sill = sills.remove(random.nextInt(sills.size()));
            }
            if (sill == null) {
                break;
            }
            int doorRow = sill.getDoorRow();
            int doorColumn = sill.getDoorColumn();
            int doorCell = cell[doorRow][doorColumn];
            if ((doorCell & CellConstant.DOORSPACE) != 0) {
                continue;
            }

            int outId = sill.getOutId();
            if (outId != 0) {
                String connect = sort(room.getId(), outId).stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining("-"));
                dungeon.getConnects().merge(connect, 1, Integer::sum);
            }

            int openRow = sill.getRow();
            int openColumn = sill.getColumn();
            Direction openDirection = sill.getDirection();

            for (int x = 0; x < 3; x++) {
                int row = openRow + openDirection.getY() * x;
                int column = openColumn + openDirection.getX() * x;

                cell[row][column] &= ~CellConstant.PERIMETER;
                cell[row][column] |= CellConstant.ENTRANCE;
            }
            int doorType = generateDoorType();
            Door door = new Door(doorColumn, doorRow);

            if (doorType == CellConstant.ARCH) {
                cell[doorRow][doorColumn] |= CellConstant.ARCH;
                door.setKey("arch");
                door.setType(DoorType.ARCHWAY);
            } else if (doorType == CellConstant.DOOR) {
                cell[doorRow][doorColumn] |= CellConstant.DOOR;
                cell[doorRow][doorColumn] |= ('o' << 24);
                door.setKey("open");
                door.setType(DoorType.UNLOCKED_DOOR);
            } else if (doorType == CellConstant.LOCKED) {
                cell[doorRow][doorColumn] |= CellConstant.LOCKED;
                cell[doorRow][doorColumn] |= ('x' << 24);
                door.setKey("lock");
                door.setType(DoorType.LOCKED_DOOR);
            } else if (doorType == CellConstant.TRAPPED) {
                cell[doorRow][doorColumn] |= CellConstant.TRAPPED;
                cell[doorRow][doorColumn] |= ('t' << 24);
                door.setKey("trap");
                door.setType(DoorType.TRAPPED_DOOR);
            } else if (doorType == CellConstant.SECRET) {
                cell[doorRow][doorColumn] |= CellConstant.SECRET;
                cell[doorRow][doorColumn] |= ('s' << 24);
                door.setKey("secret");
                door.setType(DoorType.SECRET_DOOR);
            } else if (doorType == CellConstant.PORTC) {
                cell[doorRow][doorColumn] |= CellConstant.PORTC;
                cell[doorRow][doorColumn] |= ('#' << 24);
                door.setKey("portc");
                door.setType(DoorType.PORTCULLIS);
            }
            if (outId != 0) {
                door.setOutId(outId);
            }
            if (door != null) {
                if (!room.getDoors().containsKey(openDirection)) {
                    room.getDoors().put(openDirection, new ArrayList<>());
                }
                room.getDoors().get(openDirection).add(door);
            }
            i++;
        }
        return dungeon;
    }

    private List<Sill> doorSills(Dungeon dungeon, Room room) {
        int[][] cell = dungeon.getCells();
        List<Sill> sills = new ArrayList<>();

        if (room.getNorth() >= 3) {
            for (int c = room.getWest(); c <= room.getEast(); c += 2) {
                Sill sill = checkSill(cell, room, room.getNorth(), c, Direction.NORTH);
                if (sill != null) {
                    sills.add(sill);
                }
            }
        }
        if (room.getSouth() <= (rows - 3)) {
            for (int c = room.getWest(); c <= room.getEast(); c += 2) {
                Sill sill = checkSill(cell, room, room.getSouth(), c, Direction.SOUTH);
                if (sill != null) {
                    sills.add(sill);
                }
            }
        }
        if (room.getWest() >= 3) {
            for (int r = room.getNorth(); r <= room.getSouth(); r += 2) {
                Sill sill = checkSill(cell, room, r, room.getWest(), Direction.WEST);
                if (sill != null) {
                    sills.add(sill);
                }
            }
        }
        if (room.getEast() <= (columns - 3)) {
            for (int r = room.getNorth(); r <= room.getSouth(); r += 2) {
                Sill sill = checkSill(cell, room, r, room.getEast(), Direction.EAST);
                if (sill != null) {
                    sills.add(sill);
                }
            }
        }
        Collections.shuffle(sills, random);
        return sills;
    }

    private Sill checkSill(int[][] cell, Room room, int sillRow, int sillColumn, Direction direction) {
        int doorRow = sillRow + direction.getY();
        int doorColumn = sillColumn + direction.getX();
        int doorCell = cell[doorRow][doorColumn];
        if ((doorCell & CellConstant.PERIMETER) == 0) {
            return null;
        }
        if ((doorCell & CellConstant.BLOCK_DOOR) != 0) {
            return null;
        }
        int outRow = doorRow + direction.getY();
        int outColumn = doorColumn + direction.getX();
        int outCell = cell[outRow][outColumn];

        if ((outCell & CellConstant.BLOCKED) != 0) {
            return null;
        }

        int outId = 0;
        if ((outCell & CellConstant.ROOM) != 0) {
            outId = (outCell & CellConstant.ROOM_ID) >> 6;
            if (outId == room.getId()) {
                return null;
            }
        }
        return new Sill(sillRow, sillColumn, direction, doorRow, doorColumn, outId);
    }

    private int generateDoorType() {
        int i = random.nextInt(110);

        if (i < 15) {
            return CellConstant.ARCH;
        } else if (i < 60) {
            return CellConstant.DOOR;
        } else if (i < 75) {
            return CellConstant.LOCKED;
        } else if (i < 90) {
            return CellConstant.TRAPPED;
        } else if (i < 100) {
            return CellConstant.SECRET;
        } else {
            return CellConstant.PORTC;
        }
    }

    private int allocOpens(Dungeon dungeon, Room room) {
        int roomHeight = ((room.getSouth() - room.getNorth()) / 2) + 1;
        int roomWidth = ((room.getEast() - room.getWest()) / 2) + 1;
        int flumph = (int) Math.sqrt(roomWidth * roomHeight);
        if (flumph == 0) {
            return 0;
        }
        return flumph + random.nextInt(flumph);
    }

    private Dungeon corridors(Dungeon dungeon) {
        int[][] cell = dungeon.getCells();

        for (int y = 1; y < halfRows; y++) {
            int r = (y * 2) + 1;
            for (int x = 1; x < halfColumns; x++) {
                int c = (x * 2) + 1;

                if ((cell[r][c] & CellConstant.CORRIDOR) != 0) {
                    continue;
                }
                dungeon = tunnel(dungeon, x, y, null);
            }
        }
        return dungeon;
    }

    private Dungeon tunnel(Dungeon dungeon, int x, int y, Direction lastDirection) {
        List<Direction> directions = createTunnelDirections(dungeon, lastDirection);

        for (Direction direction : directions) {
            if (openTunnel(dungeon, x, y, direction)) {
                int nextX = x + direction.getX();
                int nextY = y + direction.getY();
                dungeon = tunnel(dungeon, nextX, nextY, lastDirection);
            }
        }
        return dungeon;
    }

    private List<Direction> createTunnelDirections(Dungeon dungeon, Direction lastDirection) {
        List<Direction> directions = Arrays.asList(Direction.values());
        Collections.shuffle(directions, random);
        int mess = 100 - corridorLayout.getMess();

        if (lastDirection != null && mess != 0) {
            if (random.nextInt(100) < mess) {
                directions.add(0, lastDirection);
            }
        }
        return directions;
    }

    private boolean openTunnel(Dungeon dungeon, int x, int y, Direction direction) {
        int currentColumn = (x * 2) + 1;
        int currentRow = (y * 2) + 1;
        int nextColumn = ((x + direction.getX()) * 2) + 1;
        int nextRow = ((y + direction.getY()) * 2) + 1;
        int midColumn = (currentColumn + nextColumn) / 2;
        int midRow = (currentRow + nextRow) / 2;

        if (soundTunnel(dungeon, midRow, midColumn, nextRow, nextColumn)) {
            return delveTunnel(dungeon, currentRow, currentColumn, nextRow, nextColumn);
        } else {
            return false;
        }
    }

    private boolean soundTunnel(Dungeon dungeon, int midRow, int midColumn, int nextRow, int nextColumn) {
        if (nextRow < 0 || nextRow > rows) {
            return false;
        }
        if (nextColumn < 0 || nextColumn > columns) {
            return false;
        }
        int[][] cell = dungeon.getCells();

        int r1 = Math.min(midRow, nextRow);
        int r2 = Math.max(midRow, nextRow);
        int c1 = Math.min(midColumn, nextColumn);
        int c2 = Math.max(midColumn, nextColumn);

        for (int r = r1; r <= r2; r++) {
            for (int c = c1; c <= c2; c++) {
                if ((cell[r][c] & CellConstant.BLOCK_CORR) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean delveTunnel(Dungeon dungeon, int currentRow, int currentColumn, int nextRow, int nextColumn) {
        int[][] cell = dungeon.getCells();

        int r1 = Math.min(currentRow, nextRow);
        int r2 = Math.max(currentRow, nextRow);
        int c1 = Math.min(currentColumn, nextColumn);
        int c2 = Math.max(currentColumn, nextColumn);

        for (int r = r1; r <= r2; r++) {
            for (int c = c1; c <= c2; c++) {
                cell[r][c] &= ~CellConstant.ENTRANCE;
                cell[r][c] |= CellConstant.CORRIDOR;
            }
        }
        return true;
    }

    private Dungeon emplaceStairs(Dungeon dungeon) {
        if (stairs <= 0) {
            return dungeon;
        }
        List<Stair> stairEnds = createStairEnds(dungeon);
        if (stairEnds == null) {
            return dungeon;
        }
        int[][] cell = dungeon.getCells();

        for (int i = 0; i < stairs; i++) {
            Stair stair = stairEnds.remove(random.nextInt(stairEnds.size()));
            if (stair == null) {
                break;
            }
            int x = stair.getX();
            int y = stair.getY();
            int type = (i < 2) ? i : random.nextInt(2);

            if (type == 0) {
                cell[y][x] |= CellConstant.STAIR_DN;
                cell[y][x] |= ('d' << 24);
                stair.setKey("down");
            } else {
                cell[y][x] |= CellConstant.STAIR_UP;
                cell[y][x] |= 'u' << 24;
                stair.setKey("up");
            }
            dungeon.getStair().add(stair);
        }
        return dungeon;
    }

    private List<Stair> createStairEnds(Dungeon dungeon) {
        int[][] cell = dungeon.getCells();
        List<Stair> stairs = new ArrayList<>();

        for (int i = 0; i < halfRows; i++) {
            int r = (i * 2) + 1;
            COL:
            for (int j = 0; j < halfColumns; j++) {
                int c = (j * 2) + 1;

                if (cell[r][c] != CellConstant.CORRIDOR) {
                    continue;
                }
                if ((cell[r][c] & CellConstant.STAIRS) != 0) {
                    continue;
                }

                for (Direction direction : Direction.values()) {
                    if (checkTunnel(cell, r, c, direction)) {
                        Stair end = new Stair(c, r);
                        int[] n = StairEnd.getNextEnd(direction);
                        end.setNextY(end.getX() + n[1]);
                        end.setNextY(end.getY() + n[0]);
                        stairs.add(end);
                        break COL;
                    }
                }
            }
        }
        return stairs;
    }

    private Dungeon cleanDungeon(Dungeon dungeon) {
        if (removeDeadendsPercentage > 0) {
            dungeon = removeDeadends(dungeon);
        }
        dungeon = fixDoors(dungeon);
        dungeon = fillEmptyBlocks(dungeon);

        return dungeon;
    }

    private Dungeon removeDeadends(Dungeon dungeon) {
        int p = removeDeadendsPercentage;

        return collapseTunnels(dungeon, p);
    }

    private Dungeon collapseTunnels(Dungeon dungeon, int p) {
        if (p == 0) {
            return dungeon;
        }
        boolean all = (p == 100);
        int[][] cell = dungeon.getCells();

        for (int i = 0; i < halfRows; i++) {
            int r = (i * 2) + 1;
            for (int j = 0; j < halfColumns; j++) {
                int c = (j * 2) + 1;

                if ((cell[r][c] & CellConstant.OPENSPACE) == 0) {
                    continue;
                }
                if ((cell[r][c] & CellConstant.STAIRS) != 0) {
                    continue;
                }
                if (!all && random.nextInt(100) >= p) {
                    continue;
                }

                dungeon = collapse(dungeon, r, c);
            }
        }
        return dungeon;
    }

    private Dungeon collapse(Dungeon dungeon, int r, int c) {
        int[][] cell = dungeon.getCells();

        if ((cell[r][c] & CellConstant.OPENSPACE) == 0) {
            return dungeon;
        }
        for (Direction direction : Direction.values()) {
            if (checkTunnel(cell, r, c, direction)) {
                for (int[] p : CloseEnd.getCloseEnd(direction)) {
                    cell[r + p[0]][c + p[1]] = CellConstant.NOTHING;
                }

                int[] p = CloseEnd.getRecurseEnd(direction);
                dungeon = collapse(dungeon, r + p[0], c + p[1]);
            }
        }
        return dungeon;
    }


    private boolean checkTunnel(int[][] cell, int r, int c, Direction direction) {
        int[][] list;

        list = StairEnd.getCorridorEnd(direction);
        for (int[] p : list) {
            if (r + p[0] >= cell.length || r + p[0] < 0 || c + p[1] >= cell[0].length || c + p[1] < 0) {
                return false;
            }
            if (cell[r + p[0]][c + p[1]] != CellConstant.CORRIDOR) {
                return false;
            }
        }

        list = StairEnd.getWalledEnd(direction);
        for (int[] p : list) {
            if (r + p[0] >= cell.length || r + p[0] < 0 || c + p[1] >= cell[0].length || c + p[1] < 0) {
                return false;
            }
            if ((cell[r + p[0]][c + p[1]] & CellConstant.OPENSPACE) != 0) {
                return false;
            }
        }

        return true;
    }

    private Dungeon fixDoors(Dungeon dungeon) {
        int[][] cell = dungeon.getCells();
        boolean[][] fixed = new boolean[cell.length][cell[0].length];

        for (Room room : dungeon.getRooms().values()) {
            for (Direction direction : Direction.values()) {
                if (!room.getDoors().containsKey(direction)) {
                    continue; //work around concurrent modification exception
                }
                List<Door> shiny = new ArrayList<>();
                for (Door door : room.getDoors().get(direction)) {
                    int doorY = door.getY();
                    int doorX = door.getX();
                    int doorCell = cell[doorY][doorX];
                    if ((doorCell & CellConstant.OPENSPACE) == 0) {
                        continue;
                    }

                    if (fixed[doorY][doorX]) {
                        shiny.add(door);
                    } else {
                        Integer outId = door.getOutId();
                        if (outId != null) {
                            Direction openDirection = direction.getOpposite();
                            if (!room.getDoors().containsKey(openDirection)) {
                                room.getDoors().put(openDirection, new ArrayList<>());
                            }
                            room.getDoors().get(openDirection).add(door);
                        }
                        shiny.add(door);
                        fixed[doorY][doorX] = true;
                    }
                }

                if (!shiny.isEmpty()) {
                    room.getDoors().put(direction, shiny);
                } else {
                    room.getDoors().remove(direction);
                }
            }
        }
        return dungeon;
    }

    private Dungeon fillEmptyBlocks(Dungeon dungeon) {
        int[][] cell = dungeon.getCells();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                if ((cell[r][c] & CellConstant.BLOCKED) != 0) {
                    cell[r][c] = CellConstant.NOTHING;
                }
            }
        }
        return dungeon;
    }

    private static List<Integer> sort(Integer object1, Integer object2) {
        if (ObjectUtils.<Integer>compare(object1, object2) >= 0) {
            return Arrays.asList(object1, object2);
        } else {
            return Arrays.asList(object2, object1);
        }
    }

}
