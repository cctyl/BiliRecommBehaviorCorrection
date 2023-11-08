package io.github.cctyl.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;

@Component
public class MPMetaObject implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.setFieldValByName("createdDate",LocalDateTime.now(),metaObject);
        this.setFieldValByName("isDeleted",0,metaObject);
        this.setFieldValByName("lastModifiedDate",LocalDateTime.now(),metaObject);
        this.setFieldValByName("version",1,metaObject);

    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName("lastModifiedDate",LocalDateTime.now(),metaObject);
    }
}
