# WARNING this script doesn't work, although it seems like it should.
# The equivalent of this script works on windows imagemagick.
# See makeWinIcon.bat

# Althoug it doesn't work committing this file to git anyway
# for future reference. Maybe it can be fixed.

#Shell script to convert a image into win icon format. The script uses
# unix commandline tool 'imagemagick' and the 'specs' for the images to
# insert into the .ico file are as follows (according to Martin Lippert)
# 
# - the 16x, 32x, and 48x needs to be in the file twice - with 32bit color and with 8bit color
# - the 256x icon has to be 32bit only, but has to be uncompressed

# code from here: https://github.com/neo4j-contrib/neoclipse/pull/56

# This command can be used to check whether the contents of the .ico file
# looks ok:
#
#    identify -format '%f %p/%n %m %C/%Q %r %G %A %z\n' sts.ico
        
convert sts256.png -compress none \
    \( -clone 0 -resize 16x16 -compress none \) \
    \( -clone 0 -resize 24x24 -compress none \) \
    \( -clone 0 -resize 32x32 -compress none \) \
    \( -clone 0 -resize 48x48 -compress none \) \
    \( -clone 0 -resize 16x16 -colors 256 -compress none \) \
    \( -clone 0 -resize 24x24 -colors 256 -compress none \) \
    \( -clone 0 -resize 32x32 -colors 256 -compress none \) \
    \( -clone 0 -resize 48x48 -colors 256 -compress none \) \
    \( -clone 0 -resize 256x256 -compress none \) \
    -delete 0 sts.ico

convert sts512.png -compress none \
    \( -clone 0 -resize 256x256 -compress none \) \
    -delete 0 sts.ico
    
convert sts256.png -compress none sts.ico

identify -format '%f %p/%n %m %C/%Q %r %G %A %z\n' sts.ico
