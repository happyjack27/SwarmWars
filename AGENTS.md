# AI agent primer

## What this project is

SwarmWars is a Java Swing simulation game about evolving competing swarms of organisms. Organisms use inherited genomes to define body traits and behavior; the world advances in timed ticks and renders the simulation in a Swing window.

## Project map

All Java sources are under `src/`, organized by package:

- `_UI/`: Swing entry point and display components. Start at `MainFrame`.
- `World/`: simulation lifecycle, time scheduling, world state, climate, and landscape grids. `Time` initializes the world; `World` advances organisms and resources.
- `Entities/`: organisms, plants, resources, beacons, and shared entity state. `Organism` connects its genome to sensing, behavior, and actions.
- `Genetics/`: traits, body definitions, genome serialization, and inheritance. `AnimalGenome` combines `Body` and `Behavior`.
- `Genetics/Behavior/`: organism inputs, outputs, visible traits, and the behavior controller.
- `Player/`: player ownership, alliances, and alignment.
- `Market/`, `Util/`, and `Trash/`: market classes, general utilities/scheduling, and older grid code.

Resource type indices are used throughout the simulation: `0` is plant/soil, `1` is meat, and `2` is medicine. Check `Resource.NUM_TYPES` and the resource consumers before changing this mapping.

## Build and run

This is a plain Java/Eclipse project: `.classpath` points to `src` and `bin`; there is no checked-in Maven/Gradle build, test, or lint configuration.

From the repository root, compile without adding generated files to the checkout:

```sh
mkdir -p /tmp/swarmwars-classes
javac -d /tmp/swarmwars-classes $(find src -name '*.java')
```

Launch the Swing simulation with:

```sh
java -cp /tmp/swarmwars-classes _UI.MainFrame
```

The GUI needs a graphical display. There is no automated test suite in the repository; after changes, at minimum compile all sources. For behavior or UI changes, run the simulation when a display is available and exercise the affected interaction.

## How the simulation starts and advances

`_UI.MainFrame.main` creates the window on Swing's event-dispatch thread. Its constructor calls `Time.init()` and `Time.initWorld()`. `Time.initWorld()` creates a `World` and initializes it with a 1366×768 area and resource counts. `Time.start_time` schedules ticks; each tick advances the world, which updates climate, movement, organism behavior, and deaths. A separate timer repaints the world view.

The initial population, breeding, and genome mutation are managed in `World`. Genome traits are represented in `Trait` and aggregated by `Inheritable`; `AnimalGenome` combines body and behavior traits. Trait collection/order is coupled to DNA conversion, so changes to these structures should be checked against `toDNA`, `fromDNA`, and offspring creation.

Useful display keys in the main window:

- `g`: cycle climate display mode.
- `s`: cycle organism display mode.
- `r`: refresh the soil, meat, and medicine landscape grids.

## Working guidance

- Read the relevant implementation before editing: comments and `src/notes.txt` contain a mixture of design ideas, TODOs, and stale notes, not a specification of current behavior.
- Preserve package names and directory layout; compile all sources after changes because these older packages are tightly coupled.
- The simulation uses shared static state (for example, `Time.world`, random generators, and aggregate genome statistics). Consider initialization order and side effects when changing constructors or lifecycle code.
- Randomness comes from both `Time.rand` and `Math.random()`. Do not assume seeded runs are fully repeatable.
- No external dependencies or additional build/test tools are currently configured. Prefer the JDK and the existing project structure.
