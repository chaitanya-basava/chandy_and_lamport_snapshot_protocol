#!/bin/bash

if [ $# -eq 4 ]; then
    rsa_path="$HOME/.ssh/id_rsa"
elif [ $# -eq 5 ]; then
    rsa_path="$5"
else
    echo "Usage: $0 <project_path> <path to config file on local> <project directory on dc machine> <netid> <rsa file path>"
    exit 1
fi

project_dir="$1"
config_file="$2"
remote_proj_path="$3"
net_id="$4"

ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -i "$rsa_path" "$net_id@dc01" "rm -rf $remote_proj_path && mkdir $remote_proj_path"

if [[ $project_dir == *".jar" ]]; then
  jar_path="$project_dir"
else
  mvn -f "$project_dir" clean package
  jar_path="$project_dir/target/chandy_and_lamport_snapshot_protocol-1.0-SNAPSHOT-jar-with-dependencies.jar"
fi

scp -i "$rsa_path" "$config_file" "$net_id@dc01:$remote_proj_path/config.txt"
scp -i "$rsa_path" "$jar_path" "$net_id@dc01:$remote_proj_path/chandy_and_lamport_snapshot_protocol-1.0.jar"

java -jar "$jar_path" com.advos.ExecuteJar -c "$config_file" -id "$net_id" -jar "$remote_proj_path/chandy_and_lamport_snapshot_protocol-1.0.jar" -rc "$remote_proj_path/config.txt" -ssh "$rsa_path"

exit 0
