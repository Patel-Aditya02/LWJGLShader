#version 330 core
layout(location = 0) in vec2 aPos;
layout(location = 1) in vec2 aTexCoord;

uniform vec2 uResolution;

out vec2 TexCoord;

void main() {
    // Convert from pixel coordinates to NDC
    vec2 ndc = (aPos / uResolution) * 2.0 - 1.0;
    ndc.y = -ndc.y; // Flip Y-axis if needed
    gl_Position = vec4(ndc, 0.0, 1.0);
    TexCoord = aTexCoord;
}