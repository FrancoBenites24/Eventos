package eventos.piura.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eventos.piura.services.BilleteraService;
import eventos.piura.services.EventoService;
import eventos.piura.services.PerfilService;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Eventos Piura API")
                        .version("1.0")
                        .description("API para la aplicación de eventos de Piura"));
    }

    @Bean
    public BilleteraService billeteraService() {
        return new BilleteraService();
    }
    
    @Bean
    public EventoService eventoService() {
        return new EventoService();
    }
    
    @Bean
    public PerfilService perfilService() {
        return new PerfilService();
    }
}
