dir /s /B *.java > src.txt
set path="C:\Program Files\Java\jdk1.8.0_121\bin"
javac @src.txt
del "src.txt"
set zdir="E:\Program Files\7-Zip\7z.exe"
for %%* in (.) do set currentfolder=%%~nx*
%zdir% a -tzip ..\..\"%currentfolder%".jar *\ COPYING.txt
cd ../../
java -jar "%currentfolder%".jar -plain