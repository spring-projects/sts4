file=$1
filename="$(basename -- $file)"

workdir=`pwd`

echo "****************************************************************"
echo "*** Processing : ${file}"
echo "****************************************************************"
destination_folder_name=extracted_${filename}
echo "Extracting archive ${filename}"
unzip -q $file -d ./${destination_folder_name}
echo "Successfully extracted ${filename}"
sts_folder=`find ./${destination_folder_name} -maxdepth 1 -type d -name 'sts-*' -print -quit`
echo "Found STS distro folder: ${sts_folder}"
${workdir}/.github/scripts/sign-exe.sh ./${sts_folder}/SpringToolSuite4.exe ${sts_folder}/SpringToolSuite4.exe
echo "Adding to zip contents of a folder ${destination_folder_name}"
zip -r -q $file ./${destination_folder_name}
echo "Successfully zipped ${destination_folder_name} into ${file}"
java -jar ${workdir}/.github/scripts/self-extracting-jar-creator.jar $file

filename_no_ext="${filename%.*}"
echo "Creating checksums sha-256 and md5"
shasum -a 256 $file > ${file}.sha256
md5 $file > ${file}.md5
self_jar_file=${file::-4}.self-extracting.jar
shasum -a 256 $self_jar_file > ${self_jar_file}.sha256
md5 $self_jar_file > ${self_jar_file}.md5

