# Takes a DMG file as parameter
set -e

$dmg_file=$1
dmg_filename="$(basename -- $dmg_file)"
dir="$(dirname "$dmg_file")"

echo "****************************************************************"
echo "*** Notarizing: ${dmg_filename}"
echo "****************************************************************"
cd $dir
xcrun notarytool submit ./${dmg_filename} --keychain-profile notarize-app-dmg-profile --wait
echo "Staple and generate checksums for ${dmg_filename}"
xcrun stapler staple $dmg_filename
shasum -a 256 $dmg_filename > ${dmg_filename}.sha256
md5 $dmg_filename > ${dmg_filename}.md5)