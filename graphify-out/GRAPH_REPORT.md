# Graph Report - PewnyRegion  (2026-09-26)

## Corpus Check
- Corpus is ~11,884 words - fits in a single context window. You may not need a graph.

## Summary
- 427 nodes · 1216 edges · 15 communities (12 shown, 3 thin omitted)
- Extraction: 95% EXTRACTED · 5% INFERRED · 0% AMBIGUOUS · INFERRED: 65 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Testing Infrastructure
- Data Access Layer
- External API Integration
- Integration Tests
- Exception Framework
- Domain Entities
- Job Queue & Lifecycle
- REST API Layer
- Variable Service
- Map Service
- Application Entry Point
- Gradle Wrapper

## God Nodes (most connected - your core abstractions)
1. `ImportJobService` - 20 edges
2. `ImportJobEntity` - 19 edges
3. `NormalizationServiceTest` - 17 edges
4. `BackgroundJobQueue` - 16 edges
5. `BdlVariableEntity` - 16 edges
6. `NormalizationService` - 16 edges
7. `VariableService` - 16 edges
8. `ImportJobControllerTest` - 16 edges
9. `MapControllerTest` - 16 edges
10. `VariableServiceTest` - 16 edges

## Surprising Connections (you probably didn't know these)
- `CountyImportService` --references--> `BdlApiClient`  [EXTRACTED]
  region-data-service/src/main/java/com/pewnyregion/region/data/service/service/CountyImportService.java → region-data-service/src/main/java/com/pewnyregion/region/data/service/client/BdlApiClient.java
- `BackgroundJobQueue` --references--> `ImportJobRepository`  [EXTRACTED]
  region-data-service/src/main/java/com/pewnyregion/region/data/service/component/BackgroundJobQueue.java → region-data-service/src/main/java/com/pewnyregion/region/data/service/repository/ImportJobRepository.java
- `ImportJobService` --references--> `BackgroundJobQueue`  [EXTRACTED]
  region-data-service/src/main/java/com/pewnyregion/region/data/service/service/ImportJobService.java → region-data-service/src/main/java/com/pewnyregion/region/data/service/component/BackgroundJobQueue.java
- `MapValidator` --references--> `MapRepository`  [EXTRACTED]
  region-data-service/src/main/java/com/pewnyregion/region/data/service/component/MapValidator.java → region-data-service/src/main/java/com/pewnyregion/region/data/service/repository/MapRepository.java
- `MapController` --references--> `MapService`  [EXTRACTED]
  region-data-service/src/main/java/com/pewnyregion/region/data/service/controller/MapController.java → region-data-service/src/main/java/com/pewnyregion/region/data/service/service/MapService.java

## Import Cycles
- None detected.

## Communities (15 total, 3 thin omitted)

### Community 0 - "Testing Infrastructure"
Cohesion: 0.07
Nodes (37): any, arrays, collections, com.fasterxml.jackson.databind.ObjectMapper, eq, file, flux, get_all_variables_response_json (+29 more)

### Community 1 - "Data Access Layer"
Cohesion: 0.10
Nodes (18): objects, org.springframework.data.r2dbc.repository.Modifying, org.springframework.data.r2dbc.repository.Query, org.springframework.data.r2dbc.repository.R2dbcRepository, org.springframework.data.repository.reactive.ReactiveCrudRepository, org.springframework.stereotype.Repository, reactor.core.publisher.Mono, NormalizationStatsDto (+10 more)

### Community 2 - "External API Integration"
Cohesion: 0.07
Nodes (28): collectionutils, corsconfiguration, duration, httpclient, insecuretrustmanagerfactory, list, lombok.extern.slf4j.Slf4j, org.springframework.context.annotation.Bean (+20 more)

### Community 3 - "Integration Tests"
Cohesion: 0.09
Nodes (19): assertthat, autowired, classpathresource, dockerimagename, get_map_county_scores_api_path, io.r2dbc.spi.ConnectionFactory, org.junit.jupiter.api.Test, org.springframework.boot.test.context.SpringBootTest (+11 more)

### Community 4 - "Exception Framework"
Cohesion: 0.09
Nodes (22): collectors, dataintegrityviolationexception, fielderror, org.springframework.http.HttpStatus, org.springframework.http.HttpStatusCode, org.springframework.http.ProblemDetail, org.springframework.web.bind.annotation.ExceptionHandler, org.springframework.web.bind.annotation.RestControllerAdvice (+14 more)

### Community 5 - "Domain Entities"
Cohesion: 0.16
Nodes (24): com.fasterxml.jackson.annotation.JsonIgnoreProperties, id, localdatetime, lombok.AllArgsConstructor, lombok.Builder, lombok.Data, lombok.Getter, lombok.NoArgsConstructor (+16 more)

### Community 6 - "Job Queue & Lifecycle"
Cohesion: 0.08
Nodes (24): completed, consumer, failed, io.opentelemetry.api.OpenTelemetry, Many, opentelemetryappender, org.springframework.beans.factory.DisposableBean, org.springframework.beans.factory.InitializingBean (+16 more)

### Community 7 - "REST API Layer"
Cohesion: 0.17
Nodes (15): lombok.RequiredArgsConstructor, org.springframework.http.ResponseEntity, org.springframework.validation.annotation.Validated, org.springframework.web.bind.annotation.GetMapping, org.springframework.web.bind.annotation.PostMapping, org.springframework.web.bind.annotation.RequestMapping, org.springframework.web.bind.annotation.RestController, pathvariable (+7 more)

### Community 8 - "Variable Service"
Cohesion: 0.13
Nodes (11): arraylist, collection, map, org.springframework.stereotype.Service, VariableDirection, DESTIMULANT, STIMULANT, VariableResponse (+3 more)

### Community 9 - "Map Service"
Cohesion: 0.14
Nodes (12): jakarta.validation.constraints.AssertTrue, max, min, notempty, notnull, reactor.core.publisher.Flux, MapValidator, MapCountyScoreDto (+4 more)

### Community 10 - "Application Entry Point"
Cohesion: 0.50
Nodes (3): org.springframework.boot.autoconfigure.SpringBootApplication, RegionDataServiceApplication, springapplication

### Community 11 - "Gradle Wrapper"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **6 isolated node(s):** `PENDING`, `FULL`, `TARGETED`, `COUNTIES`, `STIMULANT` (+1 more)
  These have ≤1 connection - possible missing edges. (Counts symbols only; 68 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **3 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ImportJobService` connect `REST API Layer` to `Testing Infrastructure`, `Data Access Layer`, `External API Integration`, `Integration Tests`, `Exception Framework`, `Domain Entities`, `Job Queue & Lifecycle`, `Variable Service`?**
  _High betweenness centrality (0.040) - this node is a cross-community bridge._
- **Why does `ImportJobEntity` connect `Domain Entities` to `Data Access Layer`, `Exception Framework`, `Job Queue & Lifecycle`, `REST API Layer`?**
  _High betweenness centrality (0.034) - this node is a cross-community bridge._
- **What connects `PENDING`, `FULL`, `TARGETED` to the rest of the system?**
  _6 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Testing Infrastructure` be split into smaller, more focused modules?**
  _Cohesion score 0.0662004662004662 - nodes in this community are weakly interconnected._
- **Should `Data Access Layer` be split into smaller, more focused modules?**
  _Cohesion score 0.09764309764309764 - nodes in this community are weakly interconnected._
- **Should `External API Integration` be split into smaller, more focused modules?**
  _Cohesion score 0.06848357791754019 - nodes in this community are weakly interconnected._
- **Should `Integration Tests` be split into smaller, more focused modules?**
  _Cohesion score 0.09146341463414634 - nodes in this community are weakly interconnected._