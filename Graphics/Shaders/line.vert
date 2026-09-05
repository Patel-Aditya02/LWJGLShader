#version 330 core
layout (location = 0) in vec2 aPos;
uniform vec2 uResolution;
void main() {
    // Convert from screen coordinates (0,0)-(uResolution.x,uResolution.y) to NDC (-1,+1)
    vec2 normalized = aPos / uResolution * 2.0 - 1.0;
    // Flip Y if necessary (depends on your coordinate convention)
    gl_Position = vec4(normalized.x, -normalized.y, 0.0, 1.0);
}