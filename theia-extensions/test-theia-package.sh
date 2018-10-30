package_tarball=$1
if [ -z "${package_tarball}" ]; then
    echo "Supply package tarball path as a parameter!"
    exit 1
fi

parent_dir=$2
if [ -z "${parent_dir}" ]; then
    parent_dir=`pwd`
fi

filename=$(basename $package_tarball)

test_dir="${parent_dir}/test-package-${filename}"
echo "Testing Theia tarball package '${filename}' in folder '${test_dir}'"
if [ -d "$test_dir" ]; then
    echo "Clearing test directory..."
    rm -rf $test_dir
fi

mkdir $test_dir
cd $test_dir

echo "Generating 'package.json' file..."

cat > package.json << EOF
{
  "private": true,
  "name": "browser-app",
  "version": "0.0.0",
  "dependencies": {
    "@theia/core": "latest",
    "@theia/filesystem": "latest",
    "@theia/workspace": "latest",
    "@theia/preferences": "latest",
    "@theia/navigator": "latest",
    "@theia/process": "latest",
    "@theia/terminal": "latest",
    "@theia/editor": "latest",
    "@theia/languages": "latest",
    "@theia/markers": "latest",
    "@theia/monaco": "latest",
    "@theia/typescript": "latest",
    "@theia/messages": "latest"
  },
  "devDependencies": {
    "@theia/cli": "latest"
  },
  "scripts": {
    "prepare": "theia build",
    "start": "theia start",
    "watch": "theia build --watch"
  },
  "theia": {
    "target": "browser"
  }
}
EOF

echo "Installing NPM packages..."
yarn add $package_tarball

echo "Launching Theia browser app..."
yarn start