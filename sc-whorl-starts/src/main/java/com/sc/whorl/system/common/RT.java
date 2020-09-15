package com.sc.whorl.system.common;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RT<T> implements Serializable {
    public static final Integer SUCCESS = 0;
    public static final Integer INTERNAL_SERVER_ERROR = -99;
    public static final Integer PARAM_VALID_ERROR = -1;
    private Integer resultCode;
    private T result;
    /**
     * 过滤字段：指定需要序列化的字段
     */
    private transient Map<Class<?>, Set<String>> includes;

    /**
     * 过滤字段：指定不需要序列化的字段
     */
    private transient Map<Class<?>, Set<String>> excludes;

    public RT<T> include(Class<?> type, String... fields) {
        return include(type, Arrays.asList(fields));
    }

    public RT<T> include(Class<?> type, Collection<String> fields) {
        if (includes == null) {
            includes = new HashMap<>();
        }
        if (fields == null || fields.isEmpty()) {
            return this;
        }
        fields.forEach(field -> {
            if (field.contains(".")) {
                String tmp[] = field.split("[.]", 2);
                try {
                    Field field1 = type.getDeclaredField(tmp[0]);
                    if (field1 != null) {
                        include(field1.getType(), tmp[1]);
                    }
                } catch (Exception ignore) {
                }
            } else {
                getStringListFromMap(includes, type).add(field);
            }
        });
        return this;
    }

    public RT<T> exclude(Class type, Collection<String> fields) {
        if (excludes == null) {
            excludes = new HashMap<>();
        }
        if (fields == null || fields.isEmpty()) {
            return this;
        }
        fields.forEach(field -> {
            if (field.contains(".")) {
                String tmp[] = field.split("[.]", 2);
                try {
                    Field field1 = type.getDeclaredField(tmp[0]);
                    if (field1 != null) {
                        exclude(field1.getType(), tmp[1]);
                    }
                } catch (Exception ignore) {
                }
            } else {
                getStringListFromMap(excludes, type).add(field);
            }
        });
        return this;
    }

    public RT<T> exclude(Collection<String> fields) {
        if (excludes == null) {
            excludes = new HashMap<>();
        }
        if (fields == null || fields.isEmpty()) {
            return this;
        }
        Class type;
        if (getResult() != null) {
            type = getResult().getClass();
        } else {
            return this;
        }
        exclude(type, fields);
        return this;
    }

    public RT<T> include(Collection<String> fields) {
        if (includes == null) {
            includes = new HashMap<>();
        }
        if (fields == null || fields.isEmpty()) {
            return this;
        }
        Class type;
        if (getResult() != null) {
            type = getResult().getClass();
        } else {
            return this;
        }
        include(type, fields);
        return this;
    }

    public RT<T> exclude(Class type, String... fields) {
        return exclude(type, Arrays.asList(fields));
    }

    public RT<T> exclude(String... fields) {
        return exclude(Arrays.asList(fields));
    }

    public RT<T> include(String... fields) {
        return include(Arrays.asList(fields));
    }
    protected Set<String> getStringListFromMap(Map<Class<?>, Set<String>> map, Class type) {
        return map.computeIfAbsent(type, k -> new HashSet<>());
    }

    private RT(Integer resultCode) {
        this.resultCode = resultCode;
    }

    private RT(Integer resultCode, T message) {
        this.resultCode = resultCode;
        this.result = message;
    }

    public static RT success() {
        return new RT(SUCCESS, "调用成功!");
    }

    public static RT error() {
        return new RT(INTERNAL_SERVER_ERROR, "调用失败!");
    }

    public static RT error(String message) {
        return new RT(INTERNAL_SERVER_ERROR, message);
    }

    public static RT error(Integer resultCode, String message) {
        return new RT(resultCode, message);
    }

    public Integer getResultCode() {
        return this.resultCode;
    }

    public RT setResultCode(Integer resultCode) {
        this.resultCode = resultCode;
        return this;
    }

    public T getResult() {
        return this.result;
    }

    public RT setResult(T result) {
        this.result = result;
        return this;
    }
}