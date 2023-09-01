package com.sdu.irpc.framework.common.util;

import java.util.Random;

public class IdGenerator {

    public long getId() {
        Random random = new Random();
        return random.nextLong(1000000);
    }
}
