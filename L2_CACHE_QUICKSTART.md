# Быстрый старт: L2 JPA Cache с Ehcache

## Что было добавлено

### Файлы конфигурации:
- ✅ [`build.gradle`](space-marine-backend/build.gradle) - добавлены зависимости Ehcache и AOP
- ✅ [`application.properties`](space-marine-backend/src/main/resources/application.properties) - настройки L2 кэша
- ✅ [`ehcache.xml`](space-marine-backend/src/main/resources/ehcache.xml) - конфигурация Ehcache

### Новые классы:
- ✅ `CacheConfig.java` - конфигурация статистики
- ✅ `CacheStatisticsAspect.java` - AOP для логирования
- ✅ `CacheController.java` - REST API управления

### Обновленные классы:
- ✅ `SpaceMarine.java` - добавлена аннотация @Cache
- ✅ `Chapter.java` - добавлена аннотация @Cache
- ✅ `Coordinates.java` - добавлена аннотация @Cache
- ✅ `User.java` - добавлена аннотация @Cache

## Использование

### 1. Запуск приложения
```bash
cd space-marine-backend
./gradlew bootRun
```

По умолчанию логирование статистики **ВЫКЛЮЧЕНО**.

### 2. REST API для управления кэшем

#### Включить логирование статистики
```bash
curl -X POST http://localhost:8080/api/cache/statistics/enable
```

Ответ:
```json
{
  "enabled": true,
  "message": "Cache statistics logging enabled via AOP",
  "description": "Cache hits/misses will be logged for each repository operation"
}
```

#### Отключить логирование
```bash
curl -X POST http://localhost:8080/api/cache/statistics/disable
```

#### Получить текущую статистику
```bash
curl http://localhost:8080/api/cache/statistics
```

Ответ:
```json
{
  "enabled": true,
  "aopLoggingEnabled": true,
  "secondLevelCacheHitCount": 42,
  "secondLevelCacheMissCount": 15,
  "secondLevelCachePutCount": 15,
  "cacheHitRatio": "73.68%",
  "queryCacheHitCount": 5,
  "queryCacheMissCount": 2,
  "entityFetchCount": 57
}
```

#### Получить статистику по регионам
```bash
curl http://localhost:8080/api/cache/regions
```

Ответ показывает статистику для каждой сущности отдельно.

#### Очистить кэш
```bash
curl -X POST http://localhost:8080/api/cache/clear
```

### 3. Пример работы с логированием

**Шаг 1:** Включить логирование
```bash
curl -X POST http://localhost:8080/api/cache/statistics/enable
```

**Шаг 2:** Выполнить запрос первый раз (cache miss)
```bash
curl http://localhost:8080/api/marines/1
```

**Лог:**
```
L2 Cache Statistics for method: SpaceMarineRepository.findById
  Cache Hits: 0 | Cache Misses: 1 | Cache Puts: 1
```

**Шаг 3:** Выполнить тот же запрос второй раз (cache hit)
```bash
curl http://localhost:8080/api/marines/1
```

**Лог:**
```
L2 Cache Statistics for method: SpaceMarineRepository.findById
  Cache Hits: 1 | Cache Misses: 0 | Cache Puts: 0
  Hit Ratio for this operation: 100.00%
```

**Шаг 4:** Проверить общую статистику
```bash
curl http://localhost:8080/api/cache/statistics
```

### 4. Периодическое логирование

Каждые 60 секунд автоматически выводится общая статистика:
```
=== L2 Cache Statistics (Periodic Report) ===
Second Level Cache Hit Count: 42
Second Level Cache Miss Count: 15
Second Level Cache Put Count: 15
Cache Hit Ratio: 73.68%
Query Cache Hit Count: 5
Query Cache Miss Count: 2
============================================
```

## Конфигурация

### Изменение TTL/TTI

Редактируйте [`ehcache.xml`](space-marine-backend/src/main/resources/ehcache.xml):

```xml
<cache alias="ru.itmo.is.space_marine_backend.entity.SpaceMarine">
    <expiry>
        <ttl unit="minutes">15</ttl>  <!-- Время жизни -->
        <tti unit="minutes">10</tti>  <!-- Время простоя -->
    </expiry>
    <heap unit="entries">5000</heap>  <!-- Макс. количество -->
</cache>
```

### Изменение размера кэша

```xml
<heap unit="entries">10000</heap>  <!-- Увеличить до 10000 записей -->
```

### Отключение кэша для сущности

Удалите аннотацию `@Cache` из класса сущности:
```java
@Entity
// @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)  // Закомментировать
public class SpaceMarine { ... }
```

## Проверка работы кэша

### Тест 1: Проверка cache hits
```bash
# Включить логирование
curl -X POST http://localhost:8080/api/cache/statistics/enable

# Первый запрос - miss
curl http://localhost:8080/api/marines/1

# Второй запрос - hit
curl http://localhost:8080/api/marines/1

# Проверить статистику
curl http://localhost:8080/api/cache/statistics
# Ожидается: cacheHitRatio > 0%
```

### Тест 2: Проверка TTL
```bash
# Запрос
curl http://localhost:8080/api/marines/1

# Ждем 16 минут (больше чем TTL=15 минут)
sleep 960

# Повторный запрос - должен быть miss (запись истекла)
curl http://localhost:8080/api/marines/1
```

### Тест 3: Проверка очистки кэша
```bash
# Загрузить данные в кэш
curl http://localhost:8080/api/marines/1

# Очистить кэш
curl -X POST http://localhost:8080/api/cache/clear

# Повторный запрос - должен быть miss
curl http://localhost:8080/api/marines/1
```

## Мониторинг в Production

### Spring Boot Actuator (опционально)

Добавьте в `build.gradle`:
```gradle
implementation 'org.springframework.boot:spring-boot-starter-actuator'
```

Добавьте в `application.properties`:
```properties
management.endpoints.web.exposure.include=health,metrics,cache
management.endpoint.cache.enabled=true
```

Проверить метрики:
```bash
curl http://localhost:8080/actuator/metrics/cache.gets?tag=name:ru.itmo.is.space_marine_backend.entity.SpaceMarine
```

## Troubleshooting

### Проблема: Кэш не работает
**Решение:**
1. Проверьте наличие аннотации `@Cache` на entity
2. Проверьте `use_second_level_cache=true` в `application.properties`
3. Проверьте наличие `ehcache.xml` в classpath

### Проблема: Логирование не включается
**Решение:**
1. Проверьте `@EnableAspectJAutoProxy` или Spring Boot AOP
2. Проверьте зависимость `spring-boot-starter-aop` в `build.gradle`
3. Убедитесь, что вызов идет через Spring proxy (не direct method call)

### Проблема: OutOfMemoryError
**Решение:**
1. Уменьшите `heap entries` в `ehcache.xml`
2. Уменьшите TTL для освобождения памяти
3. Рассмотрите использование OFF_HEAP storage

### Проблема: Устаревшие данные в кэше
**Решение:**
1. Уменьшите TTL
2. Используйте `@CacheEvict` для ручной инвалидации
3. Вызовите `POST /api/cache/clear` после обновления данных

## Для отчета

Используйте файл [`L2_CACHE_REPORT.md`](L2_CACHE_REPORT.md) - там подробное описание:
- Параметров конфигурации
- Влияния на уровень хранения
- Архитектуры решения
- Примеров тестирования

## Полезные команды

```bash
# Сборка проекта
./gradlew build

# Запуск
./gradlew bootRun

# Включить логирование кэша
curl -X POST http://localhost:8080/api/cache/statistics/enable

# Статистика
curl http://localhost:8080/api/cache/statistics

# Очистить кэш
curl -X POST http://localhost:8080/api/cache/clear

# Логи приложения
tail -f logs/spring-boot-application.log
```
