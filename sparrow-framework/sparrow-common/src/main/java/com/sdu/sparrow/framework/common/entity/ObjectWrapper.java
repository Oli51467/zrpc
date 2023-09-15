package com.sdu.sparrow.framework.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ObjectWrapper<T> {

    private Byte code;

    private String name;

    private T impl;
}
