package com.nguyenquyen.mockproject_062026_group3.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "care_tasks")
public class CareTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_type", nullable = false, length = 50)
    private String taskType;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // PENDING, COMPLETED, MISSED

    @Column(name = "is_abnormal_flagged", nullable = false)
    @Builder.Default
    private Boolean isAbnormalFlagged = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "care_intervention_id", nullable = false)
    private CareIntervention careIntervention;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_cna_id")
    private User assignedCna;

    @Column(name = "scheduled_time", nullable = false)
    private OffsetDateTime scheduledTime;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;
}
