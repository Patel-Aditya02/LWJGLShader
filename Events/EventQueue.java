package Engine.Events;

import java.util.ArrayDeque;
import java.util.Queue;

public class EventQueue {
    private static final Queue<Event> events = new ArrayDeque<>();

    public static void pushEvent(Event event) {
        events.offer(event);
    }

    public static Event pollEvent() {
        return events.poll();
    }

    public static boolean hasEvents() {
        return !events.isEmpty();
    }
}