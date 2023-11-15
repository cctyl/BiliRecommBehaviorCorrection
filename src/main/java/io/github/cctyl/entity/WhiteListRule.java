package io.github.cctyl.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.github.cctyl.pojo.AuditingEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.*;

/**
 * 白名单规则
 * 它不能仅有关键词，还需要包含关键词对应的：
 * 分区名、
 * 标签名
 * 描述、
 * 标题、
 * 封面
 * 在以上条件中，包含两个以上，才算是真正匹配白名单
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class WhiteListRule extends AuditingEntity implements Serializable {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 标签名
     */
    @TableField(exist = false)
    private List<Dict> tagNameList = new HashSet<>();

    /**
     * 视频描述应当包含的关键词
     */
    @TableField(exist = false)
    private List<Dict> descKeyWordList = new HashSet<>();


    /**
     * 视频标题应当包含的关键词
     */
    @TableField(exist = false)
    private List<Dict> titleKeyWordList= new HashSet<>();

    /**
     * 封面信息应当包含的关键词
     */
    @TableField(exist = false)
    private List<Dict>  coverKeyword =  new HashSet<>();


    private String info;


    @TableField(exist = false)
    private List<Dict>  totalDict =  new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WhiteListRule that = (WhiteListRule) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
