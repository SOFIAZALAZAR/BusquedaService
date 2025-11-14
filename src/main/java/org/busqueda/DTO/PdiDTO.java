package org.busqueda.DTO;

import java.time.LocalDateTime;
import java.util.List;

public record PdiDTO(
        String id,
        String hechoId,
        String descripcion,
        String lugar,
        LocalDateTime momento,
        String contenido,
        List<String> etiquetas,
        String resultadoOcr,
        String urlImagen
) {}