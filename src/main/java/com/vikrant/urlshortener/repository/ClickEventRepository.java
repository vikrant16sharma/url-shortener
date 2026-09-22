package com.vikrant.urlshortener.repository;

import com.vikrant.urlshortener.entity.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface ClickEventRepository extends JpaRepository<ClickEvent,Long> {
    Long countByUrlId(Long urlId);
    Optional<ClickEvent> findFirstByUrlIdOrderByClickedAtAsc(Long urlId);

    Optional<ClickEvent> findFirstByUrlIdOrderByClickedAtDesc(Long urlId);
}
