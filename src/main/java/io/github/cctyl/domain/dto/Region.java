package io.github.cctyl.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Region {

    private Integer tid;
    private String code;
    private String name;
    private String desc;
    private String router;
    /**
     * 父id
     */
    private Integer pid;
    private List<Region> children;

    public static List<Region> buildTree(List<Region> list) {

        //给每个节点找子级
        for (Region parent : list) {
            parent.setChildren(list.stream()
                    .filter(region ->  region.getPid()!=null && region.getPid().equals(parent.getTid()))
                    .collect(Collectors.toList()));
        }

        List<Region> first = list
                .stream()
                .filter(region -> region.getPid()==null)
                .collect(Collectors.toList());
        return first;
    }


    public Region(Integer tid, String code, String name, String desc, String router, Integer pid) {
        this.tid = tid;
        this.code = code;
        this.name = name;
        this.desc = desc;
        this.router = router;
        this.pid = pid;
    }
}
