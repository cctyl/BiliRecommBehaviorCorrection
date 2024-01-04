package io.github.cctyl.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.github.cctyl.domain.enumeration.Classify;
import io.github.cctyl.domain.enumeration.MediaType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 *
 * </p>
 *
 * @author tyl
 * @since 2023-11-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CookieHeaderData{

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    private String url;

    private String ckey;

    private String cvalue;

    /**
     * 分类
     */
    private Classify classify;

    /**
     * 用途
     */
    private MediaType mediaType;
}
