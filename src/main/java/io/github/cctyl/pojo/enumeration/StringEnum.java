package io.github.cctyl.pojo.enumeration;

import com.baomidou.mybatisplus.annotation.IEnum;

public  interface StringEnum  extends IEnum<String> {
    @Override
    default String getValue() {
        return this.toString();
    }
}
