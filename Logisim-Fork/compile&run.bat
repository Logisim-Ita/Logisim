dir /s /B *.java > src.txt
set path="C:\Program Files\Java\jdk1.8.0_25\bin"
javac @src.txt
del "src.txt"
set zdir="C:\Program Files\7-Zip\7z.exe"
for %%* in (.) do set currentfolder=%%~nx*
%zdir% a -tzip ..\Compiled\"%currentfolder%".jar *\ COPYING.txt
cd ../Compiled/
java -jar "%currentfolder%".jar -plain