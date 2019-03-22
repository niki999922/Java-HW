SET MAINDIRECTORY=C:\Users\Nikita\IdeaProjects\Java_advanced
SET ARTIFACTS=out\production\Java_advanced\java-advanced-2019\artifacts\*
SET OUT=out\production\Java_advanced
SET SOURCE=src\ru\ifmo\rain\kochetkov\implementor\Implementor.java
SET MANIFEST=%CD%\Manifest.txt
SET STARTDIR=%CD%

cd %MAINDIRECTORY%
javac -cp %ARTIFACTS%; -d %OUT% %SOURCE%

cd %OUT%
MD Jar
cd Jar
jar cfm Implementor.jar %MANIFEST% ..\ru\ifmo\rain\kochetkov\implementor\*.class

cd %STARTDIR%