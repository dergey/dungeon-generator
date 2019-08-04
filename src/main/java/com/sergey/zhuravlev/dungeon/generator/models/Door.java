package com.sergey.zhuravlev.dungeon.generator.models;

import com.sergey.zhuravlev.dungeon.generator.enums.DoorType;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public @Data class Door {

    private final Integer x;
    private final Integer y;
    private DoorType type;
    private String key;
    private Integer outId;


}
