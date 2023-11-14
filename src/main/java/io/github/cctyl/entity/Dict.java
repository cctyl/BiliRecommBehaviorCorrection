package io.github.cctyl.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.cctyl.pojo.AuditingEntity;
import io.github.cctyl.pojo.enumeration.AccessType;
import io.github.cctyl.pojo.enumeration.DictType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
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

    /**
     * 外部关联id，例如 白名单对象id
     */
    private String outerId;

    public static List<String> transferToValue(Collection<Dict> dictCollection) {
        return dictCollection.stream().map(Dict::getValue).collect(Collectors.toList());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Dict dict = (Dict) o;
        return Objects.equals(id, dict.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
    }

}
