package io.github.cctyl.domain.dto;

import java.io.Serializable;

public record CheckResult (
        boolean total,
        boolean titleMatch,
        boolean descMatch,
        boolean tagMatch,
        boolean midMatch,
        boolean tidMatch,
        boolean coverMatch,
        boolean listRuleMatch

) implements Serializable {


    public static CheckResult error(){
        return new CheckResult(
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false
        );
    }
}
