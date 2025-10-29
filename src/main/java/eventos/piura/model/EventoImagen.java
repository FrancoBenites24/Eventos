package eventos.piura.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ev_evento_imagen")
@Getter
@Setter
@NoArgsConstructor
public class EventoImagen extends AuditableEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id")
    private Evento evento;

    @Column(name = "nombre_archivo", nullable = false, length = 200)
    private String nombreArchivo;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "tamanio_bytes", nullable = false)
    private Long tamanioBytes;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "datos", nullable = false)
    private byte[] datos;
}
