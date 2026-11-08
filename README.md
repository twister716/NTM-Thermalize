# NTM: Thermalize

An addon for [NTM - Continuum](https://github.com/Warfactory-Official/NTM-Continuum) that connects its armor to the
body temperature of [Tough As Nails](https://github.com/Glitchfiend/ToughAsNails).

Sealed suits such as hazmat suits and power armor now affect your temperature. Fit them with thermal claddings to
stay warm or cool, install an air conditioner to keep your temperature steady, and watch out for stuffiness inside a
suit with no air conditioning.

## Features

### Full-body suits

A suit only takes effect when all four pieces are worn.

- **Sealed suits** (hazmat suits, power armor and similar) feel **stuffy**: they warm you up by one level unless an
  air conditioner is running.
- Each suit has a **temperature limit**, the number of levels its claddings can move your temperature (1 or 2).
- Other full armor sets are not stuffy, but their claddings work at half strength.
- The **Euphemium** set always keeps you at a neutral temperature.
- The **fire proximity suit** is always warm inside, though claddings can still change that.
- Armor alone never brings you to Icy or Hot. Only the weather outside can do that.

### Thermal claddings

Fitted in the cladding slot of the armor modification table, on any of the four pieces. The values of all four pieces
add up, and every 1.0 moves your temperature one level.

| Cladding                       | Temperature | Made in                                    |
|--------------------------------|-------------|--------------------------------------------|
| Wool Insulation Cladding       | +0.25       | Crafting table                             |
| Asbestos Insulation Cladding   | +0.5        | Assembly machine                           |
| Aluminium-Coated Ferrouranium Insulation Cladding | +0.5 | Chemical plant (also blocks radiation) |
| Copper Cooling Cladding        | −0.25       | Crafting table                             |
| Graphite Cooling Cladding      | −0.5        | Assembly machine                           |
| Cryo-Coated Ferrouranium Cooling Cladding | −0.5 | Chemical plant (also blocks radiation) |

TAN's wool and leaf armor are not affected by claddings.

### Air conditioners

Fitted in the leggings slot of a powered (battery) suit. They draw power from the suit and turn off stuffiness.

- **Air Conditioner** (10 HE/s): removes stuffiness.
- **Thermal Equilibrium Air Conditioner** (25 HE/s): also pulls your temperature toward neutral, up to the suit's
  limit.

Press **G** (configurable) to turn the air conditioner on or off. Some top-tier suits have an air conditioner built in
that needs no power.

### Tooltips

The full set bonus in an armor tooltip lists heat retention or temperature balance, the built-in air conditioner and the temperature limit.

## Requirements

| Minecraft | Loader            | Tough As Nails |
|-----------|-------------------|----------------|
| 26.1.2      | NeoForge / Fabric | 21.11.0.6+     |
| 1.21.1    | NeoForge / Fabric | 10.1.0.13+     |
| 1.20.1    | Forge / Fabric    | 9.2.0.171+     |

All versions also need:

- [NTM - Continuum](https://modrinth.com/mod/ntm-continuum)
- Golem Core
- Tough As Nails and its library GlitchCore

1.20.1 Forge needs Forge 47.3.0 or newer.

[NTM: Space](https://github.com/Warfactory-Official/NTM-Continuum-Space) is listed as an optional dependency, but it
has no integration yet. Support for it is planned for a future version.

## Building

You need a JDK (17, 21 or 25) to start Gradle; it downloads the Java each version needs.

```bash
./gradlew :26.1-neoforge:runClient   # start a dev client
./gradlew chiseledBuild               # build every version
```

The jars are written to `versions/<node>/build/release/`. Run `./gradlew "Reset active project"` before you commit.

## Credits

- [NTM - Continuum](https://github.com/Warfactory-Official/NTM-Continuum) by Warfactory, a port of HBM's Nuclear Tech
  Mod.
- The textures of the thermal claddings and air conditioners are derived from NTM textures. They are recolored
  versions of textures from NTM - Continuum, used under LGPL-3.0.
- [Tough As Nails](https://github.com/Glitchfiend/ToughAsNails) by Glitchfiend.
- Built with the Continuum addon template and Golem Core.

## License

This mod is licensed under the **GNU Lesser General Public License v3.0**. See [LICENSE.LESSER](LICENSE.LESSER); the
LGPL builds on the GNU GPL v3.0 in [LICENSE](LICENSE).
