#!/bin/bash

# This script was used to move the i18n files from its own projects back to the
# main olat project
# @autor: Guido Schnider

olat3Path="./olat3/webapp/WEB-INF/src/"

olatcorePath="./olatcore/src/main/java/"

olat3_i18n="olat3_i18n/src/main/java/"
olat3_i18nLength=`expr length $olat3_i18n + 2`

olatcore_i18n="olatcore_i18n/src/main/java/"
olatcore_i18nLength=`expr length $olatcore_i18n + 2`


localStrLength=`expr length 'LocalStrings_de.properties'`

for f in `find . -name LocalStrings_*.properties`
do
  #skip _de. _en. files
  if [ `grep '_de.properties' <<< $f | wc -l` -eq 0 ];then
    if [ `grep 'olat3_i18n' <<< $f | wc -l` -eq 1 ];then
      #is an olat3_ non default lang files
      echo "-------------------------------"
      echo $f
      pathLength=`expr length $f`
      restOfPath=${f:$olat3_i18nLength:$pathLength}
      echo "would move to"
      echo "$olat3Path$restOfPath"
      mv $f $olat3Path$restOfPath
      echo "-------------------------------"
      
    fi
  fi
  
  if [ `grep '_en.properties' <<< $f | wc -l` -eq 0 ];then
    if [ `grep 'olatcore_i18n' <<< $f | wc -l` -eq 1 ];then
      #is an olat3_ non default lang files
      echo "-------------------------------"
      echo $f
      pathLength=`expr length $f`
      restOfPath=${f:$olatcore_i18nLength:$pathLength}
      echo "would move to"
      echo "$olatcorePath$restOfPath"
      mv $f $olatcorePath$restOfPath
      echo "-------------------------------"
      
    fi
  fi
done
exit 0