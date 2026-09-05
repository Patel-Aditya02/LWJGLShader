package Engine.Events;

import static Engine.Events.EventType.*;

public class Event {
    public final EventType type;
    private final int keyCode;

    public Event(EventType type, int keyCode) {
        this.type = type;
        this.keyCode = keyCode;
    }

    public int getKeyCode(){
        return (this.type == KEY_PRESSED || this.type == KEY_RELEASED) ? keyCode : -1;
    }
}