package io.github.cctyl.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
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
public class Config implements Serializable {

    private static final long serialVersionUID = 1L;

                @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    private String url;

    private String name;

    private String value;

    private LocalDate createdDate;

    private LocalDate lastModifiedDate;

    private Boolean isDeleted;

    private Integer version;


}
