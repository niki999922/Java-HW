SET MAINDIRECTORY=%CD%
SET ARTIFACTS=artifacts\*
SET OUT="out"

MD out

@rem javac -cp %ARTIFACTS%; -d %OUT% ru\ifmo\rain\kochetkov\%2\*.java