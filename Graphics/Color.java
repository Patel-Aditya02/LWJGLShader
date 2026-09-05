package Engine.Graphics;

public class Color {

    public static final Color WHITE   = new Color(1.0f, 1.0f, 1.0f);
    public static final Color BLACK   = new Color(0.0f, 0.0f, 0.0f);
    public static final Color RED     = new Color(1.0f, 0.0f, 0.0f);
    public static final Color GREEN   = new Color(0.0f, 1.0f, 0.0f);
    public static final Color BLUE    = new Color(0.0f, 0.0f, 1.0f);
    public static final Color CYAN    = new Color(0.0f, 1.0f, 1.0f);
    public static final Color MAGENTA = new Color(1.0f, 0.0f, 1.0f);
    public static final Color YELLOW  = new Color(1.0f, 1.0f, 0.0f);
    public static final Color GRAY    = new Color(0.5f, 0.5f, 0.5f);
    public static final Color ORANGE  = new Color(1.0f, 0.65f, 0.0f);

    public final float red, green, blue, alpha;

    // Constructor with default alpha value of 1 (opaque)
    public Color(float red, float green, float blue) {
        this(red, green, blue, 1.0f);
    }

    // Constructor with alpha specified
    public Color(float red, float green, float blue, float alpha) {
        this.red = clamp(red);
        this.green = clamp(green);
        this.blue = clamp(blue);
        this.alpha = clamp(alpha);
    }

    // Clamps the color values between 0 and 1.
    private float clamp(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
}