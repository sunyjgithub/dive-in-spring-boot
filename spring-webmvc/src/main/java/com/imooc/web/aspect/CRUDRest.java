package com.imooc.web.aspect;

import com.imooc.web.exception.BaseException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface CRUDRest {


    public static enum Type {
        /**
         * 创建
         */
        Create,
        /**
         * 查询读取
         */
        Read,
        /**
         * 更新
         */
        Update,
        /**
         * 删除
         */
        Delete,
        /**
         * 默认
         */
        Default,
        /**
         * 无操作,当在类上添加注解时，使用该项
         */
        None;
    }

    /**
     * 查询成功
     */
    public static final String SEARCH_SUCCESS = "查询成功";

    /**
     * 添加成功
     */
    public static final String ADD_SUCCESS = "添加成功";

    /**
     * 修改成功
     */
    public static final String UPDATE_SUCCESS = "修改成功";

    /**
     * 删除成功
     */
    public static final String DELETE_SUCCESS = "删除成功";

    /**
     * 存储默认提醒语的Map
     */
    public static Map<Type, String> _defaultSuccStrMap = new HashMap<Type, String>(){{
        this.put(Type.Read, SEARCH_SUCCESS);
        this.put(Type.Create, ADD_SUCCESS);
        this.put(Type.Update, UPDATE_SUCCESS);
        this.put(Type.Delete, DELETE_SUCCESS);
    }};

    /**
     * 操作类型
     * @return 操作类型
     */
    public Type type();

    /**
     * 默认业务模块编码
     */
    public final static String _defaultSuccStr = "操作成功!";

    /**
     * 成功提示语
     * @return 成功提示语
     */
    public String succStr() default _defaultSuccStr;

    /**
     * 默认业务模块编码
     */
    public final static String _defaultFaildStr = "操作失败!";

    /**
     * 失败提示语
     * @return 失败提示语
     */
    public String faildStr() default _defaultFaildStr;

    /**
     * 默认业务模块编码
     */
    public final static String _defaultModelCode = "000";

    /**
     * 业务模块编码
     * @return 业务模块编码
     */
    public String modelCode() default _defaultModelCode;

    /**
     * 默认的异常抛出类
     */
    public final static Class<?> _defaultException = BaseException.class;

    /**
     *      * 自定义异常类型。AOP会放过自定义异常
     *      * @return AOP会放过的自定义异常
     */
    public Class<?> exception() default BaseException.class;
}
