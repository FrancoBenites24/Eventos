package eventos.piura.services;

import eventos.piura.model.Usuario;

public interface VerificacionCorreoService {

    /**
     * Crea un código de verificación nuevo para el usuario y envía el correo correspondiente.
     */
    void crearSolicitudVerificacion(Usuario usuario);

    /**
     * Confirma el código de verificación recibido por correo.
     *
     * @return true si la verificación fue exitosa; false en caso de código inválido o expirado.
     */
    boolean confirmarCodigo(String codigo);
}
