package com.sanisidro.restaurante.features.notifications.registry;

import org.reflections.Reflections;
import org.springframework.stereotype.Component;
import com.sanisidro.restaurante.features.notifications.dto.NotifiableEvent;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class EventTypeRegistry {

    private final Map<String, Class<? extends NotifiableEvent>> eventTypeMap = new HashMap<>();

    @PostConstruct
    public void init() {
        // Buscar todas las clases que implementan NotifiableEvent en el paquete "dto"
        Reflections reflections = new Reflections("com.sanisidro.restaurante.features.notifications.dto");
        Set<Class<? extends NotifiableEvent>> eventClasses = reflections.getSubTypesOf(NotifiableEvent.class);

        for (Class<? extends NotifiableEvent> clazz : eventClasses) {
            eventTypeMap.put(clazz.getSimpleName(), clazz);
        }
    }

    public Class<? extends NotifiableEvent> getEventClass(String eventType) {
        return eventTypeMap.get(eventType);
    }

}
