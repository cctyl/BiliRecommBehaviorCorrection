package io.github.cctyl.domain.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.cctyl.domain.enumeration.AccessType;
import io.github.cctyl.domain.enumeration.DictType;
import io.github.cctyl.domain.po.Dict;
import io.github.cctyl.domain.po.WhiteListRule;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class WhiteListRuleAddUpdateDto {

    @Schema(description = "白名单id")
    private String id;

    /**
     * 标签名关键词
     */
    @Schema(description = "标签名关键词")
    private List<String> tagNameList ;

    /**
     * 视频描述应当包含的关键词
     */
    @Schema(description = "视频描述应当包含的关键词")
    private List<String> descKeyWordList ;


    /**
     * 视频标题应当包含的关键词
     */
    @Schema(description = "视频标题应当包含的关键词")
    private List<String> titleKeyWordList;

    /**
     * 封面信息应当包含的关键词
     */
    @Schema(description = "封面信息应当包含的关键词")
    private List<String>  coverKeyword =  new ArrayList<>();

    @Schema(description = "白名单描述")
    private String info;


    public WhiteListRule transform() {
        return new WhiteListRule().setId(this.getId()).setInfo(this.getInfo()).setTagNameList(Dict.keyword2Dict(this.getTagNameList(), DictType.TAG, AccessType.WHITE, this.getId())).setCoverKeyword(Dict.keyword2Dict(this.getCoverKeyword(), DictType.COVER, AccessType.WHITE, this.getId())).setTitleKeyWordList(Dict.keyword2Dict(this.getTitleKeyWordList(), DictType.TITLE, AccessType.WHITE, this.getId())).setDescKeyWordList(Dict.keyword2Dict(this.getDescKeyWordList(), DictType.DESC, AccessType.WHITE, this.getId()));
    }



}
