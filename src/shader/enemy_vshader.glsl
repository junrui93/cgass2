#version 120

varying vec3 diffuseColor;
varying vec3 specularColor;
varying vec2 texCoords;

void main() {
    vec3 normalDirection = normalize(gl_NormalMatrix * gl_Normal);
    vec3 viewDirection = -normalize(vec3(gl_ModelViewMatrix * gl_Vertex));

    vec3 ambientColor = vec3(0.0, 0.0, 0.0);
    diffuseColor = vec3(0.0, 0.0, 0.0);
    specularColor = vec3(0.0, 0.0, 0.0);

    // two lights, light0 is the sun, light1 is the torch
    for (int i = 0; i < 2; i++) {
        vec3 lightDirection;
        float attenuation;

        if (0.0 == gl_LightSource[i].position.w) {
            // directional light
            attenuation = 1.0; // no attenuation
            lightDirection =
            normalize(vec3(gl_LightSource[i].position));
        } else {
            // point light or spotlight (or other kind of light)
            vec3 vertexToLightSource = vec3(gl_LightSource[i].position - gl_ModelViewMatrix * gl_Vertex);
            float distance = length(vertexToLightSource);
            attenuation = 1.0 / (1.0 + 0.5 * distance); // linear attenuation
            lightDirection = normalize(vertexToLightSource);

            if (gl_LightSource[i].spotCutoff <= 90.0) {
                // spotlight
                float clampedCosine = max(0.0, dot(-lightDirection, gl_LightSource[i].spotDirection));
                if (clampedCosine < gl_LightSource[i].spotCosCutoff) {
                    // outside the cone
                    attenuation = 0.0;
                } else {
                    attenuation = attenuation * pow(clampedCosine,
                    gl_LightSource[i].spotExponent);
                }
            }
        }

        vec3 ambientLighting = vec3(gl_LightSource[i].ambient);

        vec3 diffuseReflection = attenuation
            * vec3(gl_LightSource[i].diffuse)
            * max(0.0, dot(normalDirection, lightDirection));

        vec3 specularReflection;
        if (dot(normalDirection, lightDirection) < 0.0) {
            specularReflection = vec3(0.0, 0.0, 0.0);
        } else {
            specularReflection = attenuation
                * vec3(gl_LightSource[i].specular)
                * pow(max(0.0, dot(normalDirection, normalize(lightDirection + viewDirection))),
                gl_FrontMaterial.shininess);
        }

        ambientColor += ambientLighting;
        diffuseColor += diffuseReflection;
        specularColor += specularReflection;
    }

    diffuseColor = vec3(gl_FrontMaterial.ambient) * (vec3(gl_LightModel.ambient) + ambientColor)
                 + vec3(gl_FrontMaterial.diffuse) * diffuseColor;
    specularColor = vec3(gl_FrontMaterial.specular) * specularColor;

    texCoords = vec2(gl_MultiTexCoord0);
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
}