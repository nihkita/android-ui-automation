cd app
start /wait c:\ant\bin\ant.bat build
pause
c:\users\stratmann\appdata\local\android\sdk\platform-tools\adb push bin\testautomator.jar /data/local/tmp
c:\users\stratmann\appdata\local\android\sdk\platform-tools\adb shell uiautomator runtest testautomator.jar -c at.runner.TestAutomator
cd..
ECHO c:\users\stratmann\appdata\local\android\sdk\platform-tools\adb pull /data/local/tmp/atrunner.log atrunner.log
c:\users\stratmann\appdata\local\android\sdk\platform-tools\adb pull /sdcard/ATRunner/atrunner.log
notepad.exe atrunner.log