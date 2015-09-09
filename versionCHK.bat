@echo off
cd build
"c:\Program Files\Java\jdk1.7.0_05\BIN\javap.exe" -verbose com/rapidminer/PluginInitPRules >> ..\wynik.txt
cd ..
@echo on
@type wynik.txt | grep "major version"
@echo off
del wynik.txt
pause







