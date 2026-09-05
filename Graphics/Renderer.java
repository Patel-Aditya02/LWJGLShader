package Engine.Graphics;

import Engine.Window;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static Engine.Graphics.Color.*;

public class Renderer {
    private static int vao, vbo;
    private static int imageVAO, imageVBO;
    private static int lineShaderProgram;
    private static int rectShaderProgram;
    private static int imageShaderProgram;
    private static Color color = WHITE;
    private static float thickness;

    public static void init() {
        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        glEnable(GL_MULTISAMPLE);
        // Create shader program from files in the Shaders package.
        lineShaderProgram = createShaderProgram("Engine/Graphics/Shaders/line.vert",
                "Engine/Graphics/Shaders/line.frag");
        rectShaderProgram = createShaderProgram("Engine/Graphics/Shaders/rect.vert",
                "Engine/Graphics/Shaders/rect.frag");
        // Setup VAO and VBO for drawing a line (2 vertices with 2 components each)
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        // Allocate enough space for 4 floats (2 vertices)
        glBufferData(GL_ARRAY_BUFFER, 200 * Float.BYTES, GL_DYNAMIC_DRAW);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public static void initImageRenderer() {
        // Load the image shader program
        imageShaderProgram = createShaderProgram("Engine/Graphics/Shaders/image.vert",
                "Engine/Graphics/Shaders/image.frag");

        // Generate VAO and VBO for the image quad
        imageVAO = glGenVertexArrays();
        imageVBO = glGenBuffers();

        glBindVertexArray(imageVAO);
        glBindBuffer(GL_ARRAY_BUFFER, imageVBO);
        // Allocate space for 4 vertices, each with 4 floats (2 for position, 2 for texture coords)
        glBufferData(GL_ARRAY_BUFFER, 4 * 4 * Float.BYTES, GL_DYNAMIC_DRAW);

        // Set attribute pointers:
        // Position attribute (location 0)
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        // Texture coordinate attribute (location 1)
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private static String loadShaderSource(String path) {
        try (InputStream in = Renderer.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new RuntimeException("Failed to load shader file: " + path);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading shader file: " + path, e);
        }
    }
    private static int createShaderProgram(String vertexPath, String fragmentPath) {
        String vertexSource = loadShaderSource(vertexPath);
        String fragmentSource = loadShaderSource(fragmentPath);

        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexSource);
        glCompileShader(vertexShader);
        // Check for compile errors.
        if (glGetShaderi(vertexShader, GL_COMPILE_STATUS) == GL_FALSE) {
            System.err.println("Vertex shader compilation failed:\n" + glGetShaderInfoLog(vertexShader));
        }

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentSource);
        glCompileShader(fragmentShader);
        // Check for compile errors.
        if (glGetShaderi(fragmentShader, GL_COMPILE_STATUS) == GL_FALSE) {
            System.err.println("Fragment shader compilation failed:\n" + glGetShaderInfoLog(fragmentShader));
        }

        int program = glCreateProgram();
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
        glLinkProgram(program);
        // Check for linking errors.
        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            System.err.println("Shader program linking failed:\n" + glGetProgramInfoLog(program));
        }

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

        return program;
    }
    public static void beginDrawing() {
        glClear(GL_COLOR_BUFFER_BIT); // Clear the screen
        setColor(BLACK);
        setThickness(0.5f);
    }
    public static void endDrawing() {
        GLFW.glfwSwapBuffers(Window.getID());
    }
    public static void drawLine(float x1, float y1, float x2, float y2) {
        // Activate our shader program
        glUseProgram(lineShaderProgram);

        // Set the uResolution uniform using the current window dimensions
        int resolutionLoc = glGetUniformLocation(lineShaderProgram, "uResolution");
        glUniform2f(resolutionLoc, Window.getWidth(), Window.getHeight());

        glLineWidth(thickness);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer vertexBuffer = stack.mallocFloat(4);
            vertexBuffer.put(x1).put(y1).put(x2).put(y2).flip();

            glBindVertexArray(vao);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer);

            glDrawArrays(GL_LINES, 0, 2);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        }

        glUseProgram(0);
    }
    public static void drawRect(float x, float y, float width, float height) {
        drawLine(x, y, x + width, y); // Top
        drawLine(x + width, y, x + width, y + height); // Right
        drawLine(x + width, y + height, x, y + height); // Bottom
        drawLine(x, y + height, x, y); // Left
    }
    public static void fillRect(float x, float y, float width, float height) {
        glUseProgram(rectShaderProgram);

        int resolutionLoc = glGetUniformLocation(rectShaderProgram, "uResolution");
        glUniform2f(resolutionLoc, Window.getWidth(), Window.getHeight());

        int colorLoc = glGetUniformLocation(rectShaderProgram, "uColor");
        glUniform4f(colorLoc, color.red, color.green, color.blue, color.alpha);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer vertexBuffer = stack.mallocFloat(8);
            vertexBuffer.put(x).put(y)
                    .put(x + width).put(y)
                    .put(x + width).put(y + height)
                    .put(x).put(y + height)
                    .flip();

            glBindVertexArray(vao);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer);
            glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        }

        glUseProgram(0);
    }
    public static void drawSymmetricPolygon(float centerX, float centerY, float radius, int sides){
        // Use the line shader for the outline.
        glUseProgram(lineShaderProgram);

        // Set the resolution uniform
        int resolutionLoc = glGetUniformLocation(lineShaderProgram, "uResolution");
        glUniform2f(resolutionLoc, Window.getWidth(), Window.getHeight());

        // Set line thickness if needed
        glLineWidth(thickness);

        // Allocate a buffer for all vertices (each vertex has 2 floats)
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer vertexBuffer = stack.mallocFloat(sides * 2);
            for (int i = 0; i < sides; i++) {
                double angle = 2.0 * Math.PI * i / sides;
                float x = centerX + (float)(radius * Math.cos(angle));
                float y = centerY + (float)(radius * Math.sin(angle));
                vertexBuffer.put(x).put(y);
            }
            vertexBuffer.flip();

            glBindVertexArray(vao);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            // Update the vertex buffer data. (Ensure VBO is large enough!)
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer);
            glDrawArrays(GL_LINE_LOOP, 0, sides);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        }

        glUseProgram(0);
    }
    public static void fillSymmetricPolygon(float centerX, float centerY, float radius, int sides){
        // Use the rectangle (fill) shader for the filled circle.
        glUseProgram(rectShaderProgram);

        // Set the resolution uniform
        int resolutionLoc = glGetUniformLocation(rectShaderProgram, "uResolution");
        glUniform2f(resolutionLoc, Window.getWidth(), Window.getHeight());

        // Set the color uniform
        int colorLoc = glGetUniformLocation(rectShaderProgram, "uColor");
        glUniform4f(colorLoc, color.red, color.green, color.blue, color.alpha);

        // For a triangle fan, we need center plus each circle vertex plus repeat first vertex at the end.
        int vertexCount = sides + 2;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer vertexBuffer = stack.mallocFloat(vertexCount * 2);
            // Center vertex
            vertexBuffer.put(centerX).put(centerY);

            // Circle vertices
            for (int i = 0; i <= sides; i++) {
                double angle = 2.0 * Math.PI * i / sides;
                float x = centerX + (float)(radius * Math.cos(angle));
                float y = centerY + (float)(radius * Math.sin(angle));
                vertexBuffer.put(x).put(y);
            }
            vertexBuffer.flip();

            glBindVertexArray(vao);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            // Ensure that the buffer is large enough to hold these vertices.
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer);
            glDrawArrays(GL_TRIANGLE_FAN, 0, vertexCount);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        }

        glUseProgram(0);
    }
    public static void drawCircle(float centerX, float centerY, float radius) {
        drawSymmetricPolygon(centerX, centerY, radius, getCircleSegments(radius));
    }
    public static void fillCircle(float centerX, float centerY, float radius) {
        fillSymmetricPolygon(centerX, centerY, radius, getCircleSegments(radius));
    }
    private static int getCircleSegments(float radius) {
        // For example, start with 16 segments and add extra segments based on radius,
        // clamping the maximum to 64.
        int segments = (int) (16 + (radius / 50.0f) * 8); // For radius=50, segments=24; radius=100, segments=32; etc.
        segments = Math.max(16, segments);
        segments = Math.min(64, segments);
        return segments;
    }

    public static void drawPolygon(float[] x, float[] y) {
        if (x.length != y.length) {
            throw new IllegalArgumentException("x and y arrays must be of the same length");
        }
        int vertexCount = x.length;

        // Use the line shader for polygon outline drawing
        glUseProgram(lineShaderProgram);

        // Set resolution uniform so that the shader correctly transforms pixel coordinates to NDC
        int resolutionLoc = glGetUniformLocation(lineShaderProgram, "uResolution");
        glUniform2f(resolutionLoc, Window.getWidth(), Window.getHeight());

        // Set line width based on your current thickness setting
        glLineWidth(thickness);

        // Prepare the vertex buffer with the polygon vertices
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer vertexBuffer = stack.mallocFloat(vertexCount * 2);
            for (int i = 0; i < vertexCount; i++) {
                vertexBuffer.put(x[i]).put(y[i]);
            }
            vertexBuffer.flip();

            // Bind VAO and VBO and update the buffer data
            glBindVertexArray(vao);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer);

            // Draw the polygon outline as a loop
            glDrawArrays(GL_LINE_LOOP, 0, vertexCount);

            // Unbind
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        }

        glUseProgram(0);
    }
    public static void fillPolygon(float[] x, float[] y) {
        if (x.length != y.length) {
            throw new IllegalArgumentException("x and y arrays must have the same length");
        }
        int vertexCount = x.length;

        // Activate the fill shader program.
        glUseProgram(rectShaderProgram);

        // Set the resolution uniform
        int resolutionLoc = glGetUniformLocation(rectShaderProgram, "uResolution");
        glUniform2f(resolutionLoc, Window.getWidth(), Window.getHeight());

        // Set the fill color uniform
        int colorLoc = glGetUniformLocation(rectShaderProgram, "uColor");
        glUniform4f(colorLoc, color.red, color.green, color.blue, color.alpha);

        // Prepare the vertex data from the provided arrays.
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer vertexBuffer = stack.mallocFloat(vertexCount * 2);
            for (int i = 0; i < vertexCount; i++) {
                vertexBuffer.put(x[i]).put(y[i]);
            }
            vertexBuffer.flip();

            glBindVertexArray(vao);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            // Make sure your VBO has enough space (e.g. allocated in init() with glBufferData) for these vertices.
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer);
            glDrawArrays(GL_TRIANGLE_FAN, 0, vertexCount);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        }

        glUseProgram(0);
    }

    public static void drawImage(int textureID, float x, float y, float width, float height) {
        glUseProgram(imageShaderProgram);

        // Set the resolution uniform
        int resolutionLoc = glGetUniformLocation(imageShaderProgram, "uResolution");
        glUniform2f(resolutionLoc, Window.getWidth(), Window.getHeight());

        // Activate texture unit 0 and bind the texture
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureID);
        int texLoc = glGetUniformLocation(imageShaderProgram, "uTexture");
        glUniform1i(texLoc, 0);

        // Create the quad vertices with positions and texture coordinates:
        // Define vertices in order: bottom-left, bottom-right, top-right, top-left.
        float[] vertices = {
                // Position (x, y)        // Tex Coords (s, t)
                x,         y,           0.0f, 0.0f, // bottom-left
                x + width, y,           1.0f, 0.0f, // bottom-right
                x + width, y + height,  1.0f, 1.0f, // top-right
                x,         y + height,  0.0f, 1.0f  // top-left
        };

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer vertexBuffer = stack.mallocFloat(vertices.length);
            vertexBuffer.put(vertices).flip();

            glBindVertexArray(imageVAO);
            glBindBuffer(GL_ARRAY_BUFFER, imageVBO);
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer);

            glDrawArrays(GL_TRIANGLE_FAN, 0, 4);

            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        }

        // Unbind texture and shader
        glBindTexture(GL_TEXTURE_2D, 0);
        glUseProgram(0);
    }
    public static void drawPixel(float x, float y) {
        // Activate the shader program (using the line shader)
        glUseProgram(lineShaderProgram);

        // Set the resolution uniform so the vertex shader converts pixel coordinates to NDC correctly
        int resolutionLoc = glGetUniformLocation(lineShaderProgram, "uResolution");
        glUniform2f(resolutionLoc, Window.getWidth(), Window.getHeight());

        // Set the point size (adjust as needed)
        glPointSize(1.0f);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Allocate buffer for a single vertex (x, y)
            FloatBuffer vertexBuffer = stack.mallocFloat(2);
            vertexBuffer.put(x).put(y).flip();

            glBindVertexArray(vao);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer);
            glDrawArrays(GL_POINTS, 0, 1);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        }

        glUseProgram(0);
    }
    public static void setColor(Color color) {
        Renderer.color = color;
        glUseProgram(lineShaderProgram);
        int colorLoc = glGetUniformLocation(lineShaderProgram, "uColor");
        if (colorLoc != -1) {
            glUniform4f(colorLoc, color.red, color.green, color.blue, color.alpha);
        } else {
            System.err.println("Warning: Failed to get uniform location for 'uColor'.");
        }
        glUseProgram(0);
    }
    public static Color getColor(){
        return color;
    }
    public static void setThickness(float thickness){
        Renderer.thickness = Math.max(0.125f, Math.min(thickness, 1.0f));
    }
    public static float getThickness(){
        return thickness;
    }
    public static void setBackground(Color color){
        glClearColor(color.red, color.green, color.blue, color.alpha);
    }
    public static void setBackground(int textureID){
        Renderer.drawImage(textureID, 0, 0, Window.getWidth(), Window.getHeight());
    }
    public static void dispose() {
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
        glDeleteProgram(lineShaderProgram);
        glDeleteProgram(rectShaderProgram);
    }
}