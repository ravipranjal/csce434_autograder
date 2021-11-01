#!/usr/bin/python3
import json
import sys
import difflib
import re
from itertools import islice
sample_dir = "OutSample/"
out_dir = "Out/"
in_dir = "TestInputs/"
result_dir = sys.argv[1]
result_file = result_dir+"results.json"
num_tests = int(sys.argv[2])
visibility = [1,1,1,1,0,0,0,0,0,0,0,0,0,0,0]
comments = ["","","","",
            "Find maximum number in input",
            "Nth fibonacci number",
            "Factorial",
            "Create number from Input. Ex: Input: 1,2,3,4 :: Output: 1234",
            "Sum of first N natural numbers using tail recursion",
            "Print pyramid",
            "Count number of 7 in a number",
            "Sum of digits",
            "Calcuate power a^b",
            "Calculate cumulative sum",
            "Print in reverse order"
            ]

my_dict = { "score": 0, "output" : "", "tests": []}
for i in range(1,num_tests+1):
    sample_out_file = sample_dir+"out_"+str(i)
    out_file = out_dir+"out_"+str(i)
    in_file = in_dir+"tests_"+str(i)+'.in'

    sample_reg_file = sample_dir+"out_"+str(i)+"_reg"
    reg_file = out_dir+"out_"+str(i)+"_reg"
    wc_sample_reg_file = 0
    wc_reg_file = 0
    try:
        srf = open(sample_reg_file, "r") 
        rf = open(reg_file, "r")
        wc_sample_reg_file = len(srf.readlines())
        wc_reg_file = len(rf.readlines())
    except err:
        continue

    try:
        sample_file = open (sample_out_file, "r")
        file = open (out_file, "r")
    except err:
        continue

    sample_file_text=sample_file.read().splitlines(True)
    file_text=list(islice(file, 100))
    diff=difflib.unified_diff(sample_file_text,file_text)
    diff_text="".join(list(diff))
    points=10

    if diff_text:
        points=0
    
    change = abs(wc_sample_reg_file-wc_reg_file)/wc_sample_reg_file
    if change > .5:
        points=0
        diff_text += "\n instruction count misaligned"
        diff_text += " expected: "+ str(wc_sample_reg_file) + " got: " + str(wc_reg_file)
    
#    if i==12 or i==9: ## bad test case
#        f=re.compile(r'\b({0})\b'.format("error"), flags=re.IGNORECASE).search(str(file_text))
#        if f:
#            points=10
            
    sample_file.close()
    file.close()
    idx = i-1

    comment=""

    if(visibility[idx]==1):
        in_file = open(in_file, "r")
        test_file = open("Tests/test_"+str(i), "r")
        comment = "Input:\n"+in_file.read()+"\nTest:\n"+test_file.read()+"\nDiff:\n"+diff_text
        in_file.close()
        test_file.close()
    else:
        comment = comments[idx]+"\nDiff:\n"+diff_text

    if(points==0):
        err_file = open("Out/err_"+str(i)+"_filt", "r")
        comment = comment + "\nErrors:\n" + err_file.read()+"\n"

    my_temp_dict = { "score": points, "max_score": 10 , "output": comment, "name": "Test_"+str(i)}
    my_dict["tests"].append(my_temp_dict)
    my_dict["score"] += points

my_dict["score"] = min(100,my_dict["score"])
result = open(result_file, "w")
json.dump(my_dict, result, indent = 4)
result.close()


#print(my_dict)
############################################  Content of JSON file  ##############################################
# { "score": 44.0, // optional, but required if not on each test case below. Overrides total of tests if specified.
# "execution_time": 136, // optional, seconds
# "output": "Text relevant to the entire submission", // optional
# "visibility": "after_due_date", // Optional visibility setting
# "stdout_visibility": "visible", // Optional stdout visibility setting
# "extra_data": {}, // Optional extra data to be stored
# "tests": // Optional, but required if no top-level score
# [
#     {
#         "score": 2.0, // optional, but required if not on top level submission
# "max_score": 2.0, // optional
# "name": "Your name here", // optional
# "number": "1.1", // optional (will just be numbered in order of array if no number given)
# "output": "Giant multiline string that will be placed in a <pre> tag and collapsed by default", // optional
# "tags": ["tag1", "tag2", "tag3"], // optional
# "visibility": "visible", // Optional visibility setting
# "extra_data": {} // Optional extra data to be stored
# },
# // and more test cases...
# ],
# "leaderboard": // Optional, will set up leaderboards for these values
# [
#     {"name": "Accuracy", "value": .926},
#     {"name": "Time", "value": 15.1, "order": "asc"},
#     {"name": "Stars", "value": "*****"}
# ]
# }
############################################ Content of JSON file END #############################################
