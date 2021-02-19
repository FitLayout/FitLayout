#! /bin/bash
CPWD=`pwd`
WDIR="/tmp/onto$$"
mkdir $WDIR
cd $WDIR
git clone git@github.com:FitLayout/FitLayout.github.io.git
cd FitLayout.github.io/ontology/
cp *.owl $CPWD
cd $CPWD
rm -rf $WDIR
