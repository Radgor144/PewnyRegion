package com.pewnyregion.region.analytics.service.entity;

import com.pewnyregion.region.analytics.service.model.consts.ImportJobStatus;
import com.pewnyregion.region.analytics.service.model.consts.ImportJobType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("import_jobs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportJobEntity implements Persistable<String> {

    @Id
    private String id;
    private ImportJobType jobType;
    private ImportJobStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private String message;

    @Transient
    private boolean isNew;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return this.isNew || id == null;
    }
}