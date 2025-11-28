package com.vetclinic;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;

@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableAsync
@EnableScheduling
@Slf4j
public class VetClinicApplication {

    public static void main(String[] args) {
        // Cargar variables de entorno desde .env antes de iniciar Spring
        loadEnvFile();
        
        SpringApplication.run(VetClinicApplication.class, args);
    }

    /**
     * Cargar variables de entorno desde archivo .env
     * Busca el archivo desde el directorio raíz del proyecto
     */
    private static void loadEnvFile() {
        try {
            // Buscar .env en múltiples ubicaciones posibles
            File envFile = findEnvFile();
            
            if (envFile != null && envFile.exists()) {
                String envPath = envFile.getAbsolutePath();
                String envDir = envFile.getParent();
                
                log.info("Cargando variables de entorno desde archivo .env en: {}", envPath);
                
                Dotenv dotenv = Dotenv.configure()
                        .directory(envDir != null ? envDir : ".")
                        .filename(".env")
                        .ignoreIfMissing()
                        .ignoreIfMalformed()  // Ignorar líneas mal formadas
                        .load();
                
                int loadedCount = 0;
                // Cargar todas las variables del .env al System Properties
                for (var entry : dotenv.entries()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    
                    // Solo establecer si no existe ya como variable de entorno del sistema
                    if (System.getenv(key) == null && value != null && !value.trim().isEmpty()) {
                        System.setProperty(key, value);
                        log.debug("Variable cargada: {} = {}", key, maskSensitiveValue(key, value));
                        loadedCount++;
                    }
                }
                
                log.info("Variables de entorno cargadas exitosamente desde .env ({} variables)", loadedCount);
            } else {
                log.warn("Archivo .env no encontrado. Buscando en: {}", new File(".").getAbsolutePath());
                log.warn("Usando variables de entorno del sistema.");
            }
        } catch (Exception e) {
            log.error("Error al cargar archivo .env: {}", e.getMessage(), e);
            log.warn("Continuando sin archivo .env. Usando variables de entorno del sistema.");
        }
    }

    /**
     * Buscar el archivo .env en diferentes ubicaciones posibles
     */
    private static File findEnvFile() {
        // Lista de ubicaciones posibles para buscar .env
        String[] possiblePaths = {
            ".env",                                    // Directorio actual
            "../.env",                                 // Directorio padre
            "../../.env",                              // Dos niveles arriba
            System.getProperty("user.dir") + "/.env",  // Directorio de usuario
            System.getProperty("user.dir") + "/../.env"
        };
        
        // Buscar desde el directorio del proyecto (donde está pom.xml)
        try {
            // Obtener la ruta del classpath (target/classes)
            String classPath = VetClinicApplication.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath();
            
            // Si estamos en target/classes, subir a la raíz del proyecto
            if (classPath.contains("target/classes")) {
                File targetDir = new File(classPath);
                File projectRoot = targetDir.getParentFile().getParentFile();
                File envInRoot = new File(projectRoot, ".env");
                if (envInRoot.exists()) {
                    return envInRoot;
                }
            }
        } catch (Exception e) {
            // Ignorar errores al obtener classpath
        }
        
        // Buscar en las rutas posibles
        for (String path : possiblePaths) {
            File envFile = new File(path);
            if (envFile.exists() && envFile.isFile()) {
                return envFile;
            }
        }
        
        // Último intento: buscar desde el directorio de trabajo actual
        File currentDir = new File(System.getProperty("user.dir"));
        File envInCurrent = new File(currentDir, ".env");
        if (envInCurrent.exists()) {
            return envInCurrent;
        }
        
        // Buscar recursivamente hacia arriba desde el directorio actual
        File searchDir = currentDir;
        for (int i = 0; i < 5; i++) {  // Buscar hasta 5 niveles arriba
            File envFile = new File(searchDir, ".env");
            if (envFile.exists()) {
                return envFile;
            }
            File parent = searchDir.getParentFile();
            if (parent == null || parent.equals(searchDir)) {
                break;
            }
            searchDir = parent;
        }
        
        return null;
    }

    /**
     * Enmascarar valores sensibles en los logs
     */
    private static String maskSensitiveValue(String key, String value) {
        if (key.toLowerCase().contains("password") || 
            key.toLowerCase().contains("secret") || 
            key.toLowerCase().contains("key")) {
            if (value != null && value.length() > 4) {
                return value.substring(0, 2) + "***" + value.substring(value.length() - 2);
            }
            return "***";
        }
        return value;
    }
}
