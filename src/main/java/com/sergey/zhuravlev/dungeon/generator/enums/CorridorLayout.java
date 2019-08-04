package com.sergey.zhuravlev.dungeon.generator.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CorridorLayout {

    LABYRINTH(100),
    BENT(50),
    STRAIGHT(0);

    private final int mess;
}
