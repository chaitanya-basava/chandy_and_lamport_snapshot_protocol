#bash launcher.sh "/Users/chaitanyabasava/Documents/advanced_os/project_1/src/main/resources/config.txt" "/Users/chaitanyabasava/Documents/advanced_os/project_1/target/" "project_1-1.0-SNAPSHOT-jar-with-dependencies.jar" "/home/012/s/sx/sxb220302/adv_os_proj_1/" sxb220302 "~/.ssh/id_rsa_dc" "/Users/chaitanyabasava/Documents/advanced_os/project_1/pre_run_cleanup.sh"

if [ $# -ne 7 ]; then
    echo "Usage: $0 <config_file> <jar_path> <jar_name> <remote_proj_path> <net_id> <rsa_path> <cleanup>"
    exit 1
fi

config_file="$1"
jar_path="$2"
jar_name="$3"
remote_proj_path="$4"
net_id="$5"
rsa_path="$6"
cleanup="$7"

scp -i "$rsa_path" "$cleanup" "$net_id@dc01:$remote_proj_path/pre_run_cleanup.sh"

for hostNum in $(seq -f "%02g" 1 45); do
    ((i=i%7)); ((i++==0)) && wait
    ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -i ~/.ssh/id_rsa_dc sxb220302@dc"$hostNum" "bash $remote_proj_path/pre_run_cleanup.sh" &
done

scp -i "$rsa_path" "$config_file" "$net_id@dc01:$remote_proj_path/config.txt"
scp -i "$rsa_path" "$jar_path$jar_name" "$net_id@dc01:$remote_proj_path$jar_name"

java -jar "$jar_path$jar_name" com.advos.ExecuteJar -c "$config_file" -id "$net_id" -jar "$remote_proj_path$jar_name" -rc "$remote_proj_path/config.txt" -ssh $rsa_path

exit 0
