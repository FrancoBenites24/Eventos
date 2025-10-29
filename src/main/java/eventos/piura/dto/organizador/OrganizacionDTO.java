package eventos.piura.dto.organizador;

import lombok.Data;

@Data
public class OrganizacionDTO {
    private String nombre;
    private String ruc;
    private String direccion;
    private String correo;
    private String telefono;
    private String logoPath; // ruta a imagen en data/imagenes
}
