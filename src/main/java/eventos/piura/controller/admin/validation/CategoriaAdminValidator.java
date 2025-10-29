package eventos.piura.controller.admin.validation;

import eventos.piura.model.Categoria;
import eventos.piura.repository.CategoriaRepository;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.Optional;
import java.util.UUID;

@Component
public class CategoriaAdminValidator implements Validator {

    private final CategoriaRepository categoriaRepository;

    public CategoriaAdminValidator(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Categoria.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (!(target instanceof Categoria categoria)) {
            return;
        }

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "nombre",
                "categoria.nombre.vacio", "El nombre es obligatorio.");

        if (errors.hasFieldErrors("nombre")) {
            return;
        }

        String nombre = categoria.getNombre();
        if (nombre == null) {
            return;
        }

        String nombreNormalizado = nombre.trim();
        if (nombreNormalizado.length() > 30) {
            errors.rejectValue("nombre", "categoria.nombre.longitud",
                    "El nombre no debe exceder 30 caracteres.");
            return;
        }

        categoria.setNombre(nombreNormalizado);

        Optional<Categoria> existente = categoriaRepository.findByNombreIgnoreCase(nombreNormalizado);
        if (existente.isPresent()) {
            UUID idActual = categoria.getId();
            if (idActual == null || !existente.get().getId().equals(idActual)) {
                errors.rejectValue("nombre", "categoria.nombre.duplicado",
                        "Ya existe una categoria con ese nombre.");
            }
        }
    }
}
