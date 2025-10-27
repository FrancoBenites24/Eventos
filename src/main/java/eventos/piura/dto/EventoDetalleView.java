package eventos.piura.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class EventoDetalleView {
    private final EventoInfo evento;
    private final OrganizadorInfo organizador;
    private final List<TicketInfo> tickets;

    @Getter
    @Builder
    public static class EventoInfo {
        private final UUID id;
        private final String titulo;
        private final String descripcion;
        private final String categoria;
        private final OffsetDateTime inicio;
        private final OffsetDateTime fin;
        private final OffsetDateTime creadoEn;
        private final String direccion;
        private final String distrito;
        private final String provincia;
        private final String departamento;
        private final String pais;
        private final BigDecimal latitud;
        private final BigDecimal longitud;
        private final List<String> imagenes;
    }

    @Getter
    @Builder
    public static class OrganizadorInfo {
        private final UUID id;
        private final String nombre;
        private final String apellido;
        private final String correo;
        private final String telefono;
    }

    @Getter
    @Builder
    public static class TicketInfo {
        private final UUID id;
        private final String nombre;
        private final Integer precioCentavos;
        private final Integer cupoTotal;
    }
}
