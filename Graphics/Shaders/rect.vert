#version 330 core
layout(location = 0) in vec2 aPos;
uniform vec2 uResolution;

void main() {
    // Convert pixel coordinates (0,0) - (uResolution.x, uResolution.y) to normalized device coordinates [-1,1]
    vec2 ndc = (aPos / uResolution) * 2.0 - 1.0;
    // Flip the Y-axis so (0,0) is at the bottom-left
    ndc.y = -ndc.y;
    gl_Position = vec4(ndc, 0.0, 1.0);
}