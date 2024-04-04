# A time-dependent subgraph-capacity model for multiple shortest paths and application to CO2/contrail-safe aircraft trajectories

#### Céline Demouge, Marcel Mongeau, Nicolas Couellan, Daniel Delahaye

This code is linked to the paper with the same title and authors. Please refer to it before reading or launching this software.

Documentation can be found in doc repository. Launch doc/index.html to obtain the complete javadoc.

This software uses the CPLEX java API. Don't forget to use "-Djava.library.path=path_to_cplex" to use it and to add associated .jar archive to the project before compilation. The main class is Main.java. The model is built and solved in the model package. The package graph is used for airspace and weather data. The package utils is used for constants and computational processes.  

FRA200.csv is a sample data file for this algorithm.

Please refer any question to [celine.demouge@enac.fr](mailto:celine.demouge@enac.fr).