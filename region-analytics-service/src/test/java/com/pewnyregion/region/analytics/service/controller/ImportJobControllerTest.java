package com.pewnyregion.region.analytics.service.controller;

import com.pewnyregion.region.analytics.service.exception.ConflictException;
import com.pewnyregion.region.analytics.service.exception.GlobalExceptionHandler;
import com.pewnyregion.region.analytics.service.model.JobResponse;
import com.pewnyregion.region.analytics.service.model.consts.ImportJobType;
import com.pewnyregion.region.analytics.service.service.ImportJobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ProblemDetail;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static com.pewnyregion.region.analytics.service.model.consts.ImportJobStatus.RUNNING;
import static com.pewnyregion.region.analytics.service.utils.TestConstants.POST_FULL_IMPORT_API_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportJobControllerTest {

    private static final String JOB_ID = "de717bad-d65d-423c-96ed-116a5d2f0716";
    private static final String IMPORT_IS_ALREADY_RUNNING = "Another import is already running or pending";

    private static final JobResponse FULL_IMPORT_RESPONSE = new JobResponse(
            JOB_ID,
            RUNNING.name(),
            ImportJobType.FULL.name()
    );

    private ImportJobController importJobController;
    private WebTestClient webTestClient;

    @Mock
    private ImportJobService importJobService;

    @BeforeEach
    void setUp() {
        importJobController = new ImportJobController(importJobService);
        webTestClient = WebTestClient.bindToController(importJobController)
                                     .controllerAdvice(new GlobalExceptionHandler())
                                     .build();
    }

    @Test
    void createFullImport_shouldReturnAccepted_whenNoImportRunning() {
        when(importJobService.submitFullImport()).thenReturn(Mono.just(FULL_IMPORT_RESPONSE));

        webTestClient.post()
                     .uri(POST_FULL_IMPORT_API_PATH)
                     .exchange()
                     .expectStatus().isAccepted()
                     .expectBody(JobResponse.class)
                     .isEqualTo(FULL_IMPORT_RESPONSE);

        verify(importJobService).submitFullImport();
    }

    @Test
    void createFullImport_shouldReturnConflict_whenAnotherImportIsAlreadyRunning() {
        when(importJobService.submitFullImport())
                .thenReturn(Mono.error(new ConflictException(IMPORT_IS_ALREADY_RUNNING)));

        webTestClient.post()
                     .uri(POST_FULL_IMPORT_API_PATH)
                     .exchange()
                     .expectStatus().isEqualTo(409)
                     .expectHeader().contentType("application/problem+json")
                     .expectBody(ProblemDetail.class)
                     .consumeWith(result -> {
                         ProblemDetail response = result.getResponseBody();
                         assertThat(response).isNotNull();
                         assertThat(response.getStatus()).isEqualTo(409);
                         assertThat(response.getDetail()).isEqualTo(IMPORT_IS_ALREADY_RUNNING);
                     });

        verify(importJobService).submitFullImport();
    }
}