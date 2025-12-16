package ru.itmo.is.space_marine_backend.dto.response;

/**
 * DTO для ответа на запрос статуса L2 кэша.
 *
 * @param l2CacheEnabled включен ли L2 кэш в Hibernate
 * @param loggingEnabled включено ли логирование статистики
 * @param hits           количество попаданий в кэш (null если логирование
 *                       выключено)
 * @param misses         количество промахов кэша (null если логирование
 *                       выключено)
 * @param puts           количество добавлений в кэш (null если логирование
 *                       выключено)
 */
public record CacheStatusResponse(
        boolean l2CacheEnabled,
        boolean loggingEnabled,
        Long hits,
        Long misses,
        Long puts) {
}
