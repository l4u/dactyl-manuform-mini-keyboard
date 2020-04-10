# Dactyl ManuForm Tight Keyboard

This is a fork of the [Dactyl-ManuForm](https://github.com/tshort/dactyl-keyboard). The Dactyl-Manuform is a fork of the [Dactyl](https://github.com/adereth/dactyl-keyboard) with the thumb cluster from [ManuForm](https://github.com/jeffgran/ManuForm).

## Forks

- https://github.com/lebastaq/dactyl-manuform-mini-keyboard

## Features
- As small around the keys as possible
- Smoother transition between thumb and fingers (less facets)
- Thicker walls in steep regions where walls were too thin


## Generate OpenSCAD and STL models

* Run `lein repl`
* In the repl run `(load-file "src/dactyl_keyboard/dactyl.clj")`
* This will regenerate the `things/*.scad` files
* Use OpenSCAD to open a `.scad` file.
* Make changes to design, repeat `load-file`, OpenSCAD will watch for changes and rerender.
* When done, use OpenSCAD to export STL files


## Tips

* When trying things out, 10 seconds of rendering time in OpenSCAD is really annoying. Load one of the test outputs with commented out parts that you don't use.
* If you're not sure what things are generted by a piece of code, color them in using something like
`(color [0.5 0.5 0.5 0.5] (the code)`

## License

Copyright © 2015-2020 Matthew Adereth, Tom Short, Leo Lou, Okke Formsma

The source code for generating the models is distributed under the [GNU AFFERO GENERAL PUBLIC LICENSE Version 3](LICENSE).

The generated models are distributed under the [Creative Commons Attribution-ShareAlike 4.0 International (CC BY-SA 4.0)](LICENSE-models).
