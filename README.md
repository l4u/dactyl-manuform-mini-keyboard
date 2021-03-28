# Dactyl ManuForm Mini Keyboard

This is a fork of the [Dactyl-ManuForm](https://github.com/tshort/dactyl-keyboard). The Dactyl-Manuform is a fork of the [Dactyl](https://github.com/adereth/dactyl-keyboard) with the thumb cluster from [ManuForm](https://github.com/jeffgran/ManuForm).

Build log: https://www.beekeeb.com/dactyl-manuform-mini-mechanical-keyboard-build-log/

## Forks

- https://github.com/lebastaq/dactyl-manuform-mini-keyboard
- [Dactyl ManuForm Tight Keyboard](https://github.com/okke-formsma/dactyl-manuform-tight)
- https://github.com/carbonfet/dactyl-manuform
- [Track Beast](https://github.com/davekincade/dactyl-manuform-mini-keyboard/tree/dk-track-beast)
- [Tractyl ManuForm Keyboard](https://github.com/noahprince22/tractyl-manuform-keyboard)
- [Compactyl](https://github.com/dereknheiley/dactyl-manuform-tight)

## Features

- The use of sidenubs can be disabled. Sidenub should be disabled if you use Kailh, and Outemu. If you use Cherry MX, Gateron or Zealios switches, you can enable the sidenubs.
- Spaces for rentention tabs are added.
- One key in the thumb cluster has been removed. Other thumb keys are also adjusted.
- The total height is reduced and wire posts are removed. This results in a
  higher printing speed and a lower cost.
- A TRRS mount instead of a RJ9 mount is used.
- A micro USB mount is used. A breakout board, or an extension cable can be used.
- Screw posts are moved inside. The holes are designed for TRISERT® thread inserts 145m3.
- The pro micro holder has been modified. Dupont cables can be used.

## Generate OpenSCAD and STL models

* Run `lein generate` or `lein auto generate`
* This will regenerate the `things/*.scad` files
* Use OpenSCAD to open a `.scad` file.
* Make changes to design, repeat `load-file`, OpenSCAD will watch for changes and rerender.
* When done, use OpenSCAD to export STL files



## License

Copyright © 2015-2018 Matthew Adereth, Tom Short and Leo Lou

The source code for generating the models is distributed under the [GNU AFFERO GENERAL PUBLIC LICENSE Version 3](LICENSE).

The generated models are distributed under the [Creative Commons Attribution-ShareAlike 4.0 International (CC BY-SA 4.0)](LICENSE-models).
