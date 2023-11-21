package io.github.cctyl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.cctyl.entity.Tag;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author tyl
 * @since 2023-11-17
 */
public interface TagService extends IService<Tag> {

    List<Tag> saveIfNotExists(List<Tag> tags);

    List<Tag> findByVideoId(String id);
}
