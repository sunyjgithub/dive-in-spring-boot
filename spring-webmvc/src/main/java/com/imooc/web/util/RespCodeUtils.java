package com.imooc.web.util;

import org.apache.commons.lang3.StringUtils;

/**
 * @description:
 * @author: sunyingji
 * @create: 2019-08-29 17:43
 **/
public final class RespCodeUtils {

    public static final String RESULT_CODE__SUCC = "0000";
    public static final String RESULT_CODE__INVALID_INPUT = "0001";
    public static final String RESULT_CODE__INVALID_BUSINESS = "0002";
    public static final String RESULT_CODE__INNER_ERROR = "0003";
    public static final String RESULT_CODE__AUTH_BUSINESS_FAILED = "0004";

    private RespCodeUtils() {
    }

    public static String code(String system, String model, String Code) {
        return system.concat(model).concat(Code);
    }

    public static boolean isSucc(String code) {
        return StringUtils.endsWith(code, "0000");
    }
}
