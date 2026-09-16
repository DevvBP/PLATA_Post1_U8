# Unidad 8: Catálogo con JPA/Hibernate

Laboratorio de la materia **Programación Web** — Universidad de Santander (UDES).  
Implementa un catálogo de productos completo utilizando **Spring Boot 3**, **Spring Data JPA**,
**Hibernate** y **MySQL**, cubriendo los temas de la Unidad 8:

- Mapeo objeto-relacional con anotaciones JPA (`@Entity`, `@Table`, `@Column`, etc.)
- Relaciones entre entidades: `@ManyToOne` y `@OneToMany` (bidireccional)
- Repositorios con Spring Data JPA y consultas JPQL personalizadas con `@Query`
- Estrategia `FetchType.LAZY` y uso de `JOIN FETCH` para evitar el problema N+1
- Validaciones de capa con Bean Validation (`@NotBlank`, `@Size`, `@Positive`)
- Controladores MVC con patrón Post/Redirect/Get y mensajes flash

---

## Estructura del proyecto

```
Plata_Post1_U8/
├── catalogo-jpa/                 ← Proyecto Spring Boot único
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/universidad/catalogo/
│       │   ├── CatalogoApplication.java
│       │   ├── controller/
│       │   │   ├── HomeController.java
│       │   │   ├── CategoriaController.java
│       │   │   └── ProductoController.java
│       │   ├── model/
│       │   │   ├── Categoria.java
│       │   │   └── Producto.java
│       │   ├── repository/
│       │   │   ├── CategoriaRepository.java
│       │   │   └── ProductoRepository.java
│       │   └── service/
│       │       ├── CategoriaService.java
│       │       └── ProductoService.java
│       └── resources/
│           ├── application.properties
│           └── templates/
│               ├── index.html
│               ├── categorias/
│               │   ├── lista.html
│               │   ├── formulario.html
│               │   └── confirmar-eliminar.html
│               └── productos/
│                   ├── lista.html
│                   ├── formulario.html
│                   ├── confirmar-eliminar.html
│                   └── filtrados.html
├── capturas/                     ← Capturas de pantalla del sistema en ejecución
└── README.md
```

---

## Configuración de la Base de Datos

Ejecuta los siguientes comandos en tu servidor MySQL como usuario `root`:

```sql
CREATE DATABASE catalogo_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'appuser'@'localhost' IDENTIFIED BY 'apppass';
GRANT ALL PRIVILEGES ON catalogo_db.* TO 'appuser'@'localhost';
FLUSH PRIVILEGES;
```

> Hibernate creará automáticamente las tablas `categorias` y `productos`
> al iniciar la aplicación gracias a `ddl-auto=update`.

---

## Instrucciones de ejecución

**Requisitos previos:**
- Java 17 o superior
- MySQL 8 en ejecución local
- Maven 3.8+ (o usar el wrapper incluido)

**Pasos:**

1. Clona el repositorio y entra a la carpeta del proyecto:
   ```bash
   git clone https://github.com/DevvBP/PLATA_Post1_U8.git
   cd PLATA_Post1_U8/catalogo-jpa
   ```

2. Crea la base de datos y el usuario (ver sección anterior).

3. Ejecuta la aplicación:
   ```bash
   ./mvnw spring-boot:run
   ```
   En Windows PowerShell:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

4. Abre el navegador en `http://localhost:8080`

---

## Endpoints principales

| Método | URL | Descripción |
|--------|-----|-------------|
| GET | `/` | Página de inicio |
| GET | `/categorias` | Listado de categorías |
| GET | `/categorias/nueva` | Formulario nueva categoría |
| POST | `/categorias/guardar` | Guardar categoría |
| GET | `/categorias/editar/{id}` | Formulario editar categoría |
| GET | `/categorias/eliminar/{id}` | Confirmación de eliminación |
| POST | `/categorias/eliminar/{id}` | Ejecutar eliminación |
| GET | `/productos` | Listado de productos |
| GET | `/productos/nuevo` | Formulario nuevo producto |
| POST | `/productos/guardar` | Guardar producto |
| GET | `/productos/editar/{id}` | Formulario editar producto |
| GET | `/productos/eliminar/{id}` | Confirmación de eliminación |
| POST | `/productos/eliminar/{id}` | Ejecutar eliminación |
| GET | `/productos/categoria/{id}/precio-mayor?minimo=X` | Consulta JPQL filtrada |

---

## Decisiones de Diseño

### 1. `ddl-auto=update` en lugar de `create`

Se eligió `spring.jpa.hibernate.ddl-auto=update` porque esta estrategia aplica únicamente
los cambios estructurales necesarios (agregar columnas, nuevas tablas) sin destruir los datos
existentes en cada reinicio. Con `create`, la base de datos se borraría y recreería en cada
arranque, lo cual haría imposible mantener datos de prueba entre sesiones de desarrollo.
`update` es el balance ideal para un entorno de laboratorio donde se itera sobre el esquema
pero se quieren conservar los registros creados manualmente.

### 2. Nombre único en Categoria y validación temprana

La columna `nombre` en `categorias` tiene una restricción `UNIQUE` a nivel de base de datos
(`@Column(unique = true)`). Sin embargo, esta validación se duplica **explícitamente en el
servicio** (`CategoriaService.guardar`) antes de llamar a `save()`. Esto proporciona dos
ventajas: (a) el mensaje de error es claro y en español, controlado por la aplicación, en lugar
de una excepción de Hibernate críptica; (b) la validación respeta la edición, es decir, permite
que una categoría mantenga su propio nombre al actualizarse comparando el ID del resultado
encontrado con el ID del objeto a guardar.

### 3. `FetchType.LAZY` explícito en Producto + `JOIN FETCH`

La relación `@ManyToOne` en `Producto` utiliza `FetchType.LAZY` de forma explícita.
Aunque `LAZY` es el comportamiento por defecto en JPA para colecciones, declararlo
explícitamente documenta la intención: la categoría **no** se carga al recuperar un producto
a menos que se acceda a ella en contexto transaccional. Para los listados donde se necesita
mostrar el nombre de la categoría, `ProductoRepository` usa una consulta con `JOIN FETCH p.categoria`,
lo que carga ambas entidades en una sola consulta SQL, eliminando el problema clásico N+1
(N productos = N consultas adicionales para la categoría de cada uno).

### 4. Sin `cascade = CascadeType.REMOVE` en Categoria

La relación `@OneToMany` en `Categoria` no incluye `cascade = CascadeType.REMOVE`.
Esta decisión protege la integridad de los datos: si se borrara una categoría con
`CascadeType.REMOVE`, todos sus productos asociados serían eliminados en cascada,
posiblemente de forma accidental. En lugar de eso, `CategoriaService.eliminar()` verifica
primero si la categoría tiene productos asociados y, si los tiene, lanza una
`IllegalStateException` con un mensaje descriptivo. Así el usuario recibe feedback claro
y ningún producto es eliminado sin intención explícita.

---

## Capturas de pantalla

Las capturas de la aplicación en funcionamiento se encuentran en la carpeta [`capturas/`](capturas/).

| Captura | Descripción |
|---------|-------------|
| `lista-categorias.png` | Vista de listado de categorías (`/categorias`) |
| `formulario-producto.png` | Formulario de nuevo producto (`/productos/nuevo`) |
| `lista-productos.png` | Vista de listado de productos (`/productos`) |
| `productos-filtrados.png` | Resultado del endpoint JPQL filtrado |

---

## Tecnologías

| Tecnología | Versión |
|------------|---------|
| Java | 17 |
| Spring Boot | 3.2.5 |
| Spring Data JPA | (incluido en Boot) |
| Hibernate | 6.x (incluido en Boot) |
| MySQL Connector/J | 8.x |
| Thymeleaf | 3.x |
| Maven | 3.8+ |

---

**Autor:** Brayan Plata — `02230132025@mail.udes.edu.co`  
**Universidad de Santander (UDES) — Programación Web**
