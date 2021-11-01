#!/usr/bin/bash

y="adiv|ans|base|cSum|call|checkForSanity|cnt|count|countSeven|cumulativeSum|curr|diff|div|else|exponent|factIter|factRec|fibonacci|function|getNumberFromInput|getSumToN|getTwoSum|input|inputnum|let|main|max|maxNumberFromInput|mul|myVar|next|num|outputnewline|outputnum|power|printNum|printReverse|pwr|return|round|sum|sumDigits|tailRecurSum|temp|then|units|var|while"

for ((i=1;i<=15;i++));
do 
    count=$(egrep $y Out/out_${i} | wc -l)
    if (($count > 0))
    then
        echo "Possible input print attempt" > Out/out_${i}
    fi
done 
