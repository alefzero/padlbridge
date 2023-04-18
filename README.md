# PadlBridge

This project supports the usage of localized languages:

- [English](README.md)
- [Português do Brasil](README_pt_BR.md)

PadlBridge transfer, consolidates, update (or sync) data from many sources to a LDAP. Sources can be external LDAP, relational databases an even text files. Other sources adapters can be easily created to extend funcionality.

## Packages

- padlcore: Java application where integration data services are provided. Can run standalone, using external external LDAP as target and a database for cache control, or integrated with a provided container image to run these services.



## Build yourself

This application uses gradle to generate release artifacts.
