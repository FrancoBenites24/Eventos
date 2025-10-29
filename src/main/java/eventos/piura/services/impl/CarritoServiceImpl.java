package eventos.piura.services.impl;

import eventos.piura.dto.carrito.CarritoItemView;
import eventos.piura.dto.carrito.CarritoResumenView;
import eventos.piura.model.EventoEntradaTipo;
import eventos.piura.model.enums.EstadoEvento;
import eventos.piura.repository.EventoEntradaTipoRepository;
import eventos.piura.services.CarritoService;
import eventos.piura.services.carrito.CarritoSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CarritoServiceImpl implements CarritoService {

    private final CarritoSession carritoSession;
    private final EventoEntradaTipoRepository eventoEntradaTipoRepository;

    @Override
    @Transactional(readOnly = true)
    public CarritoResumenView obtenerResumen() {
        return mapear();
    }

    @Override
    public CarritoResumenView agregar(UUID entradaTipoId, int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }

        EventoEntradaTipo tipo = eventoEntradaTipoRepository
                .findByIdAndEventoEstado(entradaTipoId, EstadoEvento.PUBLICADO)
                .orElseThrow(() -> new IllegalArgumentException("La entrada seleccionada no esta disponible."));

        Map<UUID, CarritoSession.CarritoSessionItem> items = carritoSession.getItems();
        CarritoSession.CarritoSessionItem actual = items.get(entradaTipoId);
        if (actual == null) {
            int precioCentavos = tipo.getPrecioCentavos() != null ? tipo.getPrecioCentavos() : 0;
            CarritoSession.CarritoSessionItem nuevo = new CarritoSession.CarritoSessionItem(
                    tipo.getEvento().getId(),
                    tipo.getId(),
                    tipo.getEvento().getTitulo(),
                    tipo.getNombreVisible(),
                    precioCentavos,
                    cantidad
            );
            items.put(entradaTipoId, nuevo);
        } else {
            items.put(entradaTipoId, actual.incrementar(cantidad));
        }

        return mapear();
    }

    @Override
    public CarritoResumenView eliminar(UUID entradaTipoId) {
        carritoSession.getItems().remove(entradaTipoId);
        return mapear();
    }

    @Override
    public CarritoResumenView limpiar() {
        carritoSession.getItems().clear();
        return mapear();
    }

    private CarritoResumenView mapear() {
        List<CarritoItemView> items = carritoSession.getItems().values().stream()
                .map(item -> new CarritoItemView(
                        item.eventoId(),
                        item.entradaId(),
                        item.titulo(),
                        item.tipoNombre(),
                        item.cantidad(),
                        item.precioCentavos(),
                        item.precioCentavos() * item.cantidad()
                ))
                .toList();

        int totalCentavos = items.stream().mapToInt(CarritoItemView::subtotalCentavos).sum();
        int totalCantidad = items.stream().mapToInt(CarritoItemView::cantidad).sum();

        return new CarritoResumenView(items, totalCentavos, totalCantidad);
    }
}
