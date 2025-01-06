package io.github.cctyl.domain.po;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class SimpleAuditingEntity implements Serializable {

    @TableField(value = "created_date",fill = FieldFill.INSERT)
    private Date createdDate;

    @TableField(value = "last_modified_date",fill = FieldFill.INSERT_UPDATE)
    private Date lastModifiedDate;


}
