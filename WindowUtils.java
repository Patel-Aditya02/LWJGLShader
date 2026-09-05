package Engine;

import org.lwjgl.glfw.GLFWVidMode;
import static org.lwjgl.glfw.GLFW.*;

public class WindowUtils {

    public static final long MONITOR = glfwGetPrimaryMonitor();
    public static final GLFWVidMode VID_MODE = glfwGetVideoMode(MONITOR);
}