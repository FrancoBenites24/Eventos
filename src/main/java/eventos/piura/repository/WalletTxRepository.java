package eventos.piura.repository;

import eventos.piura.model.WalletTx;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface WalletTxRepository extends JpaRepository<WalletTx, UUID> {

    @Query("select coalesce(sum(case when tx.tipo = 'CR' then tx.montoCentavos else -tx.montoCentavos end), 0) " +
            "from WalletTx tx where tx.billetera.id = :billeteraId")
    int calcularSaldoCentavos(@Param("billeteraId") UUID billeteraId);

    List<WalletTx> findTop5ByBilleteraIdOrderByCreadoEnDesc(UUID billeteraId);
}
