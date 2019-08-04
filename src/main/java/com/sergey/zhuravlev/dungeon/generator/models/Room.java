package com.sergey.zhuravlev.dungeon.generator.models;

import com.sergey.zhuravlev.dungeon.generator.enums.Direction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
public @Data class Room {

    private Integer id;

    private Integer row;
    private Integer column;

    private Integer north;
    private Integer south;
    private Integer west;
    private Integer east;

    private Integer height;
    private Integer width;

    private Integer area;

    private Map<Direction, List<Door>> doors;

}
