package org.busqueda.repo;

import org.busqueda.model.SearchDoc;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SearchDocRepository extends MongoRepository<SearchDoc, String> {
    List<SearchDoc> findByHechoId(String hechoId);
}
