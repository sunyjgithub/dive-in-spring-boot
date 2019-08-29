package com.imooc.web.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;

/**
 * @description:
 * @author: sunyingji
 * @create: 2019-08-29 17:49
 **/
public class JSONUtils {

    /**
     * instance初始化，延迟加载
     */
    private static ObjectMapper instance = null;

    /**
     * jsonNodeFactory初始化，延迟加载
     */
    private static JsonNodeFactory jsonNodeFactory = null;

    /**
     * 私有构造方法
     */
    private JSONUtils() {

    }

    /**
     * 实例化
     */
    private static synchronized void syncInit() {
        if (instance == null) {
            instance = new ObjectMapper();
        }
    }

    /**
     * 实例获取
     * @return ObjectMapper实例
     */
    public static ObjectMapper getInstance() {
        if (instance == null) {
            syncInit();
        }
        return instance;
    }

    /**
     * 实例化
     */
    private static synchronized void jsonNodeFactoryInit() {
        if (jsonNodeFactory == null) {
            jsonNodeFactory = JsonNodeFactory.instance;
        }
    }

    /**
     * 实例获取
     * @return JsonNodeFactory实例
     */
    public static JsonNodeFactory getJsonNodeFactory() {
        if (jsonNodeFactory == null) {
            jsonNodeFactoryInit();
        }
        return jsonNodeFactory;
    }

    /**
     * 生成一个对象NODE
     * @return 新的对象NODE
     */
    public static ObjectNode newObjNode() {
        return jsonNodeFactory.objectNode();
    }

    /**
     * 生成一个数组NODE
     * @return 新的数组NODE
     */
    public static ArrayNode newArrayNode() {
        return jsonNodeFactory.arrayNode();
    }

    /* -----------------------根据多级PATH来存储数据的API系列------------------------ */

    /**
     * 默认的KEY分隔符
     */
    private static final String JSONKEY_SPLIT_DEFAULT = ".";

    /**
     * 按照key的path(key之间以“.”分隔)，向指定的jsonObject，设置jsonNode.
     * @param rootNode 要放入数据的jsonNode
     * @param keyPath key路径，key按照分隔符来分隔，例如 baisc.name
     * @param valNode 要放入该路径的jsonNode。如果要传入数值，可以传入numberNode，对象可以是ObjectNode，数组是ArrayNode
     */
    public static void putJsonNode(ObjectNode rootNode, String keyPath, JsonNode valNode) {
        genJsonNode(rootNode, keyPath, JSONKEY_SPLIT_DEFAULT, valNode);
    }

    /**
     * 按照key的path，向指定的jsonObject，设置jsonNode
     * @param rootNode 要放入数据的jsonNode
     * @param keyPath key路径，key按照分隔符来分隔，例如 baisc.name
     * @param splitStr key路径的分隔符
     * @param valNode 要放入该路径的jsonNode。如果要传入数值，可以传入numberNode，对象可以是ObjectNode，数组是ArrayNode
     */
    public static void putJsonNode(ObjectNode rootNode, String keyPath, String splitStr, JsonNode valNode) {
        genJsonNode(rootNode, keyPath, splitStr, valNode);
    }

    /**
     * 创建一个jsonObject，根据key的path来创建json下的各级子对象，Path以“.”作为key的分隔符
     * @param keyPath key路径，key按照分隔符来分隔，例如 baisc.name
     * @param valNode 要放入该路径的jsonNode。如果要传入数值，可以传入numberNode，对象可以是ObjectNode，数组是ArrayNode
     * @return 生成好的json objectNode
     */
    public static ObjectNode genJsonNode(String keyPath, JsonNode valNode) {
        return genJsonNode(keyPath, JSONKEY_SPLIT_DEFAULT, valNode);
    }

    /**
     * 创建一个jsonObject，根据key的path来创建json下的各级子对象
     * @param keyPath key路径，key按照分隔符来分隔，例如 baisc.name
     * @param splitStr key路径的分隔符
     * @param valNode 要放入该路径的jsonNode。如果要传入数值，可以传入numberNode，对象可以是ObjectNode，数组是ArrayNode
     * @return 生成好的json objectNode
     */
    public static ObjectNode genJsonNode(String keyPath, String splitStr, JsonNode valNode) {
        return genJsonNode(newObjNode(), keyPath, splitStr, valNode);
    }

    /**
     * 创建一个jsonObject，根据key的path来创建json下的各级子对象
     * @param rootNode 要放入数据的jsonNode
     * @param keyPath key路径，key按照分隔符来分隔，例如 baisc.name
     * @param splitStr key路径的分隔符
     * @param valNode 要放入该路径的jsonNode。如果要传入数值，可以传入numberNode，对象可以是ObjectNode，数组是ArrayNode
     * @return 生成好的json objectNode
     */
    private static ObjectNode genJsonNode(ObjectNode rootNode, String keyPath, String splitStr, JsonNode valNode) {

        // 1. 生成根NODE、用来在每次循环使用的node引用变量
        ObjectNode currentNode = rootNode;

        String[] keyArray = StringUtils.split(keyPath, splitStr);
        for (int i = 0; i < keyArray.length; i++) {
            String key = keyArray[i];
            // 代码走到这里，currentNode一定是objectNode，且不为空
            final JsonNode keyValNode = currentNode.get(key); // 获取KEY在json中的子node
            if (i < keyArray.length - 1) { // 如果不是最后一个KEY，则生成下级node
                if (keyValNode == null) { // 如果KEY对应的node不存在，则创建该key对应的objNode
                    currentNode = currentNode.putObject(key);
                } else if (keyValNode.isObject()) { // 如果存在，并且类型为object
                    currentNode = (ObjectNode) keyValNode;
                } else { // 如果node是其他类型的，则直接抛出异常
                    throw new JsonException(String.format("给定的keyPath参数错误,KEY[%s]对应的val不是一个对象!", key));
                }
                continue;
            }
            // 如果是最后一个KEY,直接覆盖原值
            currentNode.set(key, valNode);
        }
        return rootNode;
    }

    /**
     * json处理异常
     */
    public static class JsonException extends RuntimeException {

        public JsonException() {
        }

        private JsonException(String message) {
            super(message);
        }

        public JsonException(String message, Throwable cause) {
            super(message, cause);
        }

        public JsonException(Throwable cause) {
            super(cause);
        }

        public JsonException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }
}
