# Build Spring CLI extension
npm install
rm -fr *.vsix
if [ "$1" = "pre-release" ]
then
  npm run vsce-pre-release-package
else
  npm run vsce-package
fi
rm -fr ${home}/.vscode/extensions/*vscode-spring-cli-*
code --install-extension *.vsix