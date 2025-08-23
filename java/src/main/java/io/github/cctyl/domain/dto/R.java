package io.github.cctyl.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一返回数据
 */
@Data
@Accessors(chain = true)
public class R<T> {

    private static final int SUCCESS = 20000;
    private static final int FAIL = 50000;

    @Schema(name = "是否成功")
    private Boolean success;

    @Schema(name = "返回码")

    private Integer code;

    @Schema(name = "返回消息")
    private String message;

    @Schema(name = "返回数据")
    private T data;

    private R() {
    }

    public static<T> R<T> ok() {
        R<T> r = new R<T>();
        r.setSuccess(true);
        r.setCode(SUCCESS);
        r.setMessage("成功");
        return r;
    }

    public static<T> R<T> data(T o) {
        R<T> r = ok();
        r.setData(o);
        return r;
    }


    public static<T> R<T> error() {
        R<T> r = new R<T>();
        r.setSuccess(false);
        r.setCode(FAIL);
        r.setMessage("失败");
        return r;
    }

    public static R<Map<String, Object>> data(String key, Object value) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(key, value);

        return data(map);
    }

    public static R<Map<String, Object>> data(Map<String, Object> map) {
        R<Map<String, Object>> r = ok();
        r.setData(map);
        return r;
    }
}
