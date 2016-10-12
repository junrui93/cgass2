#version 120

varying vec3 fragPosition;
varying vec3 normal;
varying vec2 texCoord;

varying vec3 tanLightDirection;
varying vec3 tanViewDirection;
varying vec3 tanFragPosition;

varying vec3 tanLightDirection2;
varying vec3 tanSpotDirection;

attribute vec3 tangent;
attribute vec3 bitangent;

void main() {
    fragPosition = vec3(gl_ModelViewMatrix * gl_Vertex);
    normal = normalize(gl_NormalMatrix * gl_Normal);
    texCoord = vec2(gl_MultiTexCoord0);

    //vec3 T = normalize(gl_NormalMatrix * tangent);
    //vec3 N = normal;
    //vec3 B = cross(T, N);

    // T can be calculated from B and N because they are perpendicular
    vec3 B = normalize(gl_NormalMatrix * bitangent);
    vec3 N = normal;
    vec3 T = cross(B, N);

    // actually inverse the matrix
    // transpose here is equal to inverse (because T,B,N are perpendicular to each other)
    mat3 TBN = transpose(mat3(T, B, N));

    // transform the directions from camera space to tangent space to implement normal mapping
    tanLightDirection = TBN * vec3(gl_LightSource[0].position);
    tanViewDirection = TBN * -vec3(fragPosition);
    tanFragPosition = TBN * fragPosition;

    tanLightDirection2 = TBN * (gl_LightSource[1].position.xyz - fragPosition);
    tanSpotDirection = TBN * gl_LightSource[1].spotDirection;

    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
}
