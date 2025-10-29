package eventos.piura.services;

import eventos.piura.dto.wallet.WalletRecargaRequest;
import eventos.piura.dto.wallet.WalletResumenView;
import eventos.piura.model.Usuario;

public interface WalletService {

    WalletResumenView obtenerResumen(Usuario usuario);

    void recargar(Usuario usuario, WalletRecargaRequest request);
}
