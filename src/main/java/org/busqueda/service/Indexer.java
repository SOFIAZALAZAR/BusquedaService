package org.busqueda.service;

import org.busqueda.DTO.HechoDTO;
import org.busqueda.DTO.PdiDTO;
import org.busqueda.model.SearchDoc;
import org.busqueda.repo.SearchDocRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class Indexer {

    private final SearchDocRepository repo;

    public Indexer(SearchDocRepository repo) {
        this.repo = repo;
    }

    /* ============ Hecho ============ */
    public void upsertHecho(HechoDTO h) {
        String id = buildId("HECHO", h.id());
        SearchDoc d = repo.findById(id).orElse(new SearchDoc());
        d.setId(id);
        d.setTipo("HECHO");
        d.setOrigenId(h.id());
        d.setHechoId(h.id());
        d.setColeccion(h.nombreColeccion());
        d.setTitulo(h.titulo());
        d.setDescripcion(null);                 // opcional
        d.setTags(copy(h.etiquetas()));
        d.setTexto(buildHechoTexto(h));
        d.setOculto(false);
        d.setFecha(h.fecha() != null ? h.fecha() : LocalDateTime.now());
        repo.save(d);

        // Completar los PDI ya indexados para este hecho (si existían)
        List<SearchDoc> pdis = repo.findByHechoId(h.id());
        for (SearchDoc pdiDoc : pdis) {
            if ("PDI".equals(pdiDoc.getTipo())) {
                pdiDoc.setTitulo(h.titulo());
                pdiDoc.setColeccion(h.nombreColeccion());
                if (pdiDoc.getFecha() == null) pdiDoc.setFecha(h.fecha());
                repo.save(pdiDoc);
            }
        }
    }

    /* ============ PDI ============ */
    public void upsertPdi(PdiDTO p) {
        String id = buildId("PDI", p.id());
        SearchDoc d = repo.findById(id).orElse(new SearchDoc());
        d.setId(id);
        d.setTipo("PDI");
        d.setOrigenId(p.id());
        d.setHechoId(p.hechoId());

        // Puede que el Hecho aún no haya llegado → dejamos null y luego upsertHecho lo completa
        d.setTitulo(d.getTitulo());     // no tocar si ya estaba
        d.setColeccion(d.getColeccion());

        d.setDescripcion(p.descripcion());
        d.setTags(copy(p.etiquetas())); // solo tags que usarás para AND
        d.setTexto(buildPdiTexto(p));
        d.setOculto(false);
        if (d.getFecha() == null) {
            d.setFecha(p.momento() != null ? p.momento() : LocalDateTime.now());
        }
        repo.save(d);
    }

    /* ============ Borrado/ocultar ============ */
    public void hideHecho(String hechoId) {
        // ocultar HECHO
        String idHecho = buildId("HECHO", hechoId);
        repo.findById(idHecho).ifPresent(doc -> { doc.setOculto(true); repo.save(doc); });

        // ocultar todos los PDI de ese hecho
        for (SearchDoc doc : repo.findByHechoId(hechoId)) {
            if (!doc.isOculto()) { doc.setOculto(true); repo.save(doc); }
        }
    }

    /* ============ Helpers ============ */
    private static String buildId(String tipo, String id) { return tipo + ":" + id; }

    private static String buildHechoTexto(HechoDTO h) {
        StringBuilder sb = new StringBuilder();
        append(sb, h.titulo());
        append(sb, h.ubicacion());
        append(sb, h.categoria());
        append(sb, h.origen());
        if (h.etiquetas() != null) append(sb, String.join(" ", h.etiquetas()));
        return sb.toString().trim();
    }

    private static String buildPdiTexto(PdiDTO p) {
        StringBuilder sb = new StringBuilder();
        append(sb, p.descripcion());
        append(sb, p.contenido());
        append(sb, p.resultadoOcr());
        if (p.etiquetas() != null) append(sb, String.join(" ", p.etiquetas()));
        append(sb, p.lugar());
        append(sb, p.momento() != null ? p.momento().toString() : null);
        append(sb, p.urlImagen());
        return sb.toString().trim();
    }

    private static void append(StringBuilder sb, String s) {
        if (s != null && !s.isBlank()) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(s);
        }
    }

    private static List<String> copy(List<String> in) {
        return in == null ? List.of() : new ArrayList<>(in);
    }
}
