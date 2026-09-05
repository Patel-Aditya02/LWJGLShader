package Engine.Graphics;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

public class Texture {
    public final BufferedImage image;
    private int textureID = -1;

    public Texture(BufferedImage image) {
        this.image = image;
    }

    public Texture(String path) throws IOException {
        try (InputStream in = Texture.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new RuntimeException("Failed to load image: " + path);
            }
            this.image = ImageIO.read(in);
        } catch (IOException e) {
            throw new RuntimeException("Error reading image: " + path, e);
        }
    }

    public int getID() {
        return textureID;
    }

    public void uploadTexture() {
        // Flip the image vertically if needed (or use STBImage for more flexibility)
        int width = image.getWidth();
        int height = image.getHeight();

        // Convert BufferedImage to a byte array in RGBA format
        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
        // Convert ARGB (default BufferedImage type) to RGBA
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = pixels[y * width + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red
                buffer.put((byte) ((pixel >> 8) & 0xFF));  // Green
                buffer.put((byte) (pixel & 0xFF));         // Blue
                buffer.put((byte) ((pixel >> 24) & 0xFF));   // Alpha
            }
        }
        buffer.flip();

        // Generate a new texture ID
        int texID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texID);

        // Setup texture parameters
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        // Upload the texture data
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

        glBindTexture(GL_TEXTURE_2D, 0);
        this.textureID = texID;
    }
}