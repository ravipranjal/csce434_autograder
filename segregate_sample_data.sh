#!/usr/bin/bash

 for ((i=1;i<=15;i++)); 
 do 
    cat OutSample_bk/out_${i} | grep " , " > OutSample/out_${i}_reg; 
    cat OutSample_bk/out_${i} | grep -v " , " > OutSample/out_${i}; 
 done 
