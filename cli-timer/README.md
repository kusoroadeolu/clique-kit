# CLI Timer
A simple CLI timer built with Clique. Currently only supports Window machines.

## Setup
1. Build the module
```bash
mvn clean package
```

2. Create a bat file named `timer.bat` with this content
```bash
@echo off

set CONFIG=C:\.cli-timer\config.json
set JAR=C:\.cli-timer\cli-timer.jar

if not exist "%CONFIG%" (
    if not exist "C:\.cli-timer\" mkdir "C:\.cli-timer"
    echo {} > "%CONFIG%"
)

java -jar "%JAR%"
```

3. Add `timer.bat` to your PATH:
    - Press **Win + S** and search "environment variables"
    - Click **"Environment Variables"** at the bottom of the window
    - Under **User variables**, find `Path` and double-click it
    - Click **New** and paste the folder path where `timer.bat` is saved (e.g. `C:\Users\you\scripts`)
    - Hit OK on everything

4. Open a terminal and run `timer` — you'll get a jar not found error, but that's fine. It will have created the `C:\.cli-timer\` folder and config file.

5. Copy the built jar from your module's `target` folder into `C:\.cli-timer\`

6. Run `timer` again and you should be up and running!

## Configuration
The config file lives at `C:\.cli-timer\config.json`. Edit it to customize your timer:
```json
{
  "time": {
    "hours": "1",
    "minutes": "0",
    "seconds": "0"
  },
  "audioPath": "path_to_your_audio_file",
  "title": "Live Timer"
}
```

- **Time** — Hours, minutes, and seconds for your timer. Note that minutes and seconds cannot exceed 60
- **Title** — The title displayed at the top of your timer
- **Audio Path** — Audio file to play when the timer completes. Can be left blank for no audio