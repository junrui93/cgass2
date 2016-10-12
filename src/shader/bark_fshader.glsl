#version 120

varying vec3 fragPosition;
varying vec3 normal;
varying vec2 texCoord;

varying vec3 tanLightDirection;
varying vec3 tanViewDirection;
varying vec3 tanFragPosition;

varying vec3 tanLightDirection2;
varying vec3 tanSpotDirection;

uniform sampler2D textureMap;
uniform sampler2D normalMap;

void main() {
    // lightDirection and viewDirection are all in tangent space
    // normals are naturally in tangent space
    vec3 lightDirection = normalize(tanLightDirection);
    vec3 viewDirection = normalize(tanViewDirection);
    vec3 normalDirection = normalize(texture2D(normalMap, texCoord).rgb * 2.0 - 1.0); // map rgb to normal

    vec3 globalAmbient = vec3(gl_LightModel.ambient);

    // calculate sun (directional) lighting
    vec3 sunAmbient = vec3(gl_LightSource[0].ambient);

    float normalDotLight = dot(normalDirection, lightDirection);

    vec3 sunDiffuse = vec3(gl_LightSource[0].diffuse) * max(0.0, normalDotLight);

    vec3 sunSpecular;
    if (normalDotLight < 0.0) {
        // light source on the wrong side?
        // no specular reflection
        sunSpecular = vec3(0.0, 0.0, 0.0);
    } else {
        // light source on the right side
        sunSpecular = vec3(gl_LightSource[0].specular)
            // uses blinn-phong (half vector) instead of phong shading
            //* pow(max(0.0, dot(reflect(-lightDirection, normalDirection), viewDirection)),
            * pow(max(0.0, dot(normalDirection, normalize(lightDirection + viewDirection))),
            gl_FrontMaterial.shininess);
    }

    // calculate torch (spot light) lighting
    vec3 lightDirection2 = normalize(tanLightDirection2);
    vec3 spotDirection = normalize(tanSpotDirection);

    float distance = length(tanLightDirection2);
    float attenuation = 1.0 / (1.0 + 1.0 * distance); // linear attenuation

    if (gl_LightSource[1].spotCutoff <= 90.0) {
        // spotlight?
        float clampedCosine = max(0.0, dot(-lightDirection2, spotDirection));
        if (clampedCosine < gl_LightSource[1].spotCosCutoff) {
            // outside of spotlight cone?
            attenuation = 0.0;
        } else {
            attenuation = attenuation * pow(clampedCosine, gl_LightSource[1].spotExponent);
        }
    }

    vec3 spotAmbient = vec3(gl_LightSource[1].ambient);

    float normalDotLight2 = dot(normalDirection, lightDirection2);

    vec3 spotDiffuse = vec3(gl_LightSource[1].diffuse) * max(0.0, normalDotLight2);

    vec3 spotSpecular;
    if (normalDotLight2 < 0.0) {
        // light source on the wrong side?
        // no specular reflection
        spotSpecular = vec3(0.0, 0.0, 0.0);
    } else {
        // light source on the right side
        spotSpecular = vec3(gl_LightSource[1].specular)
            * pow(max(0.0, dot(normalDirection, normalize(lightDirection2 + viewDirection))),
            gl_FrontMaterial.shininess);
    }

    // add up the two lights
    vec3 diffuseColor = vec3(gl_FrontMaterial.ambient) * (globalAmbient + sunAmbient + attenuation * spotAmbient)
        + vec3(gl_FrontMaterial.diffuse) * (sunDiffuse + attenuation * spotDiffuse);
    vec3 specularColor = vec3(gl_FrontMaterial.specular) * (sunSpecular + attenuation * spotSpecular);

    // seperate specular color
    gl_FragColor = vec4(diffuseColor
        * vec3(texture2D(textureMap, texCoord))
        + specularColor, 1.0);
}