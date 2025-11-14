package org.busqueda.service;

import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.*;

import java.text.Normalizer;
import java.util.*;

@Service
public class SearchServiceImpl implements SearchService {

    private final MongoTemplate mongo;

    public SearchServiceImpl(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    @Override
    public Map<String, Object> search(String q, String tag, int page, int size) {
        Criteria c = new Criteria().andOperator(
                Criteria.where("oculto").is(false),
                new Criteria().orOperator(
                        Criteria.where("titulo").regex(q, "i"),
                        Criteria.where("descripcion").regex(q, "i"),
                        Criteria.where("texto").regex(q, "i")
                )
        );
        if (tag != null && !tag.isBlank()) {
            c = new Criteria().andOperator(c, Criteria.where("tags").all(List.of(tag)));
        }

        Query query = new Query(c)
                .with(Sort.by(Sort.Direction.DESC, "fecha"))
                .skip((long) page * size)
                .limit(size);

        var raw = mongo.find(query, org.busqueda.model.SearchDoc.class);

        Map<String, org.busqueda.model.SearchDoc> uniq = new LinkedHashMap<>();
        for (var d : raw) uniq.putIfAbsent(normalize(d.getTitulo()), d);

        long total = mongo.count(new Query(c), org.busqueda.model.SearchDoc.class);

        return Map.of(
                "page", page,
                "size", size,
                "total", total,
                "items", new ArrayList<>(uniq.values())
        );
    }

    private String normalize(String s) {
        if (s == null) return "";
        String lower = s.toLowerCase(Locale.ROOT);
        return Normalizer.normalize(lower, Normalizer.Form.NFD).replaceAll("\\p{M}+","");
    }
}
