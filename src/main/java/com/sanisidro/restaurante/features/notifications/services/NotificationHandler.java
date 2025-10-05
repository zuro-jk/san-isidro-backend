package com.sanisidro.restaurante.features.notifications.services;

import com.sanisidro.restaurante.features.notifications.dto.NotifiableEvent;

public interface NotificationHandler<T extends NotifiableEvent> {

    /**
     * Envía la notificación correspondiente al evento.
     * 
     * @param event evento a notificar
     */
    void send(T event);

}
