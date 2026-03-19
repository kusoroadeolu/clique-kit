@echo off

set CONFIG=C:\.cli-timer\config.json
set JAR=C:\.cli-timer\cli-timer.jar

if not exist "%CONFIG%" (
    if not exist "C:\.cli-timer\" mkdir "C:\.cli-timer"
    echo {} > "%CONFIG%"
)

java -jar "%JAR%"