package com.sergey.zhuravlev.dungeon.generator.models;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public @Data class Stair {

    private final Integer x;
    private final Integer y;
    private Integer nextX;
    private Integer nextY;
    private String key;

}
