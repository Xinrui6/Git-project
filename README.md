# Git-project
 This is a version-control system that mimics some of the basic features of the popular system Git. The project is based on project 2 of the [Berkeley CS61B](https://sp21.datastructur.es/materials/proj/proj2/proj2) course. I implemented the `Main`, `Repository`, `Commit`, `Branches`, `Blob`. The functions in the Utils class and the python testing suit is provided by the course.
# Before Using Gitlet:
Compiling the program with: 
```
javac gitlet/*.java  
```    
Running various git command with:
```
java gitlet.Main [command]
```
Make sure you initialise, creates a new Gitlet version-control system in the current directory with this command:
```
java gitlet.Main init
```
before using gitlet supports Commands
# Gitlet supports Commands:
```
add [file name]		
commit [message]		
rm [file name]		
log		
global-log		
status	
find [commit message]		
checkout -- [file name]		
checkout [commit id] -- [file name]		
checkout [branch name]		
branch [branch name]		
rm-branch [branch name]		
reset [commit id]		
merge [branch name]
```
# Persistence  
Since static variables do NOT persist in Java between executions, when a program completes execution, all instance and static variables are completely lost. Therefore, all branches, commits, blobs, heads and status of staging areas need to be stored on the file system:  
```
  Gitlet: storing all data
    - STAGING: staging files for commits.  
       - TO_ADD: hashmap for additional files.  
       - TO_REMOVE: hashmap for removal files.  
    - BRANCHES: storing all about branches.  
       - branch: the file for staging branches tree.  
       - currB: the active branch head's commit id.  
    - ALL: storing all Commits.  
    - head: storing the head of all commits.  

