package io.github.cctyl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.cctyl.entity.Dict;
import io.github.cctyl.mapper.DictMapper;
import io.github.cctyl.pojo.enumeration.AccessType;
import io.github.cctyl.pojo.enumeration.DictType;
import io.github.cctyl.service.DictService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author tyl
 * @since 2023-11-09
 */
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {


    /**
     * 根据两大类型查询字典表
     * @param dictType
     * @param accessType
     * @return
     */
    public List<Dict> findByDictTypeAndAccessType(

            DictType dictType,
            AccessType accessType
    ){
        LambdaQueryWrapper<Dict> eq = new LambdaQueryWrapper<Dict>()
                .eq(Dict::getAccessType, accessType)
                .eq(Dict::getDictType, dictType);
        return this.list(eq);
    }

    /**
     * 获得搜索关键词
     * @return
     */
    @Override
    public List<Dict> findSearchKeyWord() {

        return this.findByDictTypeAndAccessType(
                DictType.SEARCH_KEYWORD,AccessType.OTHER
        );
    }



    @Override
    public List<Dict> findBlackUserId() {
        return this.findByDictTypeAndAccessType(
                DictType.MID,AccessType.BLACK
        );

    }

    @Override
    public List<Dict> addBlackKeyword(Collection<String> param) {
        return this.addDict(param,AccessType.BLACK,DictType.KEYWORD);
    }

    public List<Dict> addDict(Collection<String> param,AccessType accessType,DictType dictType) {
        List<Dict> newDictList = param.stream().map(
                s -> {
                    return new Dict()
                            .setAccessType(accessType)
                            .setDictType(dictType)
                            .setValue(s);
                }
        )
                .collect(Collectors.toList());
        this.saveBatch(newDictList);
        return newDictList;
    }


    @Override
    public List<Dict> findBlackKeyWord() {
        return this.findByDictTypeAndAccessType(DictType.KEYWORD,AccessType.BLACK);
    }

    @Override
    public List<Dict> findBlackTid() {
        return this.findByDictTypeAndAccessType(DictType.TID,AccessType.BLACK);
    }

    @Override
    public List<Dict> findBlackTag() {
        return this.findByDictTypeAndAccessType(DictType.TAG,AccessType.BLACK);
    }

    @Override
    public List<Dict> addBlackTag(Collection<String> param) {
        return    this.addDict(param,AccessType.BLACK,DictType.TAG);
    }

    @Override
    public List<Dict> findWhiteUserId() {
        return this.findByDictTypeAndAccessType(DictType.MID,AccessType.WHITE);
    }


    @Override
    public List<Dict> findWhiteTid() {
        return this.findByDictTypeAndAccessType(DictType.TID,AccessType.WHITE);
    }
}
