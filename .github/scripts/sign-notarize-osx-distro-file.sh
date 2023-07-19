# Takes a .tar.gz file as a parameter, entitlements.plist file path and icns file path
set -e

file=$1
entitlements=$2
icns=$3
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
codesign --verbose --deep --force --timestamp --entitlements $entitlements --options=runtime --keychain $KEYCHAIN -s $MACOS_CERTIFICATE_ID ${dir}/${destination_folder_name}/SpringToolSuite4.app

cd ${dir}/${destination_folder_name}
echo "Generating dmg-config.json..."
echo '{' >> dmg-config.json
echo '  "title": "Spring Tool Suite 4",' >> dmg-config.json
echo '  "icon": "/Users/aboyko/git/sts4/eclipse-distribution/org.springframework.boot.ide.branding/sts4.icns",' >> dmg-config.json
echo '  "contents": [' >> dmg-config.json
echo '    { "x": 192, "y": 100, "type": "file", "path": "./SpringToolSuite4.app" },' >> dmg-config.json
echo '    { "x": 448, "y": 100, "type": "link", "path": "/Applications" },' >> dmg-config.json
echo '    { "x": 1000, "y": 2000, "type": "file", "path": "'$icns'", "name": ".VolumeIcon.icns" }' >> dmg-config.json
echo '  ],' >> dmg-config.json
echo '  "format": "UDZO"' >> dmg-config.json
echo '}' >> dmg-config.json
cat ./dmg-config.json
dmg_filename=${filename%.*.*}.dmg
appdmg ./dmg-config.json ../${dmg_filename}
cd ..
rm -rf ./${destination_folder_name}
rm -f $filename

echo "Sign $dmg_filename"
codesign --verbose --deep --force --timestamp --keychain $KEYCHAIN -s $MACOS_CERTIFICATE_ID $dmg_filename

echo "Notarizing $dmg_filename"
(xcrun notarytool submit ${dmg_filename} --keychain-profile notarize-app-dmg-profile --wait \
&& echo "Staple and generate checksums" \
&& xcrun stapler staple $dmg_filename \
&& shasum -a 256 $dmg_filename > ${dmg_filename}.sha256 \
&& md5 $dmg_filename > ${dmg_filename}.md5) &