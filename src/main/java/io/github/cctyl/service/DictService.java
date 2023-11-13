package io.github.cctyl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.cctyl.entity.Dict;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author tyl
 * @since 2023-11-09
 */
public interface DictService extends IService<Dict> {

    List<Dict> findSearchKeyWord();

    List<Dict> findBlackUserId();

    List<Dict> addBlackKeyword(Collection<String> param);

    List<Dict> findBlackKeyWord();

    List<Dict> findBlackTid();

    List<Dict> findBlackTag();

    List<Dict> addBlackTag(Collection<String> param);

    List<Dict>  findWhiteUserId();

    List<Dict> findWhiteTid();

    List<Dict> findWhiteIgnoreKeyWord();
}
