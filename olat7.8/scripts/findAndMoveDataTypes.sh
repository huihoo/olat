#!/bin/bash

#use the persistet object type names to find references to this files
while read type
do
  echo ""
  echo "------------------------------------------------"
  echo "Hits for $type"
  echo "------------------------------------------------"
  #search in all javaclasses
  while read class
  do
    grep -l $type $class
  done < "allJavaClassesWithClasspath.txt"
  
  #search in all hbm files
  while read hbmclass
  do
    grep -l $type $hbmclass
  done < "allHbmXmlFiles.txt"



echo "------------------------------------------------"
echo ""
done < "PersistentObjectNames.txt"