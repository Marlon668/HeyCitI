# HeyCitI
The source code for the HeyCitI simulator.

Current up to date version: **1.1.0.**


## Building the simulator

To build the simulator, simply run the command `mvn compile`. The generated source are placed in the `target` folder.
The simulator can then be run with the following command: `mvn exec:java`.

Alternatively, run the command `mvn package`. This will generate a jar file under the target directory: `HeyCitI-{version}-jar-with-dependencies.jar`.

Similarly to the previously listed commands, `mvn test` runs the tests for the project.

## Running the simulator

Either run the jar file generated from the previous step, or use the maven exec plugin.
<!-- A jar file is exported to the folder DingNetExe which also contains the correct file structure. Run the jar file to run the simulator.
The simulator can also be started from the main method in the MainGUI class. -->


