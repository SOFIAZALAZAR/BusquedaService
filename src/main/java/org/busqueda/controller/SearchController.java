package org.busqueda.controller;

import org.busqueda.service.SearchService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final SearchService service;
    public SearchController(SearchService service) {
        this.service = service;
    }

    @GetMapping
    public Map<String, Object> search(
            @RequestParam String q,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return service.search(q, tag, page, size);
    }
}
