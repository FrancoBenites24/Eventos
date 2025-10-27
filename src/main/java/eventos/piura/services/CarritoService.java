package eventos.piura.services;

import eventos.piura.dto.carrito.CarritoResumenView;

import java.util.UUID;

public interface CarritoService {
    CarritoResumenView obtenerResumen();
    CarritoResumenView agregar(UUID entradaTipoId, int cantidad);
    CarritoResumenView eliminar(UUID entradaTipoId);
    CarritoResumenView limpiar();
}
