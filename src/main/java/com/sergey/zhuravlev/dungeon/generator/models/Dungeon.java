package com.sergey.zhuravlev.dungeon.generator.models;

import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public @Data class Dungeon {

    private Long seed;

    @ToString.Exclude
    private int [][] cells;

    private Map<String, Integer> connects;

    private Map<Integer, Room> rooms;

    private List<Stair> stair;

    public Dungeon(int rows, int columns) {
        this.cells = new int[rows][columns];
        this.connects = new HashMap<>();
        this.rooms = new HashMap<>();
        this.stair = new ArrayList<>();
    }

}
