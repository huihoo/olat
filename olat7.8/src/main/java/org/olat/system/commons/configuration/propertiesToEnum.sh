#!/bin/bash

while read line
do
        propline=`echo $line | grep -v '^#'`
        charsCount=`echo $propline | wc -m`
        if [ $charsCount -gt 2 ]
        then
                propName=`echo $propline | cut -d= -f1`
                #this trims whitespace
                propName=`echo ${propName/ /}`
                propNameUnderscored=`echo $propName | sed 's/\./_/g' | tr '[:lower:]' '[:upper:]'`
                echo "$propNameUnderscored(\"$propName\"),"
        fi
done < ./src/main/resources/serviceconfig/olat.properties