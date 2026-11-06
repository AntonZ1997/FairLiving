package com.fairliving.backend.task.dto;

import java.util.UUID;

public record MemberWorkload(
        UUID memberId,
        double workload,
        int openTaskCount
) {
}
