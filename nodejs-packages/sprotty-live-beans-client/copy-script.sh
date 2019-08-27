set -ev
# For vscode webview
rm -rf ../../vscode-extensions/vscode-spring-boot/media/*
mkdir -p ../../vscode-extensions/vscode-spring-boot/media
cp bundle.js* ../../vscode-extensions/vscode-spring-boot/media
cp -R css ../../vscode-extensions/vscode-spring-boot/media
cp -R lib ../../vscode-extensions/vscode-spring-boot/media

# For webserver embedded in language server
rm -fr ../../headless-services/spring-boot-language-server/src/main/resources/static/bundle*
rm -fr ../../headless-services/spring-boot-language-server/src/main/resources/static/css
cp bundle.js* ../../headless-services/spring-boot-language-server/src/main/resources/static
cp -R css ../../headless-services/spring-boot-language-server/src/main/resources/static
cp -R lib ../../headless-services/spring-boot-language-server/src/main/resources/static
