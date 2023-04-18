# Operational Description

## General Steps

*Run a load*

1. Orchestrator has to load configuration from YAML
1. Obtain a Target object to connect to the target LDAP with correct rights
1. Obtain Source objects and do loading according rules
1. Register cache objects into Cache object

*Update once*

1. Orchestrator has to load configuration from YAML
1. Obtain a Target object to connect to the target LDAP with correct rights
1. Obtain Source objects and obtain current cache data
1. Use Cache to obtain the difference between current and loaded data
1. Build distinct DN update hierarchy and process bottom to top
    1. Delete entries
    1. Modify attributes


### Running standalone

1. App tests variables for connection
1. App create a cache database


### Running in a container

1. Image builder request to the App all environment variables to setup the image
