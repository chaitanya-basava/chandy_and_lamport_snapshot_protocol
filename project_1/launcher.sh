#!/bin/bash
#bash launcher.sh "/Users/chaitanyabasava/Documents/advanced_os/project_1/src/main/resources/config.txt" "/Users/chaitanyabasava/Documents/advanced_os/project_1/target/" "project_1-1.0-SNAPSHOT-jar-with-dependencies.jar" "/home/012/s/sx/sxb220302/adv_os_proj_1/" sxb220302 "~/.ssh/id_rsa_dc"

pre_run_cleanup() {
    listen_lines=$(lsof -P -i -n | grep java | grep sxb220302)

    while IFS= read -r listen_line; do
      process_id=$(echo "$listen_line" | awk '{print $2}')

      if [[ -n "$process_id" ]]; then
        kill "$process_id"
        echo "Killed process $process_id"
      fi
    done <<< "$listen_lines"
}

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

mvn -f "$project_dir" clean package

#bash -c ". cleanup.sh $net_id $rsa_path"

jar_path="$project_dir/target/project_1-1.0-SNAPSHOT-jar-with-dependencies.jar"

scp -i "$rsa_path" "$config_file" "$net_id@dc01:$remote_proj_path/config.txt"
scp -i "$rsa_path" "$jar_path" "$net_id@dc01:$remote_proj_path/project_1-1.0.jar"

java -jar "$jar_path" com.advos.ExecuteJar -c "$config_file" -id "$net_id" -jar "$remote_proj_path/project_1-1.0.jar" -rc "$remote_proj_path/config.txt" -ssh "$rsa_path"

exit 0
