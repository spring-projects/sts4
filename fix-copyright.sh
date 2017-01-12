#!/bin/bash
grep -r --exclude-dir=resources --exclude-dir=target -L --include="*.java" "\* Copyright" > missing-copyright.txt

for i in `cat missing-copyright.txt` ; do
   echo Fixing $i
   cat java-copyright-header.txt "$i" > $i.tmp
   mv $i.tmp $i
done
