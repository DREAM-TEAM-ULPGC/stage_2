# Search Service (Stage 2)

Java 17 + Javalin microservice exposing search APIs over the Stage 1 artifacts:
- `index/inverted_index.json`
- `datamart/datamart.db` (SQLite with table `books(book_id,title,author,language)`)

## Run
```bash
mvn clean package
INDEX_PATH=../inverted_index/inverted_index.json \
DB_PATH=../datamart/datamart.db \
PORT=8082 \
java -jar target/search-service-1.0.0.jar
```
## Run with Docker
docker build -t search-service .
docker run -p 8082:8082 \
  -v ./inverted_index:/data/inverted_index \
  -v ./datamart:/data/datamart \
  search-service

## Endpoints
- `GET /health`
- `GET /status — extended status (service info, uptime, paths, port)`
- `GET /book/{id}`
- `GET /search?q=term1+term2&mode=and&author=austen&language=en&page=1&pageSize=10`
- `POST /admin/reload` (reloads index and DB)

## Notes
- Scoring: simple IDF sum (upgrade to TF-IDF if term frequencies are available).
- Filtering by `author` (contains, case-insensitive) and `language` (prefix).
