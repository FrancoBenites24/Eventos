package eventos.piura.dto.organizador;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class NuevoEventoForm {

    @NotBlank
    @Size(max = 160)
    private String titulo;

    @Size(max = 4000)
    private String descripcion;

    private UUID categoriaId;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate inicioFecha;

    @NotNull
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime inicioHora;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate finFecha;

    @NotNull
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime finHora;

    @Size(max = 200)
    private String direccion;

    @Size(max = 80)
    private String distrito;

    @Size(max = 80)
    private String provincia;

    @Size(max = 80)
    private String departamento;

    @Size(max = 80)
    private String pais;

    private Double latitud;
    private Double longitud;

    private boolean publicar;

    @Valid
    @NotEmpty(message = "Debe registrar al menos un tipo de entrada")
    private List<EntradaForm> entradas = new ArrayList<>();

    public NuevoEventoForm() {
        if (entradas.isEmpty()) {
            entradas.add(new EntradaForm());
        }
    }

    @Getter
    @Setter
    public static class EntradaForm {
        private UUID tipoCatalogoId;

        @NotBlank
        @Size(max = 80)
        private String nombreVisible;

        @NotNull
        @PositiveOrZero
        private Double precio;

        @NotNull
        @Positive
        private Integer cupo;
    }
}
