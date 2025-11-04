# Ingestion Service

Servicio REST (Javalin, Java 17) que descarga libros de Project
Gutenberg y los almacena en el **datalake** con jerarquía
`YYYYMMDD/HH/{book_id}`. Expone los endpoints exigidos en Stage 2.

## Build & Run
# Ingestion Service

Servicio REST (Javalin, Java 17) que descarga libros de **Project Gutenberg** y los almacena en el **datalake** siguiendo la jerarquía  
`YYYYMMDD/HH/{book_id}`.

Forma parte de la arquitectura **Stage 2** junto con `indexing-service`, `search-service` y el módulo de control.  
Este servicio **solo** se encarga de la ingesta de datos (no indexa ni busca).

---

## Requisitos

- **Java 17**
- **Maven 3.x**
- No requiere base de datos externa.

---

## Build & Run

```bash
mvn -q -e -DskipTests clean package
java -jar target/ingestion-service-1.0.0.jar
```

Variables de entorno:
- `PORT` → puerto HTTP (default `7001`)
- `DATALAKE_DIR` → ruta base del datalake (default `./datalake`)
- `INGESTION_LOG_FILE` → log persistente (default `./datalake/ingestions.log`)

---

## API

### `GET /status`
Comprueba que el microservicio está activo.
```json
{"service":"ingestion-service","status":"running"}
```

### `POST /ingest/{book_id}`
Descarga el libro, separa `header.txt`, `body.txt` y `raw.txt` y lo guarda en el datalake.  
**Respuesta ejemplo:**
```json
{"book_id":1342,"status":"downloaded","path":"datalake/20251008/14/1342"}
```
Si el libro ya existía:
```json
{"book_id":1342,"status":"available","path":"datalake/20251008/14/1342"}
```

### `GET /ingest/status/{book_id}`
Devuelve `"available"` si el libro ya existe en el datalake, `"missing"` en caso contrario.

### `GET /ingest/list`
Lista de todos los `book_id` disponibles.
```json
{"count":3,"books":[11,84,345]}
```

---

## Persistencia e idempotencia

- Cada ingesta exitosa se registra en **`ingestions.log`** con timestamp, ID y ruta.
- `/status` y `/list` consultan ese log, lo que garantiza persistencia tras reinicios.
- Si el libro ya existe, `POST /ingest/{book_id}` devuelve `status=available` (idempotencia).

---

## Docker

```bash
mvn -DskipTests clean package
docker build -t dreamteam/ingestion:1.0.0 .
docker run --rm -p 7001:7001   -e DATALAKE_DIR=/data/datalake   -v $PWD/data:/data   dreamteam/ingestion:1.0.0
```

---

## Ejemplo rápido de uso

```bash
# Ingestar un libro
curl -X POST http://localhost:7001/ingest/11

# Comprobar estado
curl http://localhost:7001/ingest/status/11

# Ver todos los libros
curl http://localhost:7001/ingest/list
```

---

## Licencia de los textos

Project Gutenberg distribuye libros de **dominio público**.  
La cabecera legal se mantiene íntegra en `header.txt` de cada ingesta.  
El servicio no altera ni elimina esa información.

---

``` bash
mvn -q -e -DskipTests clean package
java -jar target/ingestion-service-1.0.0.jar
```

Variables: - `PORT` (default `7001`) - `DATALAKE_DIR` (default
`./datalake`) - `INGESTION_LOG_FILE` (default
`./datalake/ingestions.log`)

## API

-   `POST /ingest/{book_id}` → descarga y parte en `header.txt`,
    `body.txt`, además de `raw.txt`.\
    Respuesta:

    ``` json
    {"book_id":1342,"status":"downloaded","path":"datalake/20251008/14/1342"}
    ```

-   `GET /ingest/status/{book_id}` → `"available"` si existe en el
    datalake.

-   `GET /ingest/list` → lista de `book_id` disponibles.

## Docker

``` bash
mvn -DskipTests clean package
docker build -t dreamteam/ingestion:1.0.0 .
docker run --rm -p 7001:7001 -e DATALAKE_DIR=/data/datalake -v $PWD/data:/data dreamteam/ingestion:1.0.0
```

## Notas

-   Idempotencia: si el libro ya está en el datalake,
    `POST /ingest/{book_id}` devuelve `status=available`.
-   Logging: cada ingesta se anota en `ingestions.log`.
