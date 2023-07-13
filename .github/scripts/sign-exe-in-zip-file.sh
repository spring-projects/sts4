file=$1
sign_script=$2
filename="$(basename -- $file)"

echo "****************************************************************"
echo "*** Processing : ${file}"
echo "****************************************************************"
destination_folder_name=extracted_${filename}
unzip -q $file -d ./${destination_folder_name}
sts_folder=`find ./${destination_folder_name} -maxdepth 1 -type d -name 'sts-*' -print -quit`
echo "Found folder: ${sts_folder}"
$sign_script ./${sts_folder}/SpringToolSuite4.exe ${sts_folder}/SpringToolSuite4.exe
zip -r -q $file ./${destination_folder_name}
