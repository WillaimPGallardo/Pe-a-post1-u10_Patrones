# Productos Service — Análisis SonarQube
## Unidad 10: Métricas de Calidad · Ingeniería de Sistemas UDES 2026

---

## Descripción del proyecto

Proyecto Spring Boot con código **intencionalmente imperfecto** usado como laboratorio para aprender a ejecutar análisis estático con **SonarQube** e integrar cobertura de código con **JaCoCo**. El objetivo del Post-Contenido 1 es identificar y documentar los problemas; en el Post-Contenido 2 se corrigen.

---

## Prerrequisitos

| Herramienta | Versión mínima |
|---|---|
| JDK | 21 |
| Maven | 3.9+ |
| Docker Desktop | Cualquier versión reciente |
| Git | 2.x |

---

## Cómo ejecutar el proyecto

### 1. Levantar SonarQube con Docker

```bash
# Iniciar el servidor SonarQube Community Edition en el puerto 9000
docker run -d \
  --name sonarqube \
  -p 9000:9000 \
  -e SONAR_ES_BOOTSTRAP_CHECKS_DISABLE=true \
  sonarqube:community

# Verificar que el contenedor está corriendo
docker ps

# Esperar el mensaje "SonarQube is operational" en los logs
docker logs -f sonarqube
```

Acceder a **http://localhost:9000** con las credenciales `admin / admin`.  
SonarQube pedirá cambiar la contraseña en el primer acceso.

Luego crear el proyecto manualmente:
- **Projects → Create Project → Manually**
- Project name: `Productos Service`
- Project key: `com.universidad:productos-service`
- **Generate Token** → copiar el token generado

### 2. Compilar la aplicación

```bash
mvn compile
```

### 3. Ejecutar la aplicación

```bash
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080/api/productos`.

### 4. Ejecutar tests y generar reporte JaCoCo

```bash
mvn clean verify
```

Esto genera el reporte de cobertura en `target/site/jacoco/jacoco.xml`.

### 5. Ejecutar análisis SonarQube

```bash
# Reemplazar TU_TOKEN con el token generado en el paso 1
mvn sonar:sonar -Dsonar.token=TU_TOKEN

# Alternativa: compilar + tests + análisis en un solo comando
mvn clean verify sonar:sonar -Dsonar.token=TU_TOKEN
```

Ver resultados en:  
**http://localhost:9000/dashboard?id=com.universidad%3Aproductos-service**

---

## Estado inicial del análisis

> Resultados tras la primera ejecución del análisis estático.  
> Los valores exactos se obtienen del dashboard y se completan en esta tabla.

| Categoría | Cantidad | Severidad / Rating |
|---|---|---|
| Bugs | 2 | C |
| Vulnerabilidades | 0 | A |
| Code Smells | 6+ | B |
| Deuda técnica | ~30 min | — |
| Cobertura (líneas) | ~45% | — |
| Cobertura (ramas) | ~30% | — |
| Duplicaciones | 0% | — |

> **Nota:** completar los valores exactos con los números reales del dashboard tras ejecutar el análisis.

---

## Hallazgos principales identificados

### Bug 1: Retorno de null en buscar()

- **Archivo:** `ProductoService.java`, línea 89
- **Regla SonarQube:** `java:S2637` — método puede retornar null
- **Descripción:** El método `buscar(Long id)` utiliza `orElse(null)` cuando el producto no existe en la base de datos. Cualquier llamador que no verifique el retorno producirá un `NullPointerException` en tiempo de ejecución.
- **Severidad:** Major
- **Código afectado:**
  ```java
  public Producto buscar(Long id) {
      return repo.findById(id).orElse(null); // Bug: retorna null
  }
  ```
- **Corrección esperada (Post-Contenido 2):** retornar `Optional<Producto>` o lanzar `EntityNotFoundException`.

---

### Bug 2: Campo sin restricción de nulidad en base de datos

- **Archivo:** `Producto.java`, línea 28
- **Regla SonarQube:** `java:S2637` — campo nullable en entidad JPA
- **Descripción:** El campo `nombre` no tiene `@Column(nullable=false)`, por lo que la base de datos acepta productos sin nombre. Esto viola la integridad referencial y puede causar datos inconsistentes.
- **Severidad:** Major
- **Código afectado:**
  ```java
  private String nombre; // Bug: falta @Column(nullable=false)
  ```
- **Corrección esperada (Post-Contenido 2):** agregar `@Column(nullable=false)` y validación `@NotBlank`.

---

### Code Smell 1: Inyección de dependencia por campo (@Autowired)

- **Archivo:** `ProductoService.java`, línea 37
- **Regla SonarQube:** `java:S3305` / `java:S6813` — inyección por campo desaconsejada
- **Descripción:** Usar `@Autowired` sobre un campo privado impide hacer tests unitarios sin un contenedor Spring, dificulta la inmutabilidad y oculta las dependencias del servicio.
- **Severidad:** Minor
- **Código afectado:**
  ```java
  @Autowired
  private ProductoRepository repo; // debería ser inyección por constructor
  ```
- **Corrección esperada:** inyección por constructor con campo `final`.

---

### Code Smell 2: Método con alta Complejidad Ciclomática

- **Archivo:** `ProductoService.java`, línea 64 (método `procesarProducto`)
- **Regla SonarQube:** `java:S3776` — complejidad cognitiva > 5
- **Descripción:** El método `procesarProducto()` tiene una Complejidad Ciclomática estimada de 8, mezclando validación de nombre, validación de precio (3 reglas), validación de stock, construcción del objeto y persistencia. Debería dividirse en métodos más pequeños.
- **Severidad:** Critical
- **Código afectado:** método `procesarProducto(String n, Double p, Integer s, String cat, boolean activo, String proveedor)`
- **Corrección esperada:** extraer métodos `validarNombre()`, `validarPrecio()` y `validarStock()`.

---

### Code Smell 3: Uso de equals("") en vez de isBlank()

- **Archivo:** `ProductoService.java`, línea 70
- **Regla SonarQube:** `java:S5785` — usar `isBlank()` disponible desde Java 11
- **Descripción:** La condición `n.equals("")` no detecta cadenas con solo espacios en blanco (p.ej. `"   "`). Desde Java 11 existe `String.isBlank()` que cubre ambos casos.
- **Severidad:** Minor
- **Código afectado:**
  ```java
  if (n == null || n.equals("")) { // debería ser n.isBlank()
  ```

---

### Code Smell 4: Lógica de negocio en entidad JPA

- **Archivo:** `Producto.java`, línea 38 (método `getEstado`)
- **Regla SonarQube:** `java:S6539` — God class / lógica de negocio en entidad
- **Descripción:** Las entidades JPA deben ser contenedores de datos. El método `getEstado()` introduce lógica de negocio compleja (CC=8) directamente en la entidad, acoplando persistencia con reglas de dominio.
- **Severidad:** Major
- **Corrección esperada:** mover `getEstado()` a un servicio o a un objeto de valor `EstadoStock`.

---

### Code Smell 5: Rama inalcanzable en getEstado()

- **Archivo:** `Producto.java`, línea 47
- **Regla SonarQube:** `java:S128` — código inalcanzable (dead code)
- **Descripción:** El último `return "DESCONOCIDO"` nunca se ejecuta porque las condiciones anteriores cubren todos los valores posibles de `stock` (null, 0, >0). SonarQube lo detecta como dead code.
- **Severidad:** Minor
- **Código afectado:**
  ```java
  if (stock > 100) return "EXCEDENTE";
  return "DESCONOCIDO"; // Code Smell: rama inalcanzable
  ```

---

### Code Smell 6: Comentario TODO sin resolver

- **Archivo:** `ProductoService.java`, línea 85
- **Regla SonarQube:** `java:S1134` — comentario TODO en código de producción
- **Descripción:** El comentario `// TODO: implementar lógica de categoría y proveedor` indica funcionalidad incompleta comprometida al repositorio. Los TODOs deben convertirse en tareas de gestión de proyecto.
- **Severidad:** Info

---

## Capturas del dashboard

> Agregar las capturas reales tras ejecutar el análisis.

### Dashboard general
<img width="794" height="933" alt="image" src="https://github.com/user-attachments/assets/974e0cd3-8244-4e85-b7b6-e52db67f159b" />


### Detalle de Bugs
<img width="1275" height="879" alt="image" src="https://github.com/user-attachments/assets/59658398-8082-4a61-a4e3-e0daa474abcd" />


### Detalle de Code Smells
<img width="1271" height="795" alt="image" src="https://github.com/user-attachments/assets/2b321021-2a20-46ba-bbd5-b80a8b137470" />


---

## Estructura del repositorio

```
productos-service/
├── src/
│   ├── main/
│   │   ├── java/com/universidad/productosservice/
│   │   │   ├── ProductosServiceApplication.java
│   │   │   ├── controller/
│   │   │   │   └── ProductoController.java
│   │   │   ├── domain/
│   │   │   │   └── Producto.java          ← bugs y code smells intencionales
│   │   │   ├── repository/
│   │   │   │   └── ProductoRepository.java
│   │   │   └── service/
│   │   │       └── ProductoService.java   ← bugs y code smells intencionales
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/universidad/productosservice/
│           ├── ProductoServiceTest.java   ← cobertura parcial intencional
│           └── ProductoTest.java
├── docs/                                  ← capturas del dashboard SonarQube
├── pom.xml                                ← JaCoCo + SonarQube configurados
├── sonar-project.properties               ← configuración del análisis
└── README.md
```

---

## Historial de commits sugerido

```
git commit -m "feat: setup inicial proyecto Spring Boot con dependencias"
git commit -m "feat: agregar entidades y servicio con problemas de calidad intencionales"
git commit -m "test: agregar tests unitarios con cobertura parcial"
git commit -m "docs: documentar hallazgos del análisis SonarQube en README"
```

---

## Tecnologías utilizadas

| Tecnología | Versión | Propósito |
|---|---|---|
| Spring Boot | 3.2.5 | Framework principal |
| Java | 21 | Lenguaje |
| Maven | 3.9+ | Gestión de dependencias y build |
| H2 Database | Runtime | Base de datos embebida |
| Lombok | Latest | Reducción de boilerplate |
| JaCoCo | 0.8.11 | Reporte de cobertura de código |
| SonarQube | Community | Análisis estático de calidad |
| Docker | Latest | Contenedor para SonarQube |

---

*Laboratorio Post-Contenido 1 — Patrones de Diseño de Software · UDES 2026*
