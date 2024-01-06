package io.github.cctyl.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 通过lambda解析传递的方法，从而拿到需要查询的字段
 * 例如 User::getId
 * <p>
 * 感谢：https://blog.csdn.net/leaderFLY/article/details/119414008
 * 以及: https://baomidou.com
 * 提供的灵感
 */
@Slf4j
public class LambdaUtil {

    public static <T> Field extractColum(SFunction<T, ?> column) {

        SerializedLambda serializedLambda = getSerializedLambda(column);

        // 从lambda信息取出method、field、class等
        //getImplMethodName拿到的就是apply方法的实现类
        String implMethodName = serializedLambda.getImplMethodName();
        // 确保方法是符合规范的get方法，boolean类型是is开头
        if (!implMethodName.startsWith("is") && !implMethodName.startsWith("get")) {
            throw new RuntimeException("get方法名称: " + implMethodName + ", 不符合java bean规范");
        }

        // get方法开头为 is 或者 get，将方法名 去除is或者get，然后首字母小写，就是属性名
        int prefixLen = implMethodName.startsWith("is") ? 2 : 3;

        //截掉get / is
        String fieldName = implMethodName.substring(prefixLen);

        //把属性名的大写去掉
        String firstChar = fieldName.substring(0, 1);
        fieldName = fieldName.replaceFirst(firstChar, firstChar.toLowerCase());

        //开始找到对应的字段
        Field field;
        try {
            field = ReflectionUtils.findField(Class.forName(serializedLambda.getImplClass().replace("/", ".")),fieldName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return field;
    }

    public static <T> Field extractColum(SerializedLambda serializedLambda) {


        // 从lambda信息取出method、field、class等
        //getImplMethodName拿到的就是apply方法的实现类
        String implMethodName = serializedLambda.getImplMethodName();
        // 确保方法是符合规范的get方法，boolean类型是is开头
        if (!implMethodName.startsWith("is") && !implMethodName.startsWith("get")) {
            throw new RuntimeException("get方法名称: " + implMethodName + ", 不符合java bean规范");
        }

        // get方法开头为 is 或者 get，将方法名 去除is或者get，然后首字母小写，就是属性名
        int prefixLen = implMethodName.startsWith("is") ? 2 : 3;

        //截掉get / is
        String fieldName = implMethodName.substring(prefixLen);

        //把属性名的大写去掉
        String firstChar = fieldName.substring(0, 1);
        fieldName = fieldName.replaceFirst(firstChar, firstChar.toLowerCase());

        //开始找到对应的字段
        Field field;
        try {

            field =ReflectionUtils.findField(Class.forName(serializedLambda.getImplClass().replace("/", ".")),fieldName);

            ReflectionUtils.makeAccessible(field);

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return field;
    }

    public static <T> Class<T> getBeanClass(SFunction<T, ?> column) {
        SerializedLambda serializedLambda = getSerializedLambda(column);
        Class<?> aClass = null;
        try {
            aClass = Class.forName(serializedLambda.getImplClass().replace("/", "."));
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage(),e);
        }
        return (Class<T>) aClass;
    }

    public static <T> Class<T> getBeanClass(SerializedLambda serializedLambda) {
        Class<?> aClass = null;
        try {
            aClass = Class.forName(serializedLambda.getImplClass().replace("/", "."));
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage(),e);
        }
        return (Class<T>) aClass;
    }


    public static <T> SerializedLambda getSerializedLambda(SFunction<T, ?> column) {
        Class<? extends SFunction> writeReplaceClass = column.getClass();
        Method writeReplaceMethod = null;
        try {
            writeReplaceMethod = writeReplaceClass.getDeclaredMethod("writeReplace");
        } catch (NoSuchMethodException e) {
            log.error(e.getMessage(),e);
        }

        boolean isAccessible = writeReplaceMethod.isAccessible();
        writeReplaceMethod.setAccessible(true);
        SerializedLambda serializedLambda;
        try {
            serializedLambda = (SerializedLambda) writeReplaceMethod.invoke(column);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        //还原原本的 Accessible属性
        writeReplaceMethod.setAccessible(isAccessible);

        return serializedLambda;
    }
}
