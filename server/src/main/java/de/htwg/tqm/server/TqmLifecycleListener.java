package de.htwg.tqm.server;

import de.htwg.tqm.server.poll.PollService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

final class TqmLifecycleListener implements ContainerLifecycleListener {

    private final Collection<?> injectFieldsWithServiceLocator;
    private PollService pollService;

    /**
     * @param objectsToInject The fields of these objects will be injected by calling the service locators inject method
     *                        on each of them on container startup.
     */
    public TqmLifecycleListener(@NotNull Collection<?> objectsToInject) {
        this.injectFieldsWithServiceLocator = objectsToInject;
    }

    @Override
    public void onStartup(Container container) {
        ServiceLocator serviceLocator = container.getApplicationHandler().getServiceLocator();
        startPollServiceAndInjectObjects(serviceLocator);
    }

    void startPollServiceAndInjectObjects(ServiceLocator serviceLocator) {
        injectFieldsWithServiceLocator.forEach(serviceLocator::inject);
        pollService = serviceLocator.getService(PollService.class);
        pollService.start();
    }

    @Override
    public void onShutdown(Container container) {
        pollService.shutdown();
    }

    @Override
    public void onReload(Container container) {
    }

}
