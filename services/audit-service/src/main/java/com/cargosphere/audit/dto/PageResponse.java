package com.cargosphere.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> content;

    private int page;

    private int size;

    private long totalElements;

    private int totalPages;

    private int numberOfElements;

    private boolean first;

    private boolean last;

    private boolean empty;

    public static <T> PageResponse<T> from(
            Page<T> source
    ) {
        return PageResponse.<T>builder()
                .content(source.getContent())
                .page(source.getNumber())
                .size(source.getSize())
                .totalElements(
                        source.getTotalElements()
                )
                .totalPages(
                        source.getTotalPages()
                )
                .numberOfElements(
                        source.getNumberOfElements()
                )
                .first(source.isFirst())
                .last(source.isLast())
                .empty(source.isEmpty())
                .build();
    }
}