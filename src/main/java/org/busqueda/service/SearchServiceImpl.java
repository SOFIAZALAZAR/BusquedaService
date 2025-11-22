package org.busqueda.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.busqueda.model.SearchDoc;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.*;
import io.micrometer.core.instrument.Timer;


import java.text.Normalizer;
import java.util.*;

@Service
public class SearchServiceImpl implements SearchService {

    private final MongoTemplate mongo;
    private final Counter busquedasCounter;
    private final Timer   busquedasTimer;


    public SearchServiceImpl(MongoTemplate mongo, MeterRegistry registry) {
        this.mongo = mongo;
        this.busquedasCounter = registry.counter("pdi.search.count");
        this.busquedasTimer   = registry.timer("pdi.search.duration");
    }

    @Override
    public Map<String, Object> search(String q, String tag, int page, int size) {

        return busquedasTimer.record(() -> {
            busquedasCounter.increment();

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

            var raw = mongo.find(query, SearchDoc.class);

            // HECHOS por hechoId (id lógico de hecho)
            Map<String, SearchDoc> hechosPorHechoId = new LinkedHashMap<>();

            // PDI agrupados por hechoId
            Map<String, List<SearchDoc>> pdisPorHechoId = new LinkedHashMap<>();

            // PDI sin hechoId (por las dudas)
            List<SearchDoc> pdisSinHecho = new ArrayList<>();

            // 1) Separar HECHOS y PDI de la query principal
            for (SearchDoc d : raw) {
                if ("HECHO".equalsIgnoreCase(d.getTipo())) {
                    String hid = d.getHechoId();
                    if (hid != null) {
                        hechosPorHechoId.putIfAbsent(hid, d);
                    }
                } else if ("PDI".equalsIgnoreCase(d.getTipo())) {
                    String hid = d.getHechoId();
                    if (hid != null) {
                        pdisPorHechoId
                                .computeIfAbsent(hid, k -> new ArrayList<>())
                                .add(d);
                    } else {
                        pdisSinHecho.add(d);
                    }
                } else {
                    // otros tipos a futuro: por ahora los trato como PDI sin hecho
                    pdisSinHecho.add(d);
                }
            }

            // 2) Para cada hechoId que tenga PDI pero no HECHO en el resultado, traer el HECHO de Mongo
            for (String hid : pdisPorHechoId.keySet()) {
                if (hechosPorHechoId.containsKey(hid) || hid == null) continue;

                Query qHecho = new Query(
                        Criteria.where("tipo").is("HECHO")
                                .and("hechoId").is(hid)
                );

                SearchDoc hecho = mongo.findOne(qHecho, SearchDoc.class);
                if (hecho != null && !hecho.isOculto()) {
                    hechosPorHechoId.put(hid, hecho);
                }
            }

            // 3) Construir items: un item por HECHO
            List<Map<String, Object>> items = new ArrayList<>();

            for (Map.Entry<String, SearchDoc> entry : hechosPorHechoId.entrySet()) {
                String hechoId = entry.getKey();
                SearchDoc hechoDoc = entry.getValue();

                // view reducido del hecho
                Map<String, Object> hechoView = new HashMap<>();
                hechoView.put("id", hechoDoc.getHechoId());       // o hechoDoc.getOrigenId()
                hechoView.put("titulo", hechoDoc.getTitulo());
                hechoView.put("coleccion", hechoDoc.getColeccion()); // puede ser null, y está bien

                // lista de PDI de ese hecho (o lista vacía)
                List<SearchDoc> pdisDeHecho = pdisPorHechoId.getOrDefault(hechoId, List.of());

                Map<String, Object> item = new HashMap<>();
                item.put("hecho", hechoView);
                item.put("pdis", pdisDeHecho);

                items.add(item);
            }

            // (Opcional) si querés incluir PDI sin hecho en algún grupo extra, se puede agregar acá

            int total = items.size(); // total de grupos HECHO+PDIs

            return Map.of(
                    "page", page,
                    "size", size,
                    "total", total,
                    "items", items
            );
        });
    }



    private String normalize(String s) {
        if (s == null) return "";
        String lower = s.toLowerCase(Locale.ROOT);
        return Normalizer.normalize(lower, Normalizer.Form.NFD).replaceAll("\\p{M}+","");
    }
}
