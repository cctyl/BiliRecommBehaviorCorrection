package io.github.cctyl.utils;

import io.github.cctyl.utils.LambdaUtil;
import io.github.cctyl.utils.SFunction;
import lombok.experimental.UtilityClass;
import org.springframework.cglib.core.CodeGenerationException;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 反射工具类
 */
@UtilityClass
public class ReflectUtil extends ReflectionUtils {



	/**
	 * 获取 类属性
	 * @param clazz 类信息
	 * @param fieldName 属性名
	 * @return Field
	 */
	@Nullable
	public static Field getField(Class<?> clazz, String fieldName) {
		while (clazz != Object.class) {
			try {
				return clazz.getDeclaredField(fieldName);
			} catch (NoSuchFieldException e) {
				clazz = clazz.getSuperclass();
			}
		}
		return null;
	}

    /**
     * 获取 类属性
     * @param clazz 类信息
     * @param fieldName 属性名
     * @return Field
     */
    @Nullable
    public static Class<?> getFieldType(Class<?> clazz, String fieldName) {
        Field field = getField(clazz, fieldName);
        if (field==null){
            throw new RuntimeException("field:"+fieldName+"not found!");
        }
        return field.getType();
    }

    /**
     * 获取属性名
     * @param column lambda类型的字段
     * @param <T> 实体类类型
     * @return
     */
    public static <T> String getPropertyName(SFunction<T, ?> column) {
        SerializedLambda serializedLambda = LambdaUtil.getSerializedLambda(column);
        Field field = LambdaUtil.extractColum(serializedLambda);
        return field.getName();
    }


    /**
     * 获取指定对象上指定属性的值
     * @param column lambda类型的字段
     * @return Field
     */
    @Nullable
    public static <T,C> C getFieldValue(SFunction<T, C> column, Object source) {
        String fieldName = getPropertyName(column);
        Object fieldValue = getFieldValue(source.getClass(), fieldName, source);
        return (C) fieldValue;
    }

    /**
     * 将指定对象转换为指定字节码类型
     * @param obj 源对象
     * @param tClass 字节码
     * @param <T>
     * @return
     */
    private static <T> T convertObjectToT(Object obj, Class<T> tClass) {
        // 判断参数是否为 null 或对象本身就属于 T 类型
        if (obj == null || tClass.isInstance(obj)) {
            return (T) obj;
        }
        // 将 obj 转换为 T 类型
        return tClass.cast(obj);
    }

    /**
     * 获取指定对象上指定属性的值
     * @param clazz 类信息
     * @param fieldName 属性名
     * @return Field
     */
    @Nullable
    public  static Object getFieldValue(Class<?> clazz, String fieldName, Object source)  {
        Field field = getField(clazz, fieldName);
        if (field == null) {
            throw new RuntimeException("field " + fieldName + " not found in class:" + clazz.getName());
        }
        try {
            field.setAccessible(true);
            Object result = field.get(source);
            return result;
        } catch (IllegalAccessException e) {
            throw new CodeGenerationException(e);
        } finally {
            field.setAccessible(false);
        }
    }

    /**
     * 给类中指定字段设置指定值
     * @param source
     * @param value
     */
	public static <T,C> void setFieldValue(SFunction<T, C> column, Object source,C value)  {
		String fieldName = getPropertyName(column);
		setFieldValue(source.getClass(),fieldName,source,value);
	}

    /**
     * 给类中指定字段设置指定值
     * @param clazz
     * @param fieldName
     * @param source
     * @param value
     */
    public static void setFieldValue(Class<?> clazz, String fieldName, Object source,Object value)  {
        Field field = getField(clazz, fieldName);
        if (field == null) {
            throw new RuntimeException("field " + fieldName + " not found in class:" + clazz.getName());
        }
        try {
            field.setAccessible(true);
            field.set(source,value);
        } catch (IllegalAccessException e) {
            throw new CodeGenerationException(e);
        } finally {
            field.setAccessible(false);
        }
    }

    /**
     * 将对象转换为Map
     *
     * @param obj
     * @return Map<String, Object>
     */
    public static Map<String, Object> objectToMap(Object obj) {
        Map<String, Object> map = new HashMap<>();
        Class<?> cla = obj.getClass();
        Field[] fields = cla.getDeclaredFields();
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                String keyName = field.getName();
                Object value = field.get(obj);
                map.put(keyName, value);
            }
        } catch (IllegalAccessException e) {
            throw new CodeGenerationException(e);
        } finally {
            for (Field field : fields) {
                field.setAccessible(false);
            }
        }

        map.remove("serialVersionUID");

        return map;
    }


    /**
	 * 获取 所有 field 属性上的注解
	 * @param clazz 类
	 * @param fieldName 属性名
	 * @param annotationClass 注解
	 * @param <T> 注解泛型
	 * @return 注解
	 */
	@Nullable
	public static <T extends Annotation> T getAnnotation(Class<?> clazz, String fieldName, Class<T> annotationClass) {
		Field field = ReflectUtil.getField(clazz, fieldName);
		if (field == null) {
			return null;
		}
		return field.getAnnotation(annotationClass);
	}

    /**
     * 获取当前方法的调用者
     * 示例： A 调用 B，B中使用 getEnclosingMethodName，此时获取的结果是A的方法名
     * @return
     */
    public static String getEnclosingMethodName() {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
        return stackTraceElement.getClassName() +"."+ stackTraceElement.getMethodName();
    }
}
