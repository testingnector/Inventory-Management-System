package com.nector.catalogservice.dto.response.internal;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonPropertyOrder({ "pageNumber", "pageSize", "totalElements", "totalPages", "last"})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PageMeta {

    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
    private boolean first;
    private String sort;
}

