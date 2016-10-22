#version 120

varying vec3 position;
varying vec3 normal;
varying vec2 texCoords;

void main() {
    position = vec3(gl_ModelViewMatrix * gl_Vertex);
    normal = normalize(gl_NormalMatrix * gl_Normal);
    texCoords = vec2(gl_MultiTexCoord0);

    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
}