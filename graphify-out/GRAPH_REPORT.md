# Graph Report - PewnyRegion  (2026-09-27)

## Corpus Check
- 87 files · ~12,813 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 11 file(s) not represented in the graph (top: (none) 4, .properties 4, .jar 1)

## Summary
- 475 nodes · 1380 edges · 19 communities (15 shown, 4 thin omitted)
- Extraction: 95% EXTRACTED · 5% INFERRED · 0% AMBIGUOUS · INFERRED: 75 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `cce1cd63`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ImportJobControllerTest.java
- reactor.core.publisher.Mono
- BdlDataImportService
- org.junit.jupiter.api.Test
- GlobalExceptionHandler
- ImportJobEntity
- BackgroundJobQueue.java
- lombok.RequiredArgsConstructor
- VariableService.java
- MapRequest
- RegionDataServiceApplication.java
- gradlew
- CountyDetailsControllerTest
- WebClientConfig.java
- README.md
- AverageScoreDto.java

## God Nodes (most connected - your core abstractions)
1. `ImportJobService` - 20 edges
2. `ImportJobEntity` - 19 edges
3. `CountyDetailsControllerTest` - 17 edges
4. `NormalizationServiceTest` - 17 edges
5. `BackgroundJobQueue` - 16 edges
6. `BdlVariableEntity` - 16 edges
7. `NormalizationService` - 16 edges
8. `VariableService` - 16 edges
9. `ImportJobControllerTest` - 16 edges
10. `MapControllerTest` - 16 edges

## Surprising Connections (you probably didn't know these)
- `CountyImportService` --references--> `BdlApiClient`  [EXTRACTED]
  region-data-service/src/main/java/com/pewnyregion/region/data/service/service/CountyImportService.java → region-data-service/src/main/java/com/pewnyregion/region/data/service/client/BdlApiClient.java
- `BackgroundJobQueue` --references--> `ImportJobRepository`  [EXTRACTED]
  region-data-service/src/main/java/com/pewnyregion/region/data/service/component/BackgroundJobQueue.java → region-data-service/src/main/java/com/pewnyregion/region/data/service/repository/ImportJobRepository.java
- `ImportJobService` --references--> `BackgroundJobQueue`  [EXTRACTED]
  region-data-service/src/main/java/com/pewnyregion/region/data/service/service/ImportJobService.java → region-data-service/src/main/java/com/pewnyregion/region/data/service/component/BackgroundJobQueue.java
- `CountyDetailsController` --references--> `CountyDetailsService`  [EXTRACTED]
  region-data-service/src/main/java/com/pewnyregion/region/data/service/controller/CountyDetailsController.java → region-data-service/src/main/java/com/pewnyregion/region/data/service/service/CountyDetailsService.java
- `MapController` --references--> `MapService`  [EXTRACTED]
  region-data-service/src/main/java/com/pewnyregion/region/data/service/controller/MapController.java → region-data-service/src/main/java/com/pewnyregion/region/data/service/service/MapService.java

## Import Cycles
- None detected.

## Communities (19 total, 4 thin omitted)

### Community 0 - "ImportJobControllerTest.java"
Cohesion: 0.07
Nodes (36): any, arrays, assertthat, collections, com.fasterxml.jackson.databind.ObjectMapper, eq, file, flux (+28 more)

### Community 1 - "reactor.core.publisher.Mono"
Cohesion: 0.09
Nodes (23): duration, objects, org.springframework.data.r2dbc.repository.Modifying, org.springframework.data.r2dbc.repository.Query, org.springframework.data.r2dbc.repository.R2dbcRepository, org.springframework.data.repository.reactive.ReactiveCrudRepository, org.springframework.stereotype.Repository, org.springframework.stereotype.Service (+15 more)

### Community 2 - "BdlDataImportService"
Cohesion: 0.08
Nodes (17): collectionutils, io.opentelemetry.api.OpenTelemetry, opentelemetryappender, org.springframework.beans.factory.InitializingBean, org.springframework.stereotype.Component, org.springframework.web.reactive.function.client.WebClient, reactor.util.retry.Retry, BdlApiClient (+9 more)

### Community 3 - "org.junit.jupiter.api.Test"
Cohesion: 0.09
Nodes (18): autowired, classpathresource, dockerimagename, get_map_county_scores_api_path, io.r2dbc.spi.ConnectionFactory, org.junit.jupiter.api.Test, org.springframework.boot.test.context.SpringBootTest, org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient (+10 more)

### Community 4 - "GlobalExceptionHandler"
Cohesion: 0.11
Nodes (17): collectors, fielderror, lombok.extern.slf4j.Slf4j, org.springframework.http.HttpStatus, org.springframework.http.HttpStatusCode, org.springframework.http.ProblemDetail, org.springframework.web.bind.annotation.ExceptionHandler, org.springframework.web.bind.annotation.RestControllerAdvice (+9 more)

### Community 5 - "ImportJobEntity"
Cohesion: 0.12
Nodes (27): com.fasterxml.jackson.annotation.JsonIgnoreProperties, id, localdatetime, lombok.AllArgsConstructor, lombok.Builder, lombok.Data, lombok.experimental.UtilityClass, lombok.Getter (+19 more)

### Community 6 - "BackgroundJobQueue.java"
Cohesion: 0.11
Nodes (18): completed, consumer, failed, Many, org.springframework.beans.factory.DisposableBean, org.springframework.boot.context.event.ApplicationReadyEvent, org.springframework.context.event.EventListener, reactor.core.Disposable (+10 more)

### Community 7 - "lombok.RequiredArgsConstructor"
Cohesion: 0.12
Nodes (24): dataintegrityviolationexception, lombok.RequiredArgsConstructor, modelattribute, org.springframework.http.ResponseEntity, org.springframework.validation.annotation.Validated, org.springframework.web.bind.annotation.GetMapping, org.springframework.web.bind.annotation.PostMapping, org.springframework.web.bind.annotation.RequestMapping (+16 more)

### Community 8 - "VariableService.java"
Cohesion: 0.14
Nodes (9): arraylist, collection, map, VariableDirection, DESTIMULANT, STIMULANT, VariableResponse, VariableService (+1 more)

### Community 9 - "MapRequest"
Cohesion: 0.11
Nodes (16): jakarta.validation.constraints.AssertTrue, max, min, notblank, notempty, notnull, pattern, positive (+8 more)

### Community 10 - "RegionDataServiceApplication.java"
Cohesion: 0.50
Nodes (3): org.springframework.boot.autoconfigure.SpringBootApplication, RegionDataServiceApplication, springapplication

### Community 11 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 15 - "CountyDetailsControllerTest"
Cohesion: 0.20
Nodes (5): CountyDetailsResponse, VariableDetail, YearlyData, CountyDetailsControllerTest, ResponseSpec

### Community 16 - "WebClientConfig.java"
Cohesion: 0.15
Nodes (14): corsconfiguration, httpclient, insecuretrustmanagerfactory, org.springframework.context.annotation.Bean, org.springframework.context.annotation.Configuration, org.springframework.web.cors.reactive.CorsWebFilter, reactorclienthttpconnector, CorsConfig (+6 more)

### Community 17 - "README.md"
Cohesion: 0.40
Nodes (4): About the Project, Core Features & Goals, Data Continuity & Variable Mapping, Example: The `CRIMES` Indicator

## Knowledge Gaps
- **9 isolated node(s):** `AverageScoreDto`, `PENDING`, `FULL`, `TARGETED`, `COUNTIES` (+4 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 79 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **4 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ImportJobService` connect `lombok.RequiredArgsConstructor` to `ImportJobControllerTest.java`, `reactor.core.publisher.Mono`, `BdlDataImportService`, `org.junit.jupiter.api.Test`, `GlobalExceptionHandler`, `ImportJobEntity`, `BackgroundJobQueue.java`?**
  _High betweenness centrality (0.033) - this node is a cross-community bridge._
- **Why does `GlobalExceptionHandler` connect `GlobalExceptionHandler` to `ImportJobControllerTest.java`?**
  _High betweenness centrality (0.029) - this node is a cross-community bridge._
- **What connects `AverageScoreDto`, `PENDING`, `FULL` to the rest of the system?**
  _9 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `ImportJobControllerTest.java` be split into smaller, more focused modules?**
  _Cohesion score 0.07191316146540028 - nodes in this community are weakly interconnected._
- **Should `reactor.core.publisher.Mono` be split into smaller, more focused modules?**
  _Cohesion score 0.08593396653098145 - nodes in this community are weakly interconnected._
- **Should `BdlDataImportService` be split into smaller, more focused modules?**
  _Cohesion score 0.08170731707317073 - nodes in this community are weakly interconnected._
- **Should `org.junit.jupiter.api.Test` be split into smaller, more focused modules?**
  _Cohesion score 0.09146341463414634 - nodes in this community are weakly interconnected._