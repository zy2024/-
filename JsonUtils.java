package com.yonyou.fmcg.picc.customer.utils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonUtils {

    public JsonUtils() {
    }

    public static <T> T fromJson(String jsonString, Class<T> clazz) {
        return JSONObject.parseObject(jsonString, clazz);
    }

    public static <T> List<T> fromJsonArray(String jsonArray, Class<T> clazz) {
        return JSONObject.parseArray(jsonArray, clazz);
    }

    public static Collection fromJsonArrayBy(String jsonArray, Class collectionClazz) {


        return JSONObject.parseArray(jsonArray,  collectionClazz);
    }

    /**
     * value为空时，忽略key
     * @param object
     * @return
     */
    public static String toJson(Object object) {
        return JSONObject.toJSONString(object);
    }

    /**
     * value为空时，保留key
     * @param object
     * @return
     */
    public static String toJsonNotIgnoreNullValue(Object object) {
        return JSONObject.toJSONString(object,SerializerFeature.WriteMapNullValue);
    }

//    public static void setDateFormat(String pattern) {
//        JSONObject.(pattern);
//    }

    public static String returnWhenError(String errMsg, Object data) {
        return returnResult(errMsg, data, 0);
    }

    public static String returnWhenSuccess(String errMsg, Object data) {
        return returnResult(errMsg, data, 1);
    }

    public static String returnResult(String errMsg, Object data, int status) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status", status);
        jsonObject.put("msg", errMsg);
        jsonObject.put("data", JSONObject.toJSONString(data, new SerializerFeature[]{SerializerFeature.WriteMapNullValue}));
        return jsonObject.toString();
    }

    /**
     * 获取利用反射获取类里面的值和名称
     *
     * @param obj
     * @return
     * @throws IllegalAccessException
     */
    public static Map<String, Object> objectToMap(Object obj) throws IllegalAccessException {
        Map<String, Object> map = new HashMap<>();
        Class<?> clazz = obj.getClass();
        System.out.println(clazz);
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            String fieldName = field.getName();
            Object value = field.get(obj);
            map.put(fieldName, value);
        }
        return map;
    }
}
