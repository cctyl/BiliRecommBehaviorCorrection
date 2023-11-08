package io.github.cctyl.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

/**
 * 各Api 所需要用到的cookie 和 header 的集合
 */
@Data
@Accessors(chain = true)
public class ApiHeader implements Serializable {

    /**
     * api地址，不包含参数
     */
    private String url;


    /**
     * 该api将会用到的cookie
     */
    private Map<String, String> cookies;

    /**
     * 该api将会用到的请求头
     */
    private Map<String, String> headers;

}
