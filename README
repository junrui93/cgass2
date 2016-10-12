# Computer Graphics (comp9415/3421) Assignment 2 README

## information

### me

* author: Junrui Chen
* student number: 5124037

### development platform

* OS X 10.11.6 (EI Capitan)
* oracle java 1.8.0_101
* glsl version 120

(I've added runtime librarys of other platforms to the jar, hopefully it will work)

### how to run

`java -jar ass2.jar level.json`

### directory

* `ass2.jar/ass2/spec/*.java`: java source code
* `ass2.jar/shader/*.glsl`: shader code
* `ass2.jar/texture/**`: texture images
* `ass2.jar/level/*.json`: testing level files


## extension

### night mode

* press `m` to toggle night mode
* in night mode, the directional light will be dark and there will be a spot light attached to the hero
* can be found in light setting part in `display()` in `Game.java`, also in the shaders there's some code to deal with the spot light (only enemy and tree bark I use a shader)

### normal mapping on tree bark

* use a level file containing trees then you can see it
* it's turned on by default, press `n` to toggle
* code is in `bark_vshader.glsl` and `bark_fshader.glsl`


## The other/s

can include static enemies into the scene


    {
        "width" : 10,
        "depth" : 10,

        "sunlight" : [-1, 1, 0],

        "altitude" : [...],

        "enemies": [
            {
                "x" : 2,
                "z" : 1,
                "rotation": 180,
                "scale": 0.5
            },
        ],
    }

x, z is the enemy's location. Rotation and scale are optional.
