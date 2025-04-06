package lyfjshs.gomis.utils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple event bus implementation for the GOMIS application.
 * Allows components to publish events and subscribe to events without direct coupling.
 */
public class EventBus {
    private static final Logger logger = Logger.getLogger(EventBus.class.getName());
    private static final Map<String, Set<Consumer<Object>>> subscribers = new ConcurrentHashMap<>();
    private static final Map<String, Map<Object, Set<Consumer<Object>>>> subscribersByOwner = new ConcurrentHashMap<>();
    
    /**
     * Subscribe to a specific event type.
     * 
     * @param eventType The event type to subscribe to
     * @param handler The handler to be called when the event is published
     */
    public static void subscribe(String eventType, Consumer<Object> handler) {
        subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArraySet<>()).add(handler);
        logger.fine("Subscribed to event: " + eventType);
    }
    
    /**
     * Subscribe to a specific event type with an owner object.
     * This makes it easier to unsubscribe all listeners for a specific object.
     * 
     * @param owner The owner object
     * @param eventType The event type to subscribe to
     * @param handler The handler to be called when the event is published
     */
    public static void subscribe(Object owner, String eventType, Consumer<Object> handler) {
        // Add to regular subscribers
        subscribe(eventType, handler);
        
        // Also track by owner
        subscribersByOwner
            .computeIfAbsent(eventType, k -> new ConcurrentHashMap<>())
            .computeIfAbsent(owner, k -> new CopyOnWriteArraySet<>())
            .add(handler);
        
        logger.fine("Subscribed to event: " + eventType + " for owner: " + owner);
    }
    
    /**
     * Unsubscribe from a specific event type.
     * 
     * @param eventType The event type to unsubscribe from
     * @param handler The handler to be removed
     */
    public static void unsubscribe(String eventType, Consumer<Object> handler) {
        if (subscribers.containsKey(eventType)) {
            subscribers.get(eventType).remove(handler);
            logger.fine("Unsubscribed from event: " + eventType);
        }
    }
    
    /**
     * Unsubscribe all handlers for a specific owner and event type.
     * 
     * @param owner The owner object
     * @param eventType The event type to unsubscribe from
     */
    public static void unsubscribeAll(Object owner, String eventType) {
        if (subscribersByOwner.containsKey(eventType) && 
            subscribersByOwner.get(eventType).containsKey(owner)) {
            
            // Get all handlers for this owner and event type
            Set<Consumer<Object>> handlers = subscribersByOwner.get(eventType).get(owner);
            
            // Remove each handler from the main subscribers collection
            if (subscribers.containsKey(eventType)) {
                for (Consumer<Object> handler : handlers) {
                    subscribers.get(eventType).remove(handler);
                }
            }
            
            // Remove the owner's entry
            subscribersByOwner.get(eventType).remove(owner);
            if (subscribersByOwner.get(eventType).isEmpty()) {
                subscribersByOwner.remove(eventType);
            }
            
            logger.fine("Unsubscribed all handlers for event: " + eventType + " and owner: " + owner);
        }
    }
    
    /**
     * Unsubscribe all handlers for a specific owner
     * 
     * @param owner The owner object
     */
    public static void unsubscribeAll(Object owner) {
        // Create a copy of the keys to avoid concurrent modification
        for (String eventType : subscribersByOwner.keySet().toArray(new String[0])) {
            unsubscribeAll(owner, eventType);
        }
        logger.fine("Unsubscribed all handlers for owner: " + owner);
    }
    
    /**
     * Publish an event to all subscribers of that event type.
     * 
     * @param eventType The type of event to publish
     * @param data The data associated with the event
     */
    public static void publish(String eventType, Object data) {
        if (subscribers.containsKey(eventType)) {
            logger.fine("Publishing event: " + eventType + " to " + subscribers.get(eventType).size() + " subscribers");
            for (Consumer<Object> handler : subscribers.get(eventType)) {
                try {
                    handler.accept(data);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error in event handler for event type: " + eventType, e);
                }
            }
        }
    }
    
    /**
     * Clear all subscribers for a specific event type.
     * 
     * @param eventType The event type to clear subscribers for
     */
    public static void clearSubscribers(String eventType) {
        subscribers.remove(eventType);
        subscribersByOwner.remove(eventType);
        logger.fine("Cleared all subscribers for event: " + eventType);
    }
    
    /**
     * Clear all subscribers for all event types.
     */
    public static void clearAllSubscribers() {
        subscribers.clear();
        subscribersByOwner.clear();
        logger.fine("Cleared all subscribers");
    }
} 