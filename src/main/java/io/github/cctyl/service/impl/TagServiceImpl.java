package io.github.cctyl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.cctyl.domain.po.Tag;
import io.github.cctyl.mapper.TagMapper;
import io.github.cctyl.service.TagService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author tyl
 * @since 2023-11-17
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    /**
     * 新增tag 如果数据库中不存在的话
     * 返回包含id的Tag集合
     * @param tags
     * @return
     */
    @Override
    public List<Tag> saveIfNotExists(List<Tag> tags) {

        //查询已存在的tag
        List<Integer> tagIdList = tags.stream().map(Tag::getTagId).collect(Collectors.toList());
        List<Tag> existTagList = this.list(
                new LambdaQueryWrapper<Tag>()
                .in(Tag::getTagId, tagIdList)
        );
        List<Integer> existsTagIdList = existTagList
                .stream().map(Tag::getTagId).collect(Collectors.toList());

        //过滤得到新的tag
        List<Tag> newTagList = tags.stream().filter(tag -> !existsTagIdList.contains(tag.getTagId()))
                .collect(Collectors.toList());

        this.saveBatch(newTagList);

        newTagList.addAll(existTagList);

        return newTagList;
    }

    /**
     * @param id
     * @return
     */
    @Override
    public List<Tag> findByVideoId(String id) {
        return baseMapper.findByVideoId(id);
    }
}
