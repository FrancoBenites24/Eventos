package eventos.piura.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RecuperarContrasenaRequest {

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Debe proporcionar un correo valido")
    private String correo;
}
