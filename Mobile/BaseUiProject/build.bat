cd app
start /wait c:\ant\bin\ant.bat build
pause
c:\users\stratmann\appdata\local\android\sdk\platform-tools\adb push bin\testautomator.jar /data/local/tmp
c:\users\stratmann\appdata\local\android\sdk\platform-tools\adb shell uiautomator runtest testautomator.jar -c at.runner.TestAutomator