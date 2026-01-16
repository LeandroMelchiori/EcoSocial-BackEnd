package com.alura.foro.hub.api.helpers;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

@Component
public class MetricsHelper {

    private final MeterRegistry meterRegistry;

    public MetricsHelper(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    // ---------- Counters ----------
    public void inc(String name, String accion, String resultado) {
        meterRegistry.counter(name,
                "accion", accion,
                "resultado", resultado
        ).increment();
    }

    public void incError(String name, String accion, String error) {
        meterRegistry.counter(name,
                "accion", accion,
                "resultado", "error",
                "error", error
        ).increment();
    }

    // ---------- Timers ----------
    public <T> T time(String name, String accion, Callable<T> callable) {
        Timer timer = Timer.builder(name)
                .publishPercentileHistogram()
                .tag("accion", accion)
                .register(meterRegistry);

        try {
            return timer.recordCallable(callable);
        } catch (RuntimeException e) {
            // ✅ NO romper Forbidden/BadRequest/etc.
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void timeVoid(String name, String accion, Runnable runnable) {
        Timer timer = Timer.builder(name)
                .publishPercentileHistogram()
                .tag("accion", accion)
                .register(meterRegistry);

        try {
            timer.record(runnable);
        } catch (RuntimeException e) {
            throw e;
        }
    }
}
