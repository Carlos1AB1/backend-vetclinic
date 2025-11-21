package com.vetclinic.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración completa de Swagger/OpenAPI
 * Organiza y documenta toda la API del sistema
 */
@Configuration
public class SwaggerConfig {

    @Value("${app.name}")
    private String appName;

    @Value("${app.version}")
    private String appVersion;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title(appName + " - API REST Completa")
                        .version(appVersion)
                        .description("""
                            ## 🏥 Sistema de Gestión de Clínica Veterinaria
                            
                            API REST completa para la gestión integral de una clínica veterinaria profesional.
                            
                            ### 📋 Características Principales:
                            - ✅ Autenticación y autorización con JWT
                            - ✅ Gestión de usuarios con roles (Admin, Veterinario, Recepcionista)
                            - ✅ Registro y control de pacientes (mascotas)
                            - ✅ Gestión de propietarios (clientes)
                            - ✅ Sistema de citas médicas con validaciones
                            - ✅ Historias clínicas completas
                            - ✅ Recetas y prescripciones (PDF/Excel)
                            - ✅ Inventario de productos
                            - ✅ Catálogo de servicios
                            - ✅ Consentimientos informados
                            - ✅ Agenda con vistas múltiples (diaria, semanal, mensual)
                            - ✅ Reportes operativos avanzados
                            - ✅ Dashboard con estadísticas
                            
                            ### 🔐 Autenticación:
                            1. Obtén un token usando `POST /auth/login`
                            2. Haz clic en el botón **Authorize** (🔒)
                            3. Ingresa: `Bearer TU_TOKEN_AQUI`
                            4. ¡Listo para probar los endpoints!
                            
                            ### 👥 Usuarios de Prueba:
                            - **Admin**: `admin` / `Admin123!`
                            - **Veterinario**: `vet1` / `Vet123!`
                            - **Recepcionista**: `recep1` / `Recep123!`
                            
                            ### 📦 Patrones de Diseño Implementados:
                            - Singleton, Factory Method, Abstract Factory, Builder
                            - Facade, Adapter, Proxy, Decorator
                            - Observer, Strategy, State, Chain of Responsibility
                            """)
                        .contact(new Contact()
                                .name("VetClinic Pro Team")
                                .email("support@vetclinicpro.com")
                                .url("https://vetclinicpro.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(Arrays.asList(
                        new Server()
                                .url("http://localhost:8081/api")
                                .description("🔧 Servidor de Desarrollo Local"),
                        new Server()
                                .url("https://api.vetclinicpro.com")
                                .description("🚀 Servidor de Producción")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT Authorization header usando el esquema Bearer. " +
                                                "Ingresa tu token en el formato: Bearer {token}")))
                .tags(getOrderedTags());
    }

    /**
     * Define el orden y descripción de los tags para una mejor organización
     */
    private List<Tag> getOrderedTags() {
        return Arrays.asList(
                new Tag().name("01. Autenticación").description("🔐 Login, registro y gestión de sesiones"),
                new Tag().name("02. Usuarios").description("👥 Administración de usuarios y roles"),
                new Tag().name("03. Pacientes (Mascotas)").description("🐾 Registro y gestión de pacientes"),
                new Tag().name("04. Propietarios (Clientes)").description("👨‍👩‍👧 Gestión de clientes propietarios"),
                new Tag().name("05. Citas Médicas").description("📅 Agendamiento y gestión de citas"),
                new Tag().name("06. Historias Clínicas").description("📋 Registros médicos de pacientes"),
                new Tag().name("07. Recetas y Prescripciones").description("💊 Gestión de prescripciones y exportación"),
                new Tag().name("08. Inventario").description("📦 Control de stock y productos"),
                new Tag().name("09. Catálogo de Servicios").description("🏥 Servicios ofrecidos por la clínica"),
                new Tag().name("10. Consentimientos Informados").description("📝 Documentos de consentimiento"),
                new Tag().name("11. Agenda y Visualización").description("🗓️ Vistas de agenda (diaria, semanal, mensual)"),
                new Tag().name("12. Reportes Operativos").description("📊 Generación de reportes y análisis"),
                new Tag().name("13. Dashboard y Estadísticas").description("📈 Métricas y estadísticas del sistema")
        );
    }
}

