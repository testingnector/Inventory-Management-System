package com.nector.catalogservice.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pagination {

    private int page;          
    private int size;          
    private long totaElements;    
    private int totalPages;    
    private boolean last;

}