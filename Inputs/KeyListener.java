package Engine.Inputs;

import Engine.Events.Event;
import Engine.Events.EventQueue;

import java.util.Arrays;

import static Engine.Events.EventType.KEY_PRESSED;
import static Engine.Events.EventType.KEY_RELEASED;
import static org.lwjgl.glfw.GLFW.*;

public class KeyListener {
    public static final boolean[] keyPressed = new boolean[GLFW_KEY_LAST + 1];
    public static final boolean[] keyBeginPress = new boolean[GLFW_KEY_LAST + 1];

    public static void endFrame() {
        Arrays.fill(keyBeginPress, false);
    }

    public static void keyCallback(long win, int key, int scancode, int action, int mods){
        if (key <= GLFW_KEY_LAST && key >= 0) {
            if (action == GLFW_PRESS) {
                EventQueue.pushEvent(new Event(KEY_PRESSED, key));
                KeyListener.keyPressed[key] = true;
                KeyListener.keyBeginPress[key] = true;

            } else if (action == GLFW_RELEASE) {
                EventQueue.pushEvent(new Event(KEY_RELEASED, key));
                KeyListener.keyPressed[key] = false;
                KeyListener.keyBeginPress[key] = false;
            }
        }
    }

    public static boolean isKeyPressed(int keyCode) {
        if (keyCode <= GLFW_KEY_LAST && keyCode >= 0) {
            return keyPressed[keyCode];
        }

        return false;
    }

    public static boolean keyBeginPress(int keyCode) {
        if (keyCode <= GLFW_KEY_LAST && keyCode >= 0) {
            return keyBeginPress[keyCode];
        }

        return false;
    }
}