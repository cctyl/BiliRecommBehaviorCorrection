package io.github.cctyl.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.github.cctyl.pojo.AuditingEntity;
import io.github.cctyl.pojo.enumeration.CookieHeaderType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;

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
public class CookieHeaderData  extends AuditingEntity {

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    private String url;

    private String ckey;

    private String cvalue;

    private CookieHeaderType type;

}
