SET MAINDIRECTORY=C:\Users\Nikita\IdeaProjects\Java_advanced
SET DATA=src\info\kgeorgiy\java\advanced\implementor\
SET LINK=https://docs.oracle.com/javase/10/docs/api/
SET PACKAGE=ru.ifmo.rain.kochetkov.implementor
SET STARTDIR=%CD%

cd %MAINDIRECTORY%
javadoc -cp src\; -link %LINK%   -private -author -version %PACKAGE% -d javadoc %DATA%Impler.java %DATA%JarImpler.java %DATA%ImplerException.java

cd %STARTDIR%