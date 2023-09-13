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

if [ $# -eq 5 ]; then
    rsa_path="$HOME/.ssh/id_rsa"
elif [ $# -eq 6 ]; then
    rsa_path="$6"
else
    echo "Usage: $0 <path to config file on local> <path to jar file> <jar file's name> <project directory on dc machine> <netid> <rsa file path>"
    exit 1
fi

config_file="$1"
jar_path="$2"
jar_name="$3"
remote_proj_path="$4"
net_id="$5"

for hostNum in $(seq -f "%02g" 1 45); do
    ((i=i%7)); ((i++==0)) && wait
    ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -i "$rsa_path" "$net_id"@dc"$hostNum" killall -u "$net_id" &
done

scp -i "$rsa_path" "$config_file" "$net_id@dc01:$remote_proj_path/config.txt"
scp -i "$rsa_path" "$jar_path$jar_name" "$net_id@dc01:$remote_proj_path$jar_name"

java -jar "$jar_path$jar_name" com.advos.ExecuteJar -c "$config_file" -id "$net_id" -jar "$remote_proj_path$jar_name" -rc "$remote_proj_path/config.txt" -ssh $rsa_path

exit 0
