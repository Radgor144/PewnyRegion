package com.pewnyregion.region.data.service.service;

import com.pewnyregion.region.data.service.component.BackgroundJobQueue;
import com.pewnyregion.region.data.service.entity.ImportJobEntity;
import com.pewnyregion.region.data.service.exception.ConflictException;
import com.pewnyregion.region.data.service.model.ImportSummary;
import com.pewnyregion.region.data.service.model.JobResponse;
import com.pewnyregion.region.data.service.model.NormalizationSummary;
import com.pewnyregion.region.data.service.model.TargetedImportRequest;
import com.pewnyregion.region.data.service.repository.ImportJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ImportJobServiceTest {

    private static final String PENDING = "PENDING";
    private static final String COUNTIES = "COUNTIES";
    private static final String FULL = "FULL";
    private static final String CONFLICT_ERROR_MESSAGE = "Another import is already running or pending";
    public static final String DUPLICATE_KEY_ERROR = "Duplicate key error";

    private ImportJobService importJobService;

    @Mock
    private ImportJobRepository importJobRepository;
    @Mock
    private BdlDataImportService bdlDataImportService;
    @Mock
    private CountyImportService countyImportService;
    @Mock
    private NormalizationService normalizationService;
    @Mock
    private BackgroundJobQueue backgroundJobQueue;
    @Mock
    private CountyDetailsCacheService countyDetailsCacheService;

    @BeforeEach
    void setUp() {
        importJobService = new ImportJobService(
                importJobRepository, bdlDataImportService, countyImportService, normalizationService, countyDetailsCacheService, backgroundJobQueue
        );
    }

    @Test
    void submitCountiesImport_shouldCreateAndEnqueueJob() {
        AtomicReference<ImportJobEntity> savedJob = new AtomicReference<>();

        when(countyImportService.runImportLogic()).thenReturn(Mono.empty());
        mockSuccessfulSave(savedJob);
        when(backgroundJobQueue.enqueueJob(any(), any())).thenReturn(Mono.empty());

        StepVerifier.create(importJobService.submitCountiesImport())
                    .assertNext(response -> assertJobResponse(response, savedJob.get().getId(), PENDING, COUNTIES))
                    .verifyComplete();

        verifyInteractions();
    }

    @Test
    void submitCountiesImport_shouldReturnConflictException_whenImportAlreadyRunning() {
        when(countyImportService.runImportLogic()).thenReturn(Mono.empty());
        when(importJobRepository.save(any(ImportJobEntity.class)))
                .thenReturn(Mono.error(new DataIntegrityViolationException(DUPLICATE_KEY_ERROR)));

        StepVerifier.create(importJobService.submitCountiesImport())
                    .expectErrorSatisfies(this::assertConflictException)
                    .verify();
    }

    @Test
    void submitFullImport_shouldCreateAndEnqueueJob() {
        AtomicReference<ImportJobEntity> savedJob = new AtomicReference<>();

        mockFullImportDependencies();
        mockSuccessfulSave(savedJob);
        when(backgroundJobQueue.enqueueJob(any(), any())).thenReturn(Mono.empty());


        StepVerifier.create(importJobService.submitFullImport())
                    .assertNext(response -> assertJobResponse(response, savedJob.get().getId(), PENDING, FULL))
                    .verifyComplete();

        verifyInteractions();
    }

    @Test
    void submitFullImport_shouldReturnConflictException_whenImportAlreadyRunning() {
        mockFullImportDependencies();
        when(importJobRepository.save(any(ImportJobEntity.class)))
                .thenReturn(Mono.error(new DataIntegrityViolationException(DUPLICATE_KEY_ERROR)));

        StepVerifier.create(importJobService.submitFullImport())
                    .expectErrorSatisfies(this::assertConflictException)
                    .verify();
    }

    @Test
    void submitTargetedImport_shouldCreateAndEnqueueJob() {
        AtomicReference<ImportJobEntity> savedJob = new AtomicReference<>();
        TargetedImportRequest request = new TargetedImportRequest(List.of("crimes_total"), List.of(2023));

        lenient().when(bdlDataImportService.runTargetedImport(any(), any())).thenReturn(Mono.just(new ImportSummary(1)));
        lenient().when(normalizationService.calculateAndSaveScoresForYears(any()))
                 .thenReturn(Mono.just(new NormalizationSummary(List.of(2023))));

        mockSuccessfulSave(savedJob);
        when(backgroundJobQueue.enqueueJob(any(), any())).thenReturn(Mono.empty());

        StepVerifier.create(importJobService.submitTargetedImport(request))
                    .assertNext(response -> assertJobResponse(response, savedJob.get().getId(), PENDING, "TARGETED"))
                    .verifyComplete();

        verifyInteractions();
    }

    @Test
    void submitTargetedImport_shouldReturnConflictException_whenImportAlreadyRunning() {
        TargetedImportRequest request = new TargetedImportRequest(List.of("crimes_total"), List.of(2023));

        lenient().when(bdlDataImportService.runTargetedImport(any(), any())).thenReturn(Mono.just(new ImportSummary(1)));
        lenient().when(normalizationService.calculateAndSaveScoresForYears(any()))
                 .thenReturn(Mono.just(new NormalizationSummary(List.of(2023))));

        when(importJobRepository.save(any(ImportJobEntity.class)))
                .thenReturn(Mono.error(new DataIntegrityViolationException(DUPLICATE_KEY_ERROR)));

        StepVerifier.create(importJobService.submitTargetedImport(request))
                    .expectErrorSatisfies(this::assertConflictException)
                    .verify();
    }

    private void mockSuccessfulSave(AtomicReference<ImportJobEntity> savedJob) {
        when(importJobRepository.save(any(ImportJobEntity.class)))
                .thenAnswer(invocation -> {
                    ImportJobEntity job = invocation.getArgument(0);
                    savedJob.set(job);
                    return Mono.just(job);
                });
    }

    private void mockFullImportDependencies() {
        lenient().when(bdlDataImportService.runFullImport()).thenReturn(Mono.just(new ImportSummary(1)));
        lenient().when(normalizationService.calculateAndSaveScoresForAllYears())
                 .thenReturn(Mono.just(new NormalizationSummary(List.of(2023, 2024))));
    }

    private void assertJobResponse(JobResponse response, String expectedId, String expectedStatus, String expectedType) {
        assertThat(response.id()).isEqualTo(expectedId);
        assertThat(response.status()).isEqualTo(expectedStatus);
        assertThat(response.type()).isEqualTo(expectedType);
        assertThatCode(() -> UUID.fromString(response.id())).doesNotThrowAnyException();
    }

    private void assertConflictException(Throwable throwable) {
        assertThat(throwable).isInstanceOf(ConflictException.class);
        assertThat(throwable.getMessage()).isEqualTo(CONFLICT_ERROR_MESSAGE);
    }

    private void verifyInteractions() {
        verify(importJobRepository).save(any(ImportJobEntity.class));
        verify(backgroundJobQueue).enqueueJob(any(), any());
    }
}