git_repo=$1
clone_folder="${git_repo##*/}"
rm -rf ${clone_folder}
git clone ${git_repo}
docker run -it -p 3000:3000 -v "$(pwd)/${clone_folder}:/home/project" $2