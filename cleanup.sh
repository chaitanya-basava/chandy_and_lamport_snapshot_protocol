if [ $# -eq 1 ]; then
    rsa_path="$HOME/.ssh/id_rsa"
elif [ $# -eq 2 ]; then
    rsa_path="$2"
else
    echo "Usage: $0 <net_id> <rsa file path>"
    exit 1
fi

net_id="$1"

for hostNum in $(seq -f "%02g" 1 45); do
    ((i=i%7)); ((i++==0)) && wait
    ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -i "$rsa_path" "$net_id"@dc"$hostNum" killall -u "$net_id" &
done

exit 0
