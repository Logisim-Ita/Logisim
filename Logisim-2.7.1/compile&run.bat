dir /s /B *.java > sources.txt
javac @sources.txt
del "sources.txt"
set zdir="E:\Program Files\7-Zip\7z.exe"
for %%* in (.) do set currentfolder=%%~nx*
%zdir% a -tzip ..\"%currentfolder%".jar *\ COPYING.txt
cd ../
java -jar "%currentfolder%".jar -plain