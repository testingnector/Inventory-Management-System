package com.nector.catalogservice.dto.response.internal;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({"content", "pageNumber", "pageSize", "totalElements", "totalPages", "last"})
@Data
public class PagedResponse<T> {

    private List<T> content;     
    private int pageNumber;         
    private int pageSize;           
    private long totalElements;     
    private int totalPages;        
    private boolean last;           
}
