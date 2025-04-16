package io.github.cctyl.domain.dto;

import cn.hutool.dfa.WordTree;
import io.github.cctyl.domain.po.WhiteListRule;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public record FirstProcessData (
        List<WhiteListRule> whitelistRuleList,
        List<String> whiteUserIdSet,
        List<String> whiteTidSet,
        WordTree whiteTitleKeywordTree,
        WordTree whiteDescKeywordTree,


        Set<String> blackTagSet,
        WordTree blackKeywordTree,
        List<String> blackTidSet,
        List<String> blackUserIdSet
) {
}
