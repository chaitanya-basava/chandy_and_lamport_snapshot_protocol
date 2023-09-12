# Project 1

This is the implementation of project 1 for Advanced Operating System course section 001.

## Steps to compile and run the project

This code works by generating an executable jar file of the project and then uploading it into the dcXX machines.
it is used for invoking the application on the respective machines (passed via config file) and execute the MAP protocol on each of them.

It uses maven for managing the build and packages and is a requirement for compiling and running the application.

1. `cd` into the project's root directory (`project_1`).
2. To generate the jar file run the following maven cmd
```
mvn clean package
```
3. Once the jar file is generated, execute the `launcher.sh` script as following to start the MAP protocol
```
bash launcher.sh <path to config file on local> <path to jar file> <jar file's name> <project directory on dc machine> <netid> <rsa file path>
```

**NOTE:** This assumes you have done the steps to enable passwordless login to dcXX machine on your local machine and have the corresponding rsa pem file.
