package eventos.piura.services;

public interface RecuperacionContrasenaService {

    /**
     * Genera un token de recuperacion y envia el correo con el enlace si el usuario es valido.
     */
    void solicitarRecuperacion(String correo);

    /**
     * Valida que el token exista, no haya expirado y no se haya consumido.
     */
    boolean tokenValido(String token);

    /**
     * Intenta restablecer la contrasena con el token indicado.
     *
     * @return true si se restablecio correctamente, false en caso de token invalido o expirado.
     */
    boolean restablecerContrasena(String token, String nuevaContrasena);
}
