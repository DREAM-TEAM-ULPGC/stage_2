# Stage 2 – Professionalization of the Java Project

## Introduction
This project corresponds to Stage 2 of the DREAM-TEAM-ULPGC initiative. Its goal is to professionalize the development of a distributed Java-based system, following clean architecture and microservices design principles. The repository includes multiple independent services (control, ingestion, indexer, and search), each responsible for a specific domain, providing modularity, scalability, and maintainability. This stage focuses on production-quality code, integration between services, proper testing, and full documentation of the system.

## Repository Structure
The repository includes the following modules:
- /control-service – Central orchestrator service that coordinates operations among all other services. It manages workflow execution, system scheduling, and overall service communication.
- /ingestion-service – Responsible for retrieving, validating, and processing raw data from external sources. It standardizes input data and makes it available for indexing.
- /indexer-service – Processes the ingested data, building search-friendly index structures that can later be queried efficiently by the search service.
- /search-service – Provides endpoints for querying indexed data, supporting filters, pagination, and ranking. It acts as the public interface for data retrieval.
- /docs – Contains technical documentation, including architecture diagrams, API definitions, and service workflows.
- pom.xml – Main build configuration file for Maven that handles dependency management across all modules.
- README.md – This documentation file describing the system, installation, and development standards.

Each service is a standalone Spring Boot microservice and can be executed, built, and deployed independently.

## Technologies and Tools
- Programming Language: Java 21
- Framework: Spring Boot 3.x
- Build System: Maven
- API Design: RESTful APIs using Spring Web
- Data Storage: Internal repositories and index structures (specific backend configurable)
- Testing: JUnit 5, Mockito, and integration tests with Spring Test
- Logging: SLF4J and Logback
- Code Quality: Checkstyle, PMD, and SonarLint
- Version Control: Git and GitHub
- Continuous Integration: GitHub Actions for automated build, test, and code analysis

## Architecture Overview
The system follows a microservices architecture based on four main services:
- The Control Service coordinates the execution of ingestion, indexing, and search operations.
- The Ingestion Service collects and validates data from external APIs or sources, ensuring standard formats.
- The Indexer Service transforms this ingested data into a structured and optimized index for fast search queries.
- The Search Service exposes endpoints that allow users or other applications to search and retrieve information from the indexed data.
Each service is independently deployable, communicates via REST endpoints, and follows the same internal architectural layering:
- controller – Handles REST endpoints and request/response mapping.
- application – Contains business logic and service orchestration.
- domain – Includes domain entities, models, and value objects.
- infrastructure – Implements persistence, data access, and external API integration.
- config – Defines Spring Boot configuration classes, beans, and properties.

### Communication Flow
1. Control Service triggers the ingestion process via REST call to Ingestion Service.
2. Ingestion Service retrieves data from external APIs, validates and stores it.
3. Control Service triggers the Indexer Service to process and build searchable indices.
4. Search Service receives requests from clients and returns relevant results using the built index.
All services report status and logs back to the Control Service for monitoring and orchestration.

## Installation and Execution Guide
1. Clone the repository:
   git clone https://github.com/DREAM-TEAM-ULPGC/stage_2.git
2. Move into the project directory:
   cd stage_2
3. Build the entire project:
   mvn clean install
4. To build a specific service:
   cd ingestion-service && mvn clean install
   cd indexer-service && mvn clean install
5. Run any service:
   mvn spring-boot:run
6. Run all tests:
   mvn test
Each service can be executed individually on a separate port as defined in its application.properties file. They can also run simultaneously for end-to-end testing of the data flow.

## Main Features
- Centralized orchestration of ingestion, indexing, and search workflows.
- Independent services that can be scaled horizontally.
- Modular design allowing future extension (new data sources or search filters).
- Consistent REST API design across all services.
- Logging and monitoring of workflows from the Control Service.
- Automatic data validation and normalization in the Ingestion Service.
- Efficient indexing pipeline ensuring fast search queries.
- Resilient and testable code following clean architecture standards.

## Development Guidelines
- Follow code style rules defined in checkstyle.xml.
- Document all public classes and key methods using Javadoc.
- Maintain 80% minimum test coverage on new code.
- Run mvn verify before any commit to ensure quality gates are passed.
- Branch naming convention:
  - feature/<short-description> for new features
  - bugfix/<short-description> for bug fixes
  - chore/<task> for maintenance work
- Commit message convention: use Conventional Commits (e.g., feat:, fix:, chore:).
- All merges to main branch must go through pull requests and peer review.
- Each microservice must be buildable and runnable independently.

## Example Workflow
1. Developer starts all services locally using mvn spring-boot:run in separate terminals.
2. Control Service issues POST /ingest to the Ingestion Service.
3. Ingestion Service collects and stores data, then notifies Control Service.
4. Control Service issues POST /index to the Indexer Service.
5. Indexer Service builds search indices and confirms readiness.
6. User performs GET /search?query=value on Search Service to retrieve data.
7. Control Service logs workflow completion and runtime metrics.

## License
This project is licensed under the MIT License. Please refer to the LICENSE file for details.

## Team Information
- Team: DREAM-TEAM-ULPGC
- University: Universidad de Las Palmas de Gran Canaria
- Stage: Stage 2 – Professionalization of Java Code and Distributed Microservices System

