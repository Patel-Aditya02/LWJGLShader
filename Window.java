package Engine;

import Engine.Events.Event;
import Engine.Events.EventQueue;
import Engine.Graphics.Renderer;
import Engine.Inputs.KeyListener;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

import javax.xml.crypto.dsig.spec.XSLTTransformParameterSpec;

import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.glfw.GLFW.*;
import static Engine.Events.EventType.*;
import static Engine.WindowUtils.*;

public class Window {
    private static int width, height;
    private static int fbWidth, fbHeight;
    private static String title;
    private static long id;

    public static void init(int width, int height, String title, int fps, boolean resizeable){
        if(id != 0) return;
        Window.width = width;
        Window.height = height;
        Window.title = title;

        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW!");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW_FALSE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_SAMPLES, 4); // Request a 4x multisampled context
        if(!resizeable){
            glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        }

        if(width == 0 && height == 0){
            Window.width = VID_MODE.width();
            Window.height = VID_MODE.height();
        }

        Window.id = GLFW.glfwCreateWindow(Window.width, Window.height, Window.title, NULL, NULL);
        if (id == NULL) {
            throw new RuntimeException("Failed to create GLFW window!");
        }

        int[] fbWidthArr = new int[1];
        int[] fbHeightArr = new int[1];
        glfwGetFramebufferSize(id, fbWidthArr, fbHeightArr);
        Window.fbWidth = fbWidthArr[0];
        Window.fbHeight = fbHeightArr[0];

        glfwSetWindowSizeCallback(id, (win, newWidth, newHeight) -> {
            Window.width = newWidth;
            Window.height = newHeight;
        });
        glfwSetFramebufferSizeCallback(id, (win, newFbWidth, newFbHeight) -> {
            Window.fbWidth = newFbWidth;
            Window.fbHeight = newFbHeight;
            updateViewport();
        });

        glfwSetKeyCallback(id, KeyListener::keyCallback);

        glfwSetWindowCloseCallback(id, (win) -> {
            glfwSetWindowShouldClose(id, false);
            EventQueue.pushEvent(new Event(QUIT, -1));
        });

        glfwMakeContextCurrent(id);
        glfwSwapInterval(VID_MODE.refreshRate() / fps); // enable v-sync
        createCapabilities();

        updateViewport();

        Renderer.init();
        Renderer.initImageRenderer();
    }

    public static int getWidth(){
        return width;
    }

    public static int getHeight() {
        return height;
    }

    public static void updateViewport() {
        glViewport(0, 0, width, height);
    }

    public static void show(){
        glfwShowWindow(id);
    }
    public static void hide(){
        glfwHideWindow(id);
    }

    public static boolean shouldClose(){
        return glfwWindowShouldClose(id);
    }

    /**
     * Frees the allocated OpenGL resources
     * It is optional to call the function, but it is recommended to call it at the end of the program
     */
    public static void terminate(){
        Renderer.dispose();
        glfwSetErrorCallback(null).free();
        glfwSetKeyCallback(id, null).free();
        glfwDestroyWindow(Window.id);
        glfwTerminate();
    }

    public static void pollEvents(){
        glfwPollEvents();
    }

    public static long getID() {
        return id;
    }

    public static String getTitle() {
        return title;
    }

    public static void setTitle(String title) {
        Window.title = title;
        glfwSetWindowTitle(id, title);
    }
}