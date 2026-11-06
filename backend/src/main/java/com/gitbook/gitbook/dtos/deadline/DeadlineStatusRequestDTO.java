package com.gitbook.gitbook.dtos.deadline;

import com.gitbook.gitbook.models.enums.StatusTask;

public record DeadlineStatusRequestDTO(StatusTask status) {
}
