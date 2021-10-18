# Coteji CLI
Command Line Interface tool for Coteji
### Run Coteji CLI
1. Make sure you have JRE or JDK 8+ installed (`JAVA_HOME` environemnt variable is set)
2. Download the `zip` or `tar.gz` file from [releases](https://github.com/coteji/coteji-cli/releases) (or build it from sources as described below)
3. Unpack somewhere, add `bin` directory your `PATH` environment variable
4. Create a configuration file (e.g. `config.coteji.kts`). See example [here](https://github.com/coteji/coteji.github.io).
5. From the directory where your configuration file resides run (shoud work both for Windows and Unix):
```shell
./coteji --help
```
You should see something like this:
```
Usage: coteji [OPTIONS] COMMAND [ARGS]...

Options:
  --config TEXT
  -h, --help     Show this message and exit

Commands:
  sync-all   Pushes all the tests found in the Source, to the Target. Deletes
             all the tests in the Target that are not present in the Source
             (match by id).
  sync-only  Pushes selected by QUERY tests in the Source, to the Target
  push-new   Finds all tests in the Source without IDs and pushes them to the
             Target
  dry-run    Emulates the result of syncAll action without actually doing
             anything, just logs the results to the console.
  try-query  Prints the list of tests from the Source found by query
```
### Build the CLI from sources
To build the CLI from sources, run:
```shell
./gradlew installDist
```
and you will find the CLI in `build/install/coteji-cli` directory. 
