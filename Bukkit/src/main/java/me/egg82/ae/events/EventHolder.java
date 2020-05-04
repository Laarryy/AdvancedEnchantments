package me.egg82.ae.events;

import java.util.ArrayList;
import java.util.List;
import me.egg82.ae.EnchantAPI;
import me.egg82.ae.services.entity.EntityItemHandler;
import ninja.egg82.events.BukkitEventSubscriber;
import ninja.egg82.service.ServiceLocator;
import ninja.egg82.service.ServiceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class EventHolder {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final List<BukkitEventSubscriber<?>> events = new ArrayList<>();

    protected final EnchantAPI api = EnchantAPI.getInstance();

    public final int numEvents() { return events.size(); }

    public final void cancel() {
        for (BukkitEventSubscriber<?> event : events) {
            event.cancel();
        }
    }

    protected EntityItemHandler getItemHandler() {
        EntityItemHandler retVal;
        try {
            retVal = ServiceLocator.get(EntityItemHandler.class);
        } catch (InstantiationException | IllegalAccessException | ServiceNotFoundException ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
        return retVal;
    }
}
