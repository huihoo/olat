#!/bin/bash

#
# @author: guido
# searches for all i18n folders and creates an output which can be used in an java enum typesafe manner

for path in `find . -name '_i18n' | grep 'presentation' | sed 's/_i18n//g' | sed 's/\.//g'`
do

	enumName=`echo $path | cut -d/ -f5,6,7,8,9,10,11,12,13,14 | sed 's/\//_/g' | tr '[:lower:]' '[:upper:]'`
	echo "$enumName(\"$path\"),"

done
