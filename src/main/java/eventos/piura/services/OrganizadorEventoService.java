package eventos.piura.services;

import eventos.piura.dto.organizador.NuevoEventoForm;
import eventos.piura.model.Categoria;
import eventos.piura.model.Evento;
import eventos.piura.model.EventoEntradaTipo;
import eventos.piura.model.TipoEntradaCatalogo;
import eventos.piura.model.Usuario;
import eventos.piura.model.enums.EstadoEvento;
import eventos.piura.repository.CategoriaRepository;
import eventos.piura.repository.EventoRepository;
import eventos.piura.repository.TipoEntradaCatalogoRepository;
import eventos.piura.services.EventoImagenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizadorEventoService {

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

    private final EventoRepository eventoRepository;
    private final CategoriaRepository categoriaRepository;
    private final TipoEntradaCatalogoRepository tipoEntradaCatalogoRepository;
    private final EventoImagenService eventoImagenService;

    @Transactional
    public UUID crearEvento(Usuario organizador, @Valid NuevoEventoForm form) {
        Evento evento = new Evento();
        evento.setOrganizador(organizador);
        evento.setTitulo(form.getTitulo().trim());
        evento.setDescripcion(trimToNull(form.getDescripcion()));

        asignarFechas(form, evento);
        asignarUbicacion(form, evento);
        asignarCategoria(form, evento);
        asignarEstado(form, evento);
        asignarTiposEntrada(form, evento);
        eventoImagenService.asignarImagenes(evento, form.getImagenes());

        Evento guardado = eventoRepository.save(evento);
        return guardado.getId();
    }

    private void asignarFechas(NuevoEventoForm form, Evento evento) {
        OffsetDateTime inicio = combinar(form.getInicioFecha(), form.getInicioHora());
        OffsetDateTime fin = combinar(form.getFinFecha(), form.getFinHora());
        if (fin.isBefore(inicio)) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior al inicio.");
        }
        evento.setInicioEn(inicio);
        evento.setFinEn(fin);
    }

    private OffsetDateTime combinar(LocalDate fecha, LocalTime hora) {
        return fecha.atTime(hora).atZone(ZONA_LIMA).toOffsetDateTime();
    }

    private void asignarUbicacion(NuevoEventoForm form, Evento evento) {
        evento.setDireccion(trimToNull(form.getDireccion()));
        evento.setDistrito(trimToNull(form.getDistrito()));
        evento.setProvincia(trimToNull(form.getProvincia()));
        evento.setDepartamento(trimToNull(form.getDepartamento()));
        evento.setPais(trimToNull(form.getPais()));

        if (form.getLatitud() != null) {
            evento.setLatitud(BigDecimal.valueOf(form.getLatitud()).setScale(6, RoundingMode.HALF_UP));
        }
        if (form.getLongitud() != null) {
            evento.setLongitud(BigDecimal.valueOf(form.getLongitud()).setScale(6, RoundingMode.HALF_UP));
        }
    }

    private void asignarCategoria(NuevoEventoForm form, Evento evento) {
        if (form.getCategoriaId() == null) {
            evento.setCategoria(null);
            return;
        }
        Categoria categoria = categoriaRepository.findById(form.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada"));
        evento.setCategoria(categoria);
    }

    private void asignarEstado(NuevoEventoForm form, Evento evento) {
        evento.setEstado(form.isPublicar() ? EstadoEvento.PUBLICADO : EstadoEvento.BORRADOR);
    }

    private void asignarTiposEntrada(NuevoEventoForm form, Evento evento) {
        evento.getTipos().clear();
        List<NuevoEventoForm.EntradaForm> entradas = form.getEntradas();
        if (entradas == null || entradas.isEmpty()) {
            throw new IllegalArgumentException("Debe registrar al menos un tipo de entrada.");
        }
        for (NuevoEventoForm.EntradaForm entradaForm : entradas) {
            if (entradaForm.getTipoCatalogoId() == null) {
                throw new IllegalArgumentException("Debe seleccionar el catalogo de la entrada.");
            }
            TipoEntradaCatalogo tipoCatalogo = tipoEntradaCatalogoRepository.findById(entradaForm.getTipoCatalogoId())
                    .orElseThrow(() -> new IllegalArgumentException("Tipo de entrada no encontrado."));

            EventoEntradaTipo tipo = new EventoEntradaTipo();
            tipo.setEvento(evento);
            tipo.setTipoEntrada(tipoCatalogo);
            tipo.setNombreVisible(entradaForm.getNombreVisible());
            tipo.setPrecioCentavos(toCentavos(entradaForm.getPrecio()));
            tipo.setCupoTotal(entradaForm.getCupo());
            evento.getTipos().add(tipo);
        }
    }

    private Integer toCentavos(Double precio) {
        BigDecimal monto = BigDecimal.valueOf(Optional.ofNullable(precio).orElse(0.0d));
        return monto.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).intValueExact();
    }

    private String trimToNull(String valor) {
        if (valor == null) {
            return null;
        }
        String trimmed = valor.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
