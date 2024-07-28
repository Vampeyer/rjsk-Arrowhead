# rjsk-Arrowhead
RskJ is a Java implementation of the Rootstock node.  --- Rebuilt with Gradle 8.8

--- Main Notes ----- 
- The Gradle 7.x.x build in the current rjsk node repository  , was causing a major build break issue.  
- I had tried and repeated the process several times , with stil an error to build and run a node.  

- So I have decided to make my entry to the hackathon a big bug build fix for rootstock. 



---- Notes -----  
- The Gradle 7.x.x build in the current rjsk node repository  , was causing a major build break issue. 
- Solid documentation for the break process and rebuilt  ,  fix instructions are provided as well.   
- Installed Gradle CLI , on local  Dev env and updated Gradle Wrapper to Gradle 8.8 
- Re-engineered file system during the process with new API gradle wrappers. 



- Checked build with Local Gradle CLI - gradle build
- Checked build with gradlew file   - gradlew build         /                     ./gradlew build  

This project can be much more easily maintained , and if any one else needs to upgrade the gradle wrapper again for any reason , 
I tried to provde sufficient documentation to start to assist. 


The final project redacts some uneccesary config code , and should provide a more 
stable environment for DRY principles , and overall easability. 

--- Edit as  needed , just give me my credit_ please and thank you. 
____________________________________________________________________________________
___________________Videos ______________________________________
________________________________________________________________

Broken trying to run  - 

![GRADLE-WRAPPER-BUILD-breaking-BUG](https://github.com/user-attachments/assets/a274c6f2-43da-498a-963a-f98be6508deb)

This working repo , running a good  consistent build. _______

![GRADLE-BUILD-FIX](https://github.com/user-attachments/assets/30b8db29-b3d4-44b8-b835-a714f90e56e4)


--- And I think the .bat file works as well on windows. 



