file=$0
sign_script=$1
filename="$(basename -- $file)"

echo "****************************************************************"
echo "*** Processing : ${file}"
echo "****************************************************************"
destination_folder_name=extracted_${filename}
unzip $file -d ./${destination_folder_name}
sts_folder=find ./${destination_folder_name} -maxdepth 1 -type d -name 'sts-*' -print -quit
$sign_script ${sts_folder}/SpringToolSuite4.exe ${sts_folder}/SpringToolSuite4.exe
zip -r $file ./${destination_folder_name}
