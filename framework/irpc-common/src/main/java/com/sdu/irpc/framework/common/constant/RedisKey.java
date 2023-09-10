package com.sdu.irpc.framework.common.constant;

public class RedisKey {

    public static final Integer TOKEN_RENEWAL_DAYS = 2; // token续期时间
    public static final Integer TOKEN_EXPIRE_DAYS = 5;  // token有效期

    private static final String BASE_KEY = "rabbit:";
    public static String USER_TOKEN_KEY = "user:token_%d";
    public static final String PRODUCT_DETAILS_KEY = "product:details_%s";

    public static final String LUA_INCR_EXPIRE =
            "local key,ttl=KEYS[1],ARGV[1] \n" +
                    " \n" +
                    "if redis.call('EXISTS',key)==0 then   \n" +
                    "  redis.call('SETEX',key,ttl,1) \n" +
                    "  return 1 \n" +
                    "else \n" +
                    "  return tonumber(redis.call('INCR',key)) \n" +
                    "end ";

    public static String getKey(String key, Object... objects) {
        return BASE_KEY + String.format(key, objects);
    }

    public static String getKeyWithString(String key, String objects) {
        return BASE_KEY + key + objects;
    }
}
