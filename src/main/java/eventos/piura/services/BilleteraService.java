package eventos.piura.services;

import eventos.piura.model.Billetera;
import eventos.piura.model.MovimientoBilletera;
import eventos.piura.model.Usuario;
import eventos.piura.repository.BilleteraRepository;
import eventos.piura.repository.MovimientoBilleteraRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class BilleteraService {

    private static final Logger logger = LoggerFactory.getLogger(BilleteraService.class);
    
    @Autowired
    private BilleteraRepository billeteraRepository;
    
    @Autowired
    private MovimientoBilleteraRepository movimientoBilleteraRepository;
    
    @Transactional(readOnly = true)
    public Optional<Billetera> obtenerPorUsuarioId(Long usuarioId) {
        try {
            logger.info("Buscando billetera para el usuario ID: {}", usuarioId);
            Optional<Billetera> billetera = billeteraRepository.findByUsuarioId(usuarioId);
            if (billetera.isPresent()) {
                logger.info("Billetera encontrada con ID: {} para el usuario ID: {}", 
                        billetera.get().getId(), usuarioId);
            } else {
                logger.info("No se encontró billetera para el usuario ID: {}", usuarioId);
            }
            return billetera;
        } catch (Exception e) {
            logger.error("Error al buscar billetera para el usuario ID: {}", usuarioId, e);
            return Optional.empty();
        }
    }
    
    @Transactional(readOnly = true)
    public List<MovimientoBilletera> obtenerMovimientosRecientes(Long usuarioId) {
        try {
            logger.info("Buscando movimientos recientes para el usuario ID: {}", usuarioId);
            List<MovimientoBilletera> movimientos = movimientoBilleteraRepository.findTop5ByBilletera_UsuarioIdOrderByCreadoEnDesc(usuarioId);
            logger.info("Se encontraron {} movimientos recientes para el usuario ID: {}", 
                    movimientos.size(), usuarioId);
            return movimientos;
        } catch (Exception e) {
            logger.error("Error al buscar movimientos recientes para el usuario ID: {}", usuarioId, e);
            return Collections.emptyList();
        }
    }
    
    @Transactional
    public Billetera crearBilleteraParaUsuario(Usuario usuario) {
        try {
            logger.info("Creando nueva billetera para el usuario: {}", usuario.getUsername());
            Billetera billetera = new Billetera();
            billetera.setUsuario(usuario);
            billetera.setSaldo(0.0);
            Billetera nuevaBilletera = billeteraRepository.save(billetera);
            logger.info("Billetera creada exitosamente con ID: {} para el usuario: {}", 
                    nuevaBilletera.getId(), usuario.getUsername());
            return nuevaBilletera;
        } catch (Exception e) {
            logger.error("Error al crear billetera para el usuario: {}", usuario.getUsername(), e);
            throw new RuntimeException("No se pudo crear la billetera para el usuario: " + usuario.getUsername(), e);
        }
    }
}