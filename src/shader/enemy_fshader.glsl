#version 120

varying vec3 diffuseColor;
varying vec3 specularColor;
varying vec2 texCoords;

uniform sampler2D texUnit;

void main() {
    gl_FragColor = vec4(diffuseColor
        * vec3(texture2D(texUnit, texCoords))
        + specularColor, 1.0);
}