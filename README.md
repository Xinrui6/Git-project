# Git-project
 This is a version-control system that mimics some of the basic features of the popular system Git. The project is based on project 2 of the [Berkeley CS61B](https://sp21.datastructur.es/materials/proj/proj2/proj2) course. I implemented the `Main`, `Repository`, `Commit`, `Branches`, `Blob`. The functions in the Utils class and the python testing suit is provided by the course.
# Commands
Compiling the program with: 
```
javac gitlet/*.java  
```    
Running various git command with:
```
java gitlet.Main [command]
```
The gitlet supports Commands:
```
init 

add [file name]

commit [message]

rm [file name]

log

global-log

find [commit message]

status

checkout -- [file name]

checkout [commit id] -- [file name]

checkout [branch name]

branch [branch name]

rm-branch [branch name]

reset [commit id]

merge [branch name]
```
