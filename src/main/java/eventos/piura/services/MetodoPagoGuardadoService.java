package eventos.piura.services;

import eventos.piura.dto.checkout.MetodoPagoGuardadoView;
import eventos.piura.model.MetodoPagoGuardado;
import eventos.piura.model.Usuario;
import eventos.piura.model.enums.MetodoPago;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MetodoPagoGuardadoService {

    List<MetodoPagoGuardadoView> listarGuardados(UUID usuarioId);

    Optional<MetodoPagoGuardado> obtenerParaUsuario(UUID metodoId, UUID usuarioId);

    MetodoPagoGuardado guardarTarjeta(Usuario usuario,
                                      String numero,
                                      String expiracion,
                                      String titular,
                                      String alias);

    MetodoPagoGuardado guardarWallet(Usuario usuario,
                                     MetodoPago tipo,
                                     String telefono,
                                     String alias);
}
