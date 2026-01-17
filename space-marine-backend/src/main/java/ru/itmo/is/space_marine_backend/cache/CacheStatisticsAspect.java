package ru.itmo.is.space_marine_backend.cache;

import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.stereotype.Component;


@Slf4j
@Aspect
@Component
public class CacheStatisticsAspect {

    private final Statistics hibernateStatistics;
    private final CacheLoggingProperties loggingProperties;

    public CacheStatisticsAspect(EntityManagerFactory entityManagerFactory,
                                 CacheLoggingProperties loggingProperties) {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        this.hibernateStatistics = sessionFactory.getStatistics();
        this.loggingProperties = loggingProperties;
        this.hibernateStatistics.setStatisticsEnabled(loggingProperties.isEnabled());
        log.info("Hibernate Statistics initialized. Enabled: {}", loggingProperties.isEnabled());
    }


    @Around("@annotation(ru.itmo.is.space_marine_backend.cache.L2CacheTracked)")
    public Object logCacheStatistics(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!loggingProperties.isEnabled()) {
            return joinPoint.proceed();
        }

        CacheSnapshot before = takeSnapshot();
        try {
            Object result = joinPoint.proceed();
            logDelta(joinPoint, before);
            return result;
        } catch (Throwable throwable) {
            logDelta(joinPoint, before);
            throw throwable;
        }
    }


    private CacheSnapshot takeSnapshot() {
        return new CacheSnapshot(
                hibernateStatistics.getSecondLevelCacheHitCount(),
                hibernateStatistics.getSecondLevelCacheMissCount(),
                hibernateStatistics.getSecondLevelCachePutCount());
    }

    private void logDelta(ProceedingJoinPoint joinPoint, CacheSnapshot before) {
        CacheSnapshot after = takeSnapshot();
        long hitDelta = after.hits - before.hits;
        long missDelta = after.misses - before.misses;
        long putDelta = after.puts - before.puts;

        if (hitDelta == 0 && missDelta == 0 && putDelta == 0) {
            return;
        }

        String methodName = joinPoint.getSignature().toShortString();
        log.info("L2 cache [{}] hits +{}, misses +{}, puts +{} (totals H={}/M={}/P={})",
                methodName, hitDelta, missDelta, putDelta,
                after.hits, after.misses, after.puts);
    }

    public void enableLogging() {
        loggingProperties.setEnabled(true);
        hibernateStatistics.setStatisticsEnabled(true);
        log.info("Cache statistics logging ENABLED");
    }

    public void disableLogging() {
        loggingProperties.setEnabled(false);
        hibernateStatistics.setStatisticsEnabled(false);
        log.info("Cache statistics logging DISABLED");
    }

    public Statistics getStatistics() {
        return hibernateStatistics;
    }

    public boolean isLoggingEnabled() {
        return loggingProperties.isEnabled();
    }

    private record CacheSnapshot(long hits, long misses, long puts) {
    }
}
