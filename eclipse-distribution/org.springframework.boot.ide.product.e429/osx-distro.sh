set -e

file=$1
filename="$(basename -- $file)"
dir="$(dirname "$file")"

echo "****************************************************************"
echo "*** Processing : ${file}"
echo "****************************************************************"
destination_folder_name=extracted_${filename}
echo "Extracting archive ${filename} to ${dir}/${destination_folder_name}"
mkdir ${dir}/${destination_folder_name}
tar -zxf $file --directory ${dir}/${destination_folder_name}
echo "Successfully extracted ${filename}"
echo "About to sign OSX .app file: ${dir}/${destination_folder_name}/SpringToolSuite4.app"
cd ${dir}/${destination_folder_name}
echo "Generating dmg-config.json..."
echo '{' >> dmg-config.json
echo '  "title": "Spring Tool Suite 4",' >> dmg-config.json
echo '  "icon": "/Users/aboyko/git/sts4/eclipse-distribution/org.springframework.boot.ide.branding/sts4.icns",' >> dmg-config.json
echo '  "contents": [' >> dmg-config.json
echo '    { "x": 192, "y": 100, "type": "file", "path": "./SpringToolSuite4.app" },' >> dmg-config.json
echo '    { "x": 448, "y": 100, "type": "link", "path": "/Applications" },' >> dmg-config.json
echo '    { "x": 1000, "y": 2000, "type": "file", "path": "/Users/aboyko/git/sts4/eclipse-distribution/org.springframework.boot.ide.branding/sts4.icns", "name": ".VolumeIcon.icns" }' >> dmg-config.json
echo '  ],' >> dmg-config.json
echo '  "format": "UDZO"' >> dmg-config.json
echo '}' >> dmg-config.json
cat ./dmg-config.json
dmg_filename=${filename%.*.*}.dmg
appdmg ./dmg-config.json ../${dmg_filename}
cd ..
rm -rf ./${destination_folder_name}

echo "Checksums $dmg_filename"
(shasum -a 256 $dmg_filename > ${dmg_filename}.sha256 \
&& md5 $dmg_filename > ${dmg_filename}.md5) &
wait
echo "Completed"
#rm -f $filename
#zip -r -q ../$file .
#cd ..
#echo "Successfully zipped ${destination_folder_name} into ${file}"
#java -jar $self_extr_jar $file
#echo "Remove temporary ${destination_folder_name}"
#rm -rf ./${destination_folder_name}
#
#echo "Creating checksums sha-256 and md5 for ${file}"
#shasum -a 256 $file > ${file}.sha256
#md5 $file > ${file}.md5
#self_jar_file=${file%.*}.self-extracting.jar
#echo "Creating checksums sha-256 and md5 for ${self_jar_file}"
#shasum -a 256 $self_jar_file > ${self_jar_file}.sha256
#md5 $self_jar_file > ${self_jar_file}.md5