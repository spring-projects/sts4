# Takes a DMG file as parameter
set -e

dmg_file=$1
notarize_profile=$2
dmg_filename="$(basename -- $dmg_file)"
dir="$(dirname "$dmg_file")"

cd $dir
xcrun notarytool submit ./${dmg_filename} --keychain-profile $notarize_profile --wait
echo "Staple and generate checksums for ${dmg_filename}"
xcrun stapler staple $dmg_filename
if [ $? -eq 0 ]; then
  shasum -a 256 $dmg_filename > ${dmg_filename}.sha256
  md5 $dmg_filename > ${dmg_filename}.md5
else
  echo "Notarization failed for ${dmg_file}"
  exit 1
fi
