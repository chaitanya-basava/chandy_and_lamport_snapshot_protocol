# Project 1

This is the implementation of project 1 for Advanced Operating System course section 001.

## Requirements
1. java@1.8.0_341
2. maven@3.9.4

## Steps to compile and run the project

This code works by generating an executable jar file of the project and then uploading it into the dcXX machines.
it is used for invoking the application on the respective machines (passed via config file) and execute the MAP protocol on each of them.

It uses maven for managing the build and packages and is a requirement for compiling and running the application.

1. `cd` into the project's root directory (`project_1`)
2. Execute the `launcher.sh` script as following to generate the jar and start the MAP protocol
```
bash launcher.sh <project_path> <path to config file on local> <project directory on dc machine> <netid> <rsa file path>
```

**Example:**
```
bash launcher.sh "/Users/chaitanyabasava/Documents/advanced_os/project_1/" "/Users/chaitanyabasava/Documents/advanced_os/project_1/src/main/resources/config.txt" "/home/012/s/sx/sxb220302/adv_os_proj_1" sxb220302 "~/.ssh/id_rsa_dc"
```

**NOTE:** This assumes you have done the steps to enable passwordless login to dcXX machine on your local machine and have the corresponding rsa pem file.

## Steps to clean up
You can use the cleanup script to clean up all the dangling processes (if any).
```
bash cleanup.sh <netid> <rsa file path>
```

**Example:**
```
bash cleanup.sh sxb220302 "~/.ssh/id_rsa_dc"
```
