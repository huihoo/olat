#!/usr/bin/perl

$num_args = $#ARGV + 1;
$olatDirectory = ".";
$language = "de";

if ($num_args < 2 ) {
	print "\n usage: ./extract-changed-i18-keys.pl oldRevisionNumber newRevisionNumber [olatDirectory] [language]\n";
	print " *** olatDirectory = ".$olatDirectory." [default value]\n";
	print " *** language = ".$language." [default value]\n";
	exit;
}

$oldRevision = $ARGV[0];
$newRevision = $ARGV[1];
$resultFile = "ChangedKeywordsBetweenRevisions_".$oldRevision."-".$newRevision.".txt";
$rawResultFile = "ChangedKeywordsBetweenRevisions_".$oldRevision."-".$newRevision.".raw";

if ($ARGV[2]){
    $olatDirectory = $ARGV[2];
}

if ($ARGV[3]){
    $language = $ARGV[3];
}


if ( !($oldRevision =~ /^\d+$/) ){
    print "usage: Old revision number (first parameter) is not a valid revision number;\n";
	exit;
}

if ( !($newRevision =~ /^\d+$/) ){
	print "usage: New revision number (second parameter) is not a valid revision number;\n";
	exit;
}

print("\nSearching for '*_".$language.".properties\' files and changes between revisions ".$oldRevision." and ".$newRevision."...\n\n. It will take a while.");
$findCommand="find ".$olatDirectory." -name \'*_".$language.".properties\' -exec hg diff -r ".$oldRevision." -r ".$newRevision." {} \\; > ".$rawResultFile;
#windows version: $findCommand="find ".$olatDirectory." -name \'*_".$language.".properties\' -exec hg diff -r ".$oldRevision." -r ".$newRevision." {} \; > ".$rawResultFile; 

system( $findCommand );

open (InputFile, $rawResultFile);
open (ResultFile, ">>".$resultFile);

while (<InputFile>) {
 	chomp;
 	$bline = "$_\n";
 	$line = "$_\n";
 	
 	# remove lines that start with ---, +++, @@ or space
 	$line =~ s/---.*\r?\n?//;
    $line =~ s/\+\+\+.*\r?\n?//;
    $line =~ s/@@.*\r?\n?//;
    $line =~ s/^\s.*\r?\n?//;
    
    # extract package path and decorate it
    $line =~ s/(diff.*src)(.*)(_i18n.*)/src\2/;
    $line =~ s/(src.*)/\n\npackage: \1\n------/;
    
    if ($line ne ""){
        print ResultFile $line;
    }
}
close (InputFile); 
close (ResultFile); 


print "\n\n DONE: Check ".$resultFile." for changed keywords. \n";
print " *** Symbol \"-keyword\" followed by \"+keyword\" indicates that text associated with the keyword has changed. \n";
print " *** Unique symbol \"-keyword\" indicates that the keyword is deleted. \n";
print " *** Unique symbol \"+keyword\" indicates that the keyword is newly added. \n";

print "\nThe unfiltered find/diff result is stored in ".$resultFile."_res .\n\n";


