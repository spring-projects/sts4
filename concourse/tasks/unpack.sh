#!/bin/bash
set -e
workdir=$(pwd)
in=${workdir}/in
out=${workdir}/out
cd $out
tar xvf ${in}/*.tar.gz 