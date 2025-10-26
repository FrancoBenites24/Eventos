package eventos.piura.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RegistroUsuarioRequest {
    @NotBlank @Size(max = 40)
    private String nombre;

    @NotBlank @Size(max = 40)
    private String apellido;

    @NotBlank @Pattern(regexp = "^[0-9]{8}$")
    private String dni;

    @Pattern(regexp = "^\\+?[0-9]{9,15}$")
    private String telefono;

    @NotBlank @Size(min = 4, max = 20)
    private String username;

    @NotBlank @Email @Size(max = 120)
    private String correo;

    @NotBlank @Size(min = 6, max = 72)
    private String contrasena;

    @NotBlank
    private String confirmarContrasena;
}
