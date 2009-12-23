find . -type f  | while read file;
#find . -type d  | while read file;
do
  target=`echo "$file" | sed -e 's/'$(echo -e "\0204")'/a/g' | sed -e 's/'$(echo -e "\0216")'/A/g' | sed -e 's/'$(echo -e "\0224")'/o/g' | sed -e 's/'$(echo -e "\0231")'/O/g'`;
  if [ "$file" != "$target" ]
  then
    echo "Renaming '$file' to '$target'";
    #mv  "$file" "$target";
  fi  
done;