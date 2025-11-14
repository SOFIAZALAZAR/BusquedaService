package org.busqueda.model;

import lombok.Data;                           // <-- import Lombok
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document("search_docs")
public class SearchDoc {
    @Id
    private String id;            // "HECHO:<id>" o "PDI:<id>"
    private String tipo;          // HECHO | PDI
    private String origenId;
    private String hechoId;
    private String coleccion;
    private String titulo;
    private String descripcion;
    private List<String> tags;
    private String texto;
    private boolean oculto;
    private LocalDateTime fecha;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getOrigenId() { return origenId; }
    public void setOrigenId(String origenId) { this.origenId = origenId; }

    public String getHechoId() { return hechoId; }
    public void setHechoId(String hechoId) { this.hechoId = hechoId; }

    public String getColeccion() { return coleccion; }
    public void setColeccion(String coleccion) { this.coleccion = coleccion; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public boolean isOculto() { return oculto; }
    public void setOculto(boolean oculto) { this.oculto = oculto; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
