package com.nector.catalogservice.dto.response.internal;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonPropertyOrder({ "pageNumber", "pageSize", "totalElements", "totalPages", "last"})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageMeta {
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
