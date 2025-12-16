package ru.itmo.is.space_marine_backend.cache;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
@Component
@ConfigurationProperties(prefix = "app.l2cache")
public class CacheLoggingProperties {
    private boolean enabled = false;
}
