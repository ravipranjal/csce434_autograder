#!/usr/bin/bash


for ((i=1;i<=15;i++));
do 
    cat Out/err_${i} | grep -A10 "Exception in" | head -100  > Out/err_${i}_filt
done 
