#!/bin/bash
echo "Making Deep Zoom Files."
echo "Usage:"
echo "1. Navigate to a directory of images."
echo "2. 'mkdir ../new_image_archive'"
echo "3. './dz2.sh *.jpg'"
PYRAMID_DIR=../new_image_archive/
SRC_EXTENSION=jpg
# Script doing the work:
VIPS=`which vips`
MV=`which mv`
for f in $*
do
	echo "------------------"
	baseName=$(echo $f | sed -e "s/.$SRC_EXTENSION//")    
	newF=${PYRAMID_DIR}/${baseName}
	echo "copying " $f " to " $PYRAMID_DIR " ..."
	cp $f $PYRAMID_DIR
	echo "converting " $f " to " $newF " ..."
	echo
	$VIPS dzsave $f $newF
done;
