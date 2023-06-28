package io.github.cctyl.api;


import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

/**
 * 百度相关api
 */
@ConditionalOnExpression("'${api.service}'.contains('baidu')")
@Component
public class BaiduApi {
}
