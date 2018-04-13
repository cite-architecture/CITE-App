#!/bin/bash
echo "works"
PYRAMID_DIR=../tiff

#
# SRC_EXTENSION is the dot-extension on the SOURCE files:  if you're
# converting .tif files, change this value to "tif" for example
SRC_EXTENSION=jpg

echo $PYRAMID_DIR

# Script doing the work:
VIPS=`which vips`
echo $VIPS
MV=`which mv`
echo $MV
for f in $*
do
	echo "------------------"
  baseName=$(echo $f | sed -e "s/.$SRC_EXTENSION//")    
   newF=${PYRAMID_DIR}/${baseName}
  echo "converting " $f " to " $newF " ..."
  echo
  $VIPS dzsave $f $newF
done;

