package com.pewnyregion.region.data.service.service;

import com.pewnyregion.region.data.service.component.BackgroundJobQueue;
import com.pewnyregion.region.data.service.entity.ImportJobEntity;
import com.pewnyregion.region.data.service.exception.ConflictException;
import com.pewnyregion.region.data.service.model.JobResponse;
import com.pewnyregion.region.data.service.model.TargetedImportRequest;
import com.pewnyregion.region.data.service.model.consts.ImportJobStatus;
import com.pewnyregion.region.data.service.model.consts.ImportJobType;
import com.pewnyregion.region.data.service.repository.ImportJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportJobService {

    private final ImportJobRepository repository;
    private final BdlDataImportService dataImportService;
    private final CountyImportService countyImportService;
    private final NormalizationService normalizationService;
    private final CountyDetailsCacheService countyDetailsCacheService;
    private final BackgroundJobQueue jobQueue;
    private final BdlStatisticalDataImportService bdlStatisticalDataImportService;

    public Mono<JobResponse> submitFullImport() {
        return createAndEnqueue(ImportJobType.FULL,
                dataImportService.runFullImport()
                                 .flatMap(importSummary -> normalizationService.calculateAndSaveScoresForAllYears()
                                                                               .doOnSuccess(ignored -> countyDetailsCacheService.invalidateAll())
                                                                               .map(normSummary -> importSummary.toMessage() + " | " + normSummary.toMessage())));
    }

    public Mono<JobResponse> submitTargetedImport(TargetedImportRequest request) {
        return createAndEnqueue(ImportJobType.TARGETED,
                dataImportService.runTargetedImport(request.apiNames(), request.years())
                                 .flatMap(importSummary -> normalizationService.calculateAndSaveScoresForYears(request.years())
                                                                               .doOnSuccess(ignored -> countyDetailsCacheService.invalidateAll())
                                                                               .map(normSummary -> importSummary.toMessage() + " | " + normSummary.toMessage())));
    }

    public Mono<JobResponse> submitCountiesImport() {
        return createAndEnqueue(ImportJobType.COUNTIES,
                countyImportService.runImportLogic().thenReturn("Counties imported successfully")
        );
    }

    public Mono<JobResponse> submitBdlCountyImport() {
        return createAndEnqueue(
                ImportJobType.BDL_COUNTIES,
                "BDL",
                importJobId -> bdlStatisticalDataImportService.importAllCounties(importJobId)
                                                              .map(importedRecords ->
                                                                      "BDL county import completed. Imported records: " + importedRecords
                                                              )
        );
    }

    public Mono<JobResponse> submitBdlUnitImport(String terytCode) {
        return createAndEnqueue(
                ImportJobType.TERRITORIAL_UNIT_DATA,
                "BDL",
                importJobId -> bdlStatisticalDataImportService.importUnit(terytCode, importJobId)
                                                              .map(importedRecords ->
                                                                      "Imported " + importedRecords
                                                                              + " statistical records for TERYT "
                                                                              + terytCode
                                                              )
        );
    }

    public Mono<JobResponse> getJobResponse(String id) {
        return repository.findById(id)
                         .map(this::toResponse)
                         .switchIfEmpty(Mono.error(new IllegalArgumentException("Job not found: " + id)));
    }

    private Mono<JobResponse> createAndEnqueue(ImportJobType type, Mono<String> task) {
        return createAndEnqueue(type, null, ignored -> task);
    }

    private Mono<JobResponse> createAndEnqueue(
            ImportJobType type,
            String provider,
            Function<String, Mono<String>> taskFactory
    ) {
        return repository.save(buildPendingJob(type, provider))
                         .onErrorMap(
                                 DataIntegrityViolationException.class,
                                 e -> new ConflictException(
                                         "Another import is already running or pending"
                                 )
                         )
                         .flatMap(job ->
                                 jobQueue.enqueueJob(
                                         job.getId(),
                                         taskFactory.apply(job.getId())
                                 ).thenReturn(job)
                         )
                         .map(this::toResponse);
    }

    private ImportJobEntity buildPendingJob(ImportJobType type) {
        return buildPendingJob(type, null);
    }

    private ImportJobEntity buildPendingJob(ImportJobType type, String provider) {
        return ImportJobEntity.builder()
                              .id(UUID.randomUUID().toString())
                              .jobType(type)
                              .status(ImportJobStatus.PENDING)
                              .provider(provider)
                              .startedAt(LocalDateTime.now())
                              .message("Import enqueued")
                              .isNew(true)
                              .build();
    }

    private JobResponse toResponse(ImportJobEntity job) {
        return new JobResponse(job.getId(), job.getStatus().name(), job.getJobType().name());
    }
}