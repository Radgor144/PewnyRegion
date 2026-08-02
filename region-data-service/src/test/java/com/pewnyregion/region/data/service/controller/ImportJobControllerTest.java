package com.pewnyregion.region.data.service.controller;

import com.pewnyregion.region.data.service.exception.ConflictException;
import com.pewnyregion.region.data.service.exception.GlobalExceptionHandler;
import com.pewnyregion.region.data.service.model.JobResponse;
import com.pewnyregion.region.data.service.model.TargetedImportRequest;
import com.pewnyregion.region.data.service.model.consts.ImportJobType;
import com.pewnyregion.region.data.service.service.ImportJobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ProblemDetail;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.pewnyregion.region.data.service.model.consts.ImportJobStatus.RUNNING;
import static com.pewnyregion.region.data.service.utils.TestConstants.POST_FULL_IMPORT_API_PATH;
import static com.pewnyregion.region.data.service.utils.TestConstants.POST_TARGETED_IMPORT_API_PATH;
import static com.pewnyregion.region.data.service.utils.TestConstants.POST_TERYT_IMPORT_API_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportJobControllerTest {

    private static final String JOB_ID = "de717bad-d65d-423c-96ed-116a5d2f0716";
    private static final String IMPORT_IS_ALREADY_RUNNING = "Another import is already running or pending";

    private WebTestClient webTestClient;

    @Mock
    private ImportJobService importJobService;

    @BeforeEach
    void setUp() {
        ImportJobController importJobController = new ImportJobController(importJobService);
        webTestClient = WebTestClient.bindToController(importJobController)
                                     .controllerAdvice(new GlobalExceptionHandler())
                                     .build();
    }

    @Test
    void shouldReturnAccepted_whenImportingCounties() {
        JobResponse expectedResponse = getJobResponse(ImportJobType.COUNTIES);
        when(importJobService.submitCountiesImport()).thenReturn(Mono.just(expectedResponse));

        webTestClient.post()
                     .uri(POST_TERYT_IMPORT_API_PATH)
                     .exchange()
                     .expectStatus().isAccepted()
                     .expectBody(JobResponse.class)
                     .isEqualTo(expectedResponse);

        verify(importJobService).submitCountiesImport();
    }

    @Test
    void shouldReturnAccepted_whenCreatingFullImport() {
        JobResponse expectedResponse = getJobResponse(ImportJobType.FULL);
        when(importJobService.submitFullImport()).thenReturn(Mono.just(expectedResponse));

        webTestClient.post()
                     .uri(POST_FULL_IMPORT_API_PATH)
                     .exchange()
                     .expectStatus().isAccepted()
                     .expectBody(JobResponse.class)
                     .isEqualTo(expectedResponse);

        verify(importJobService).submitFullImport();
    }

    @Test
    void shouldReturnAccepted_whenCreatingTargetedImport() {
        JobResponse expectedResponse = getJobResponse(ImportJobType.TARGETED);
        TargetedImportRequest request = new TargetedImportRequest(List.of("var1"), List.of(2023));

        when(importJobService.submitTargetedImport(any(TargetedImportRequest.class)))
                .thenReturn(Mono.just(expectedResponse));

        webTestClient.post()
                     .uri(POST_TARGETED_IMPORT_API_PATH)
                     .bodyValue(request)
                     .exchange()
                     .expectStatus().isAccepted()
                     .expectBody(JobResponse.class)
                     .isEqualTo(expectedResponse);

        verify(importJobService).submitTargetedImport(any(TargetedImportRequest.class));
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

    @Test
    void shouldReturnBadRequest_whenRequestBodyIsMissing() {
        webTestClient.post()
                     .uri(POST_TARGETED_IMPORT_API_PATH)
                     .exchange()
                     .expectStatus().isBadRequest();
    }

    @Test
    void getStatus_shouldReturnOk_whenJobExists() {
        JobResponse expectedResponse = getJobResponse(ImportJobType.FULL);
        when(importJobService.getJobResponse(JOB_ID)).thenReturn(Mono.just(expectedResponse));

        webTestClient.get()
                     .uri("/api/imports/{jobId}", JOB_ID)
                     .exchange()
                     .expectStatus().isOk()
                     .expectBody(JobResponse.class)
                     .isEqualTo(expectedResponse);

        verify(importJobService).getJobResponse(JOB_ID);
    }

    @Test
    void getStatus_shouldReturnNotFound_whenJobDoesNotExist() {
        when(importJobService.getJobResponse(JOB_ID))
                .thenReturn(Mono.error(new IllegalArgumentException("Job not found: " + JOB_ID)));

        webTestClient.get()
                     .uri("/api/imports/{jobId}", JOB_ID)
                     .exchange()
                     .expectStatus().isNotFound()
                     .expectBody().isEmpty();

        verify(importJobService).getJobResponse(JOB_ID);
    }

    private static JobResponse getJobResponse(ImportJobType importJobType) {
        return new JobResponse(JOB_ID, RUNNING.name(), importJobType.name());
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("provideInvalidTargetedImportRequests")
    void shouldReturnBadRequest_whenTargetedImportRequestIsInvalid(String testDescription, TargetedImportRequest invalidRequest) {

        webTestClient.post()
                     .uri(POST_TARGETED_IMPORT_API_PATH)
                     .bodyValue(invalidRequest)
                     .exchange()
                     .expectStatus().isBadRequest();
    }

    private static Stream<Arguments> provideInvalidTargetedImportRequests() {
        return Stream.of(
                // invalid apiNames
                Arguments.of("Null apiNames",
                        new TargetedImportRequest(null, List.of(2023))),
                Arguments.of("Empty apiNames",
                        new TargetedImportRequest(Collections.emptyList(), List.of(2023))),
                Arguments.of("Too many apiNames (over 10)",
                        new TargetedImportRequest(Collections.nCopies(21, "var"), List.of(2023))),

                // invalid years
                Arguments.of("Null years list",
                        new TargetedImportRequest(List.of("var1"), null)),
                Arguments.of("Empty years list",
                        new TargetedImportRequest(List.of("var1"), Collections.emptyList())),
                Arguments.of("Year before 2012",
                        new TargetedImportRequest(List.of("var1"), List.of(2011))),
                Arguments.of("Year after 2026",
                        new TargetedImportRequest(List.of("var1"), List.of(2027))),

                Arguments.of("Null year inside list",
                        new TargetedImportRequest(List.of("var1"), Arrays.asList(2023, null))),

                // invalid both
                Arguments.of("Null apiNames and null years (simulates empty JSON body)",
                        new TargetedImportRequest(null, null)),
                Arguments.of("Empty apiNames and empty years",
                        new TargetedImportRequest(Collections.emptyList(), Collections.emptyList())),
                Arguments.of("Invalid apiNames and invalid years",
                        new TargetedImportRequest(Collections.nCopies(21, "var"), List.of(2011, 2027)))
        );
    }
}