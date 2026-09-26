package com.vikrant.urlshortener.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "click_events",
        indexes = {
                @Index(
                        name = "idx_click_events_url_clicked_at",
                        columnList = "url_id, clicked_at"
                )
        }
)
public class ClickEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "url_id",
            nullable = false
    )
    private Url url;

    @Column(nullable = false)
    private LocalDateTime clickedAt;

    public Long getId() {
        return id;
    }

    public Url getUrl() {
        return url;
    }

    public void setUrl(Url url) {
        this.url = url;
    }

    public LocalDateTime getClickedAt() {
        return clickedAt;
    }

    public void setClickedAt(LocalDateTime clickedAt) {
        this.clickedAt = clickedAt;
    }
}