package io.github.cctyl.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.github.cctyl.pojo.AuditingEntity;
import io.github.cctyl.pojo.enumeration.AccessType;
import io.github.cctyl.pojo.enumeration.DictType;
import lombok.Data;

@Data
public class Dict extends AuditingEntity {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 黑白名单类型
     */
    private AccessType accessType;

    /**
     * 字典类型，描述此字典值的用途
     * 例如：标题，描述，封面
     */
    private DictType dictType;

    /**
     * 字典值
     */
    private String value;

}
