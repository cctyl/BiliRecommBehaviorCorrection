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
public class R {

    private static final int SUCCESS = 20000;
    private static final int FAIL = 50000;

    @Schema(name = "是否成功")
    private Boolean success;

    @Schema(name = "返回码")

    private Integer code;

    @Schema(name = "返回消息")
    private String message;

    @Schema(name = "返回数据")
    private Object data;

    private R() {
    }

    public static R ok() {
        R r = new R();
        r.setSuccess(true);
        r.setCode(SUCCESS);
        r.setMessage("成功");
        return r;
    }

    public static R data(Object o) {
        R r = ok();
        r.setData(o);
        return r;
    }


    public static R error() {
        R r = new R();
        r.setSuccess(false);
        r.setCode(FAIL);
        r.setMessage("失败");
        return r;
    }

    public static R data(String key, Object value) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(key, value);

        R r = ok();
        r.setData(map);
        return r;
    }

    public static R data(Map<String, Object> map) {
        R r = ok();
        r.setData(map);
        return r;
    }
}
