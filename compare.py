#!/usr/bin/python3
import json
import sys
import difflib
import re
sample_dir = "OutSample/"
out_dir = "Out/"
in_dir = "TestInputs/"
result_dir = sys.argv[1]
result_file = result_dir+"results.json"
num_tests = int(sys.argv[2])
visibility = [1,1,1,1,0,0,0,0,0,0,0,1,0,0,0]
comments = ["","","","",
            "This test case tests expression evaluation",
            "This test case tests if statement evaluation",
            "This test case tests nested if statement evaluation with comments",
            "This test case uses while,for,outputnum as variables",
            "",
            "This test case changes variable values based inside if condition",
            "This test case changes variable values based inside if condition",
            "Bad program - Error expected",
            "This test case is complex and is a mixture of everything",
            "This test case is complex and is a mixture of everything",
            "This test case is complex and is a mixture of everything",
            ]

my_dict = { "score": 0, "output" : "", "tests": []}
for i in range(1,num_tests+1):
    sample_out_file = sample_dir+"out_"+str(i)
    out_file = out_dir+"out_"+str(i)
    in_file = in_dir+"tests_"+str(i)+'.in'
    try:
        sample_file = open (sample_out_file, "r")
        file = open (out_file, "r")
    except err:
        break

    sample_file_text=sample_file.read().splitlines(True)
    file_text=file.read().splitlines(True)
    diff=difflib.unified_diff(sample_file_text,file_text)
    diff_text="".join(list(diff))
    points=10
    if diff_text:
        points=0

    if i==12: ## bad test case
        f=re.compile(r'\b({0})\b'.format("error"), flags=re.IGNORECASE).search(str(file_text))
        if f:
            #print("found " + str(i))
            points=10
            
    #for line in difflib.unified_diff(sample_file_text,file_text):
    #    print(line)
        
        #points=0

    #sample_file_line = sample_file.readline()
    #file_line = file.readline()
    #points = 0
    #total_points = 0;
    #while sample_file_line != '' and file_line != '':
    #    total_points += 1
    #    sample_file_line = sample_file_line.rstrip()
    #    file_line = file_line.rstrip()
    #    if sample_file_line == file_line:
    #        points += 1
    #    sample_file_line = sample_file.readline()
    #    file_line = file.readline()
    #while sample_file_line != '':
    #    total_points += 1
    #    sample_file_line = sample_file.readline()
    sample_file.close()
    file.close()
    idx = i-1
    #if(points == total_points):
    #        points = 10
    #else:
    #        points = 0

    comment=""

    if(visibility[idx]==1):
        #sample_file = open (sample_out_file, "r")
        #file = open (out_file, "r")
        in_file = open(in_file, "r")
        test_file = open("Tests/test_"+str(i), "r")
        #comment = "Input:\n"+in_file.read()+"\n\nExpected:\n"+sample_file.read()+"\n\nGot:\n"+file.read()
        comment = "Input:\n"+in_file.read()+"\nTest:\n"+test_file.read()+"\nDiff:\n"+diff_text
        #sample_file.close()
        #file.close()
        in_file.close()
        test_file.close()
    else:
        comment = comments[idx]
    #if(points==10.0):
    #        comment = comments[idx] + " Well done!"
    my_temp_dict = { "score": points, "max_score": 10 , "output": comment, "name": "Test_"+str(i)}
    my_dict["tests"].append(my_temp_dict)
    my_dict["score"] += points

#my_dict["score"] = min(100,my_dict["score"])
result = open(result_file, "w")
json.dump(my_dict, result, indent = 4)
result.close()
print(my_dict)
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
