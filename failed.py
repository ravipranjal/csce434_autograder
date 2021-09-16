#!/usr/bin/python3
import json
import sys
sample_dir = "OutSample/"
out_dir = "Out/"
result_dir = sys.argv[1]
result_file = result_dir+"results.json"
num_tests = int(sys.argv[2])

my_dict = { "score": 0, "output" : "NA", "tests": []}
for i in range(1,num_tests+1):
    my_temp_dict = { "score": 0, "max_score": 10 , "output": "Failed to compile"}
    my_dict["tests"].append(my_temp_dict)

result = open(result_file, "w")
json.dump(my_dict, result, indent = 4)
result.close()
