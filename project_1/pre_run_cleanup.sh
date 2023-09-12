listen_lines=$(lsof -P -i -n | grep java | grep sxb220302)

while IFS= read -r listen_line; do
  echo "$listen_line"
  process_id=$(echo "$listen_line" | awk '{print $2}')

  if [[ -n "$process_id" ]]; then
    kill "$process_id"
    echo "Killed process $process_id"
  fi
done <<< "$listen_lines"
