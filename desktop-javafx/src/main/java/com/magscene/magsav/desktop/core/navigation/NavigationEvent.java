package com.magscene.magsav.desktop.core.navigation;

import javafx.scene.layout.Pane;

/**
 * Événement de navigation émis lors des changements de vue
 */
public class NavigationEvent {
    private final Route route;
    private final Pane view;
    private final long timestamp;
    
    public NavigationEvent(Route route, Pane view) {
        this.route = route;
        this.view = view;
        this.timestamp = System.currentTimeMillis();
    }
    
    public Route getRoute() {
        return route;
    }
    
    public Pane getView() {
        return view;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        return "NavigationEvent{" +
                "route=" + route +
                ", timestamp=" + timestamp +
                '}';
    }
}