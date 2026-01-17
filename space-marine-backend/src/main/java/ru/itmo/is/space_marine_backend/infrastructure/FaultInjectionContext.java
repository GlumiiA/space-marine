package ru.itmo.is.space_marine_backend.infrastructure;

/**
 * Контекст для передачи информации о fault injection через ThreadLocal.
 * Позволяет тестировать сценарии отказов без смешивания с бизнес-логикой API.
 */
public class FaultInjectionContext {
    private static final ThreadLocal<FaultInjectionConfig> context = new ThreadLocal<>();

    public static void set(FaultInjectionConfig config) {
        context.set(config);
    }

    public static FaultInjectionConfig get() {
        return context.get();
    }

    public static void clear() {
        context.remove();
    }

    public static boolean isFailAfterFile() {
        FaultInjectionConfig config = context.get();
        return config != null && config.isFailAfterFile();
    }

    public static class FaultInjectionConfig {
        private final boolean failAfterFile;

        public FaultInjectionConfig(boolean failAfterFile) {
            this.failAfterFile = failAfterFile;
        }

        public boolean isFailAfterFile() {
            return failAfterFile;
        }
    }
}
