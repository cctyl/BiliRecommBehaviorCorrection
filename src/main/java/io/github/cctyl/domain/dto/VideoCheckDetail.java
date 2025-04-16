package io.github.cctyl.domain.dto;

import io.github.cctyl.domain.po.VideoDetail;

import java.io.Serializable;

public record VideoCheckDetail(
        CheckResult whiteResult,
        CheckResult blackResult,
        String thumbUpReason,
        String blackReason,
        VideoDetail videoDetail) implements Serializable {
}
