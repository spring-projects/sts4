export JAVA_HOME=/home/kdvolder/Applications/jdk-11.0.8+10
mvn clean package -Pe417 -Psnapshot -Dsigning.skip=true -Dhttpclient.retry-max=20 -Dmaven.test.skip=true -Declipse.p2.mirrors=false -Dtycho.localArtifacts=ignore -Dorg.eclipse.equinox.p2.transport.ecf.retry=5 -Dskip.osx.signing=true -Dskip.win.signing=true -Dskip.osx.notarizing=true

