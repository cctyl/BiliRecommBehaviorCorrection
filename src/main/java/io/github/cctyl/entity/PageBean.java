package io.github.cctyl.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class PageBean<T> {

    private List<T> data;
    private long total;
    private long pageSize;
    private long pageNum;

    public boolean hasMore(){
        return this.getTotal()/this.getPageSize()>pageNum;
    }
}
