package org.busqueda.service;

import java.util.Map;

public interface SearchService {
    Map<String, Object> search(String q, String tag, int page, int size);
}

