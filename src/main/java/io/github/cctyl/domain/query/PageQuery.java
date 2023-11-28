package io.github.cctyl.domain.query;

import lombok.Data;

@Data
public class PageQuery {
    private int page = 1;
    private int size = 10;
    private String sort;
    private boolean isAsc = false;
}
