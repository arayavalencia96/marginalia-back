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

/** Represents an ordered chapter that may recursively reference a parent chapter in the same book. */
@Entity
@Table(name = "chapters")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "book_id", nullable = false)
    private UUID bookId;

    @Column(nullable = false)
    private String title;

    @Column(name = "parent_chapter_id")
    private UUID parentChapterId;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;
}
