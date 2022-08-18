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
**The gitlet supports Commands:**
`init`: java gitlet.Main init   
`add`: java gitlet.Main add [file name]
`commit`: java gitlet.Main commit [message]
`rm`: java gitlet.Main rm [file name]
`log`: java gitlet.Main log
`global-log`: java gitlet.Main global-log
`find`: java gitlet.Main find [commit message]
`status`: java gitlet.Main status
`checkout file`: java gitlet.Main checkout -- [file name]
`checkout commit`: java gitlet.Main checkout [commit id] -- [file name]
`checkout branch`: java gitlet.Main checkout [branch name]
`branch`: java gitlet.Main branch [branch name]
`rm-branch`: java gitlet.Main rm-branch [branch name]
`reset`: java gitlet.Main reset [commit id]
`merge`: java gitlet.Main merge [branch name]

