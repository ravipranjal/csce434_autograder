#!/usr/bin/bash

 for ((i=1;i<=15;i++)); 
 do 
    cat Out_bk/out_${i} | grep " , " > Out/out_${i}_reg; 
    cat Out_bk/out_${i} | grep -v " , " > Out/out_${i}; 
 done 
