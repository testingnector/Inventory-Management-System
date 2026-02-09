package com.nector.catalogservice.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.nector.catalogservice.enums.TaxComponentType;

@RestController
@RequestMapping("/tax-component-types")
public class TaxComponentTypeController {

    @GetMapping
    public List<TaxComponentType> getAll() {
        return Arrays.asList(TaxComponentType.values());
    }
}
