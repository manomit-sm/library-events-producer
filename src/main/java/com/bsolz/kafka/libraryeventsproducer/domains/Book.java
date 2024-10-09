package com.bsolz.kafka.libraryeventsproducer.domains;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record Book(
        @NotNull
        Integer bookId,
        @NotEmpty
        String bokName,

        @NotEmpty
        String bookAuthor
) {
}
