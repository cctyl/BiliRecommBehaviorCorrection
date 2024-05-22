package io.github.cctyl.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.github.cctyl.domain.po.Tag;
import io.github.cctyl.domain.po.VideoReply;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author tyl
 * @since 2023-11-17
 */
public interface ReplyService extends IService<VideoReply> {


    Page<VideoReply> getReplyByVideoId(int avid, long page, long limit);

    void saveReply(int avid);
}
