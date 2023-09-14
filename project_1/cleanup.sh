if [ $# -ne 2 ]; then
    echo "Usage: $0 <net_id> <rsa file path>"
    exit 1
fi

net_id="$1"
rsa_path="$2"

for hostNum in $(seq -f "%02g" 1 45); do
    ((i=i%7)); ((i++==0)) && wait
    ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -i "$rsa_path" "$net_id"@dc"$hostNum" killall -u "$net_id" &
done