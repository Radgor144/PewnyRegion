package com.pewnyregion.region.data.service.service;

import com.pewnyregion.region.data.service.component.BackgroundJobQueue;
import com.pewnyregion.region.data.service.entity.ImportJobEntity;
import com.pewnyregion.region.data.service.exception.ConflictException;
import com.pewnyregion.region.data.service.model.JobResponse;
import com.pewnyregion.region.data.service.model.NormalizationSummary;
import com.pewnyregion.region.data.service.model.TargetedImportRequest;
import com.pewnyregion.region.data.service.model.consts.ImportJobStatus;
import com.pewnyregion.region.data.service.model.consts.ImportJobType;
import com.pewnyregion.region.data.service.repository.ImportJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportJobService {

    private static final String BDL_PROVIDER = "BDL";
    private static final String TERYT_PROVIDER = "TERYT";

    private final ImportJobRepository importJobRepository;
    private final BackgroundJobQueue jobQueue;

    private final TercCatalogImportService tercCatalogImportService;
    private final BdlUnitMappingImportService bdlUnitMappingImportService;
    private final BdlStatisticalDataImportService bdlStatisticalDataImportService;

    private final NormalizationService normalizationService;
    private final CountyDetailsCacheService countyDetailsCacheService;

    public Mono<JobResponse> submitTerytCatalogImport() {
        return createAndEnqueue(
                ImportJobType.TERYT_CATALOG,
                TERYT_PROVIDER,
                jobId -> tercCatalogImportService.importCatalog()
                                                 .thenReturn("TERYT catalog imported successfully")
        );
    }

    public Mono<JobResponse> submitBdlUnitMappingsImport() {
        return createAndEnqueue(
                ImportJobType.UNIT_MAPPINGS,
                BDL_PROVIDER,
                jobId -> bdlUnitMappingImportService.importMappings()
                                                    .thenReturn("BDL unit mappings imported successfully")
        );
    }

    public Mono<JobResponse> submitFullImport() {
        return createAndEnqueue(
                ImportJobType.FULL,
                BDL_PROVIDER,
                jobId -> importAndNormalize(
                        jobId,
                        bdlStatisticalDataImportService::importAllCounties,
                        importedRecords -> "BDL county import completed. Imported records: " + importedRecords
                )
        );
    }

    public Mono<JobResponse> submitTargetedImport(TargetedImportRequest request) {
        return createAndEnqueue(
                ImportJobType.TARGETED,
                BDL_PROVIDER,
                jobId -> importAndNormalize(
                        jobId,
                        currentJobId -> bdlStatisticalDataImportService.importTargetedCounties(request, currentJobId),
                        importedRecords -> "BDL targeted import completed. Imported records: " + importedRecords
                )
        );
    }

    public Mono<JobResponse> submitBdlUnitImport(String terytCode) {
        return createAndEnqueue(
                ImportJobType.SINGLE_UNIT,
                BDL_PROVIDER,
                jobId -> importAndNormalize(
                        jobId,
                        currentJobId -> bdlStatisticalDataImportService.importUnit(terytCode, currentJobId),
                        importedRecords -> "Imported " + importedRecords + " statistical records for TERYT " + terytCode
                )
        );
    }

    public Mono<JobResponse> submitNormalization() {
        return createAndEnqueue(
                ImportJobType.NORMALIZATION,
                null,
                jobId -> invalidateCacheAfterNormalization(normalizationService.calculateAndSaveScores(jobId, BDL_PROVIDER, false))
                        .map(NormalizationSummary::toMessage)
        );
    }

    public Mono<JobResponse> getJobResponse(String jobId) {
        return importJobRepository.findById(jobId)
                                  .map(ImportJobService::toResponse);
    }

    private Mono<JobResponse> createAndEnqueue(ImportJobType jobType,
                                               String provider,
                                               Function<String, Mono<String>> taskFactory) {
        return importJobRepository.save(buildPendingJob(jobType, provider))
                                  .onErrorMap(DataIntegrityViolationException.class,
                                              exception -> new ConflictException("Another import is already running or pending"))
                                  .flatMap(job ->
                                          jobQueue.enqueueJob(
                                                          job.getId(),
                                                          Mono.defer(() -> taskFactory.apply(job.getId()))
                                                  )
                                                  .thenReturn(job)
                                  )
                                  .map(ImportJobService::toResponse);
    }

    private <T> Mono<String> importAndNormalize(String jobId,
                                                Function<String, Mono<T>> importOperation,
                                                Function<T, String> successMessage) {
        return bdlUnitMappingImportService.ensureMappings()
                                          .then(Mono.defer(() -> importOperation.apply(jobId)))
                                          .flatMap(importedRecords ->
                                                  normalizeAfterImport(jobId)
                                                          .map(summary ->
                                                                  successMessage.apply(importedRecords)
                                                                          + ". "
                                                                          + summary.toMessage()
                                                          )
                                          );
    }

    private Mono<NormalizationSummary> normalizeAfterImport(String importJobId) {
        return invalidateCacheAfterNormalization(normalizationService.calculateAndSaveScores(importJobId, BDL_PROVIDER, true));
    }

    private Mono<NormalizationSummary> invalidateCacheAfterNormalization(Mono<NormalizationSummary> normalization) {
        return normalization.doOnNext(ignored -> countyDetailsCacheService.invalidateAll());
    }

    private static ImportJobEntity buildPendingJob(ImportJobType jobType, String provider) {
        return ImportJobEntity.builder()
                              .id(UUID.randomUUID().toString())
                              .jobType(jobType)
                              .provider(provider)
                              .status(ImportJobStatus.PENDING)
                              .startedAt(null)
                              .finishedAt(null)
                              .message("Import enqueued")
                              .isNew(true)
                              .build();
    }

    private static JobResponse toResponse(ImportJobEntity job) {
        return new JobResponse(job.getId(),
                               job.getStatus().name(),
                               job.getJobType().name()
        );
    }
}