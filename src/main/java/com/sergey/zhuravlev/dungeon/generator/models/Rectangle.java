package com.sergey.zhuravlev.dungeon.generator.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public @Data class Rectangle {

    private Integer x;
    private Integer y;
    private Integer height;
    private Integer width;

}
