package ru.itmo.is.space_marine_backend.controller;

import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Cache;
import org.hibernate.SessionFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.is.space_marine_backend.cache.CacheStatisticsAspect;
import ru.itmo.is.space_marine_backend.dto.response.CacheOperationResponse;
import ru.itmo.is.space_marine_backend.dto.response.CacheStatusResponse;


@Slf4j
@RestController
@RequestMapping("/api/cache")
public class CacheController {

    private final CacheStatisticsAspect statisticsAspect;
    private final SessionFactory sessionFactory;
    private final Cache cache;

    public CacheController(CacheStatisticsAspect statisticsAspect,
                           EntityManagerFactory entityManagerFactory) {
        this.statisticsAspect = statisticsAspect;
        this.sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        this.cache = sessionFactory.getCache();
    }

    @PostMapping("/enable")
    public ResponseEntity<CacheOperationResponse> enableLogging() {
        statisticsAspect.enableLogging();
        return ResponseEntity.ok(new CacheOperationResponse(
                true,
                "Cache statistics logging enabled"));
    }

    @PostMapping("/disable")
    public ResponseEntity<CacheOperationResponse> disableLogging() {
        statisticsAspect.disableLogging();
        return ResponseEntity.ok(new CacheOperationResponse(
                true,
                "Cache statistics logging disabled"));
    }

    @GetMapping("/status")
    public ResponseEntity<CacheStatusResponse> getCacheStatus() {
        boolean l2CacheEnabled = sessionFactory.getSessionFactoryOptions().isSecondLevelCacheEnabled();
        boolean loggingEnabled = statisticsAspect.isLoggingEnabled();

        Long hits = null;
        Long misses = null;
        Long puts = null;

        if (loggingEnabled) {
            var stats = statisticsAspect.getStatistics();
            hits = stats.getSecondLevelCacheHitCount();
            misses = stats.getSecondLevelCacheMissCount();
            puts = stats.getSecondLevelCachePutCount();
        }

        return ResponseEntity.ok(new CacheStatusResponse(
                l2CacheEnabled,
                loggingEnabled,
                hits,
                misses,
                puts));
    }

    @PostMapping("/clear")
    public ResponseEntity<CacheOperationResponse> clearCache() {
        cache.evictAllRegions();
        log.info("All L2 cache regions cleared");
        return ResponseEntity.ok(new CacheOperationResponse(
                true,
                "Cache cleared successfully"));
    }
}