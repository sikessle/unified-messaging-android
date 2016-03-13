package de.htwg.tqm.server;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;
import org.jetbrains.annotations.NotNull;

final class TqmResourceConfig extends ResourceConfig {

    private static final String RESOURCE_PACKAGE = "de.htwg.tqm.server.resource";

    public TqmResourceConfig(@NotNull AbstractBinder binder, @NotNull ContainerLifecycleListener lifecycleListener) {
        packages(RESOURCE_PACKAGE);
        register(JacksonFeature.class);
        register(binder);
        register(lifecycleListener);
    }
}
