package org.busqueda.controller;

import org.busqueda.DTO.HechoDTO;
import org.busqueda.DTO.PdiDTO;
import org.busqueda.service.Indexer;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/events")
public class WebhooksController {

    private final Indexer indexer;

    public WebhooksController(Indexer indexer) {
        this.indexer = indexer;
    }

    @PostMapping("/hechoUpsert")
    public void hechoUpsert(@RequestBody HechoDTO hecho) {
        indexer.upsertHecho(hecho);
    }

    @PostMapping("/pdiUpsert")
    public void pdiUpsert(@RequestBody PdiDTO pdi) {
        indexer.upsertPdi(pdi);
    }

    @PostMapping("/hechoHide")
    public void hechoHide(@RequestBody Map<String, String> body) {
        indexer.hideHecho(body.get("hechoId"));
    }
}
