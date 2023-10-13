# Chandy and Lamport's Snapshot Protocol

This is the implementation of the chandy and lamport's snapshot protocol for Advanced OS course (CS 6378).
It makes use of Fidge/Mattern’s vector clock protocol for timestamping the application messages.

More micro details about the implementation can be found in the project description [here](project1.pdf).

## Requirements
1. java@1.8.0_341
2. maven@3.9.4

## Steps to compile and run the project

This code works by generating an executable jar file of the project and then uploading it into the dcXX machines (utd servers).
It is used for invoking the application on the respective machines (passed via config file) and execute the MAP protocol node on each of them.

It uses maven for managing the build and packages and hence is a requirement for compiling and running the application smoothly.

1. `cd` into the project's root directory
2. Execute the `launcher.sh` script as following to generate the jar and start the MAP protocol
```
bash launcher.sh <project_path> <path to config file on local> <project directory on dc machine> <netid> <rsa file path>
```

**Example:**
```
bash launcher.sh "/Users/chaitanyabasava/Documents/chandy_and_lamport_snapshot_protocol/" "/Users/chaitanyabasava/Documents/chandy_and_lamport_snapshot_protocol/src/main/resources/config.txt" "/home/012/s/sx/sxb220302/adv_os_proj_1" sxb220302 "~/.ssh/id_rsa_dc"
```

we can also pass the jar file path as `<project_path>` in case the jar has already been built.

**Example:**
```
bash launcher.sh "/Users/chaitanyabasava/Documents/chandy_and_lamport_snapshot_protocol/target/chandy_and_lamport_snapshot_protocol-1.0-SNAPSHOT-jar-with-dependencies.jar" "/Users/chaitanyabasava/Documents/chandy_and_lamport_snapshot_protocol/src/main/resources/config2.txt" "/home/012/s/sx/sxb220302/adv_os_proj_1" sxb220302 "~/.ssh/id_rsa_dc"
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

**NOTE:** The termination detection of the protocol should take care of this step though.

## executable jar file
The latest version of the built jar file has been uploaded to google drive 
([link](https://drive.google.com/file/d/1mntSa50e6jp-Dcbpss3zdeCojqWesQ7I/view?usp=sharing)).
