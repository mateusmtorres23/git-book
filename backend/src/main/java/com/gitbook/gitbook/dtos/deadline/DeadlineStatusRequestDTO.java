package com.gitbook.gitbook.dtos.deadline;

import jakarta.validation.constraints.NotNull;

import com.gitbook.gitbook.models.enums.StatusTask;

public record DeadlineStatusRequestDTO(@NotNull StatusTask status) {
}
