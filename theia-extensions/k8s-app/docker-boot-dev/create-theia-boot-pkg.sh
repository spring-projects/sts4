workdir=`pwd`

cd ../../theia-spring-boot
./build.sh
cd spring-boot
tar_file=theia-spring-boot.tgz
yarn pack --filename $tar_file
mv $tar_file $workdir
