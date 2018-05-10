# The Dactyl-ManuForm-Mini Keyboard

This is a fork of the [Dactyl-ManuForm](https://github.com/tshort/dactyl-keyboard). The Dactyl-Manuform is a fork of the [Dactyl](https://github.com/adereth/dactyl-keyboard) with the thumb cluster from [ManuForm](https://github.com/jeffgran/ManuForm).

## Features

- Sidenub is disabled by default so that Kailh, Outemu and other switches can be
  used. If you use Cherry MX or Gateron switches, you can enable the sidenubs.
- One key in the thumb cluster has been removed. Other thumb keys are also adjusted.
- The total height is reduced and wire posts are removed. This results in a
  higher printing speed and a lower cost.
- A TRRS mount instead of a RJ9 mount is used.
- A micro USB mount is used. A breakout board, or an extension cable can be used.
- Screw posts are moved inside.
- The pro micro holder has been modified. Dupont cables can be used.

## Future plans (PR is welcome)

- Move screw posts 3mm upwards so that the bottom plate can be inserted into the case.
- Add smoothing.

## Getting the case files and bottom plate

### Option 1: Generate OpenSCAD and STL models

* Run `lein generate` or `lein auto generate`
* This will regenerate the `things/*.scad` files
* Use OpenSCAD to open a `.scad` file.
* Make changes to design, repeat `load-file`, OpenSCAD will watch for changes and rerender.
* When done, use OpenSCAD to export STL files

### Option 2: Download from Releases

Generated models (only 4x5) can be downloaded from [Releases](https://github.com/l4u/dactyl-manuform-mini-keyboard/releases).

### Option 3: Download from Thingiverse

[Search for Dactyl-ManuForm-Mini on Thingiverse](https://www.thingiverse.com/l4u/collections/dactyl-manuform-mini)

## License

Copyright © 2015-2018 Matthew Adereth, Tom Short and Leo Lou

The source code for generating the models is distributed under the [GNU AFFERO GENERAL PUBLIC LICENSE Version 3](LICENSE).

The generated models are distributed under the [Creative Commons Attribution-ShareAlike 4.0 International (CC BY-SA 4.0)](LICENSE-models).
