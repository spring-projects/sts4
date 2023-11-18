f=$1
entitlements=$2
echo "Looking for 'libjansi.jnilib' files inside ${f} to sign..."
f_name="$(basename -- $f)"
extracted_jar_dir=extracted_${f_name}
rm -rf $extracted_jar_dir
mkdir $extracted_jar_dir
echo "Extracting archive ${f}"
unzip -q $f -d ./${extracted_jar_dir}
for jnilib_file in `find $extracted_jar_dir -type f | grep -E ".*/libjansi\.jnilib$"`
do
  echo "Signing binary file: ${jnilib_file}"
  codesign --verbose --deep --force --timestamp --entitlements "${entitlements}" --options=runtime --keychain "${KEYCHAIN}" -s "${MACOS_CERTIFICATE_ID}" $jnilib_file
done
cd $extracted_jar_dir
zip -r -u $f .
cd ..
rm -rf $extracted_jar_dir