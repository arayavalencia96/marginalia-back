package com.marginalia.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/** Represents one ordered entry in a STEP_LIST content block. */
@Entity
@Table(name = "content_block_steps")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentBlockStep {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "content_block_id", nullable = false)
    private UUID contentBlockId;

    @Column(name = "step_order", nullable = false)
    private int stepOrder;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;
}
