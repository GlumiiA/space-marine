package ru.itmo.is.space_marine_backend.dto.response;

/**
 * DTO для ответа на операции управления кэшем (enable/disable/clear).
 *
 * @param success успешность операции
 * @param message сообщение о результате операции
 */
public record CacheOperationResponse(
        boolean success,
        String message) {
}
