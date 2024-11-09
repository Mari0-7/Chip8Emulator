# Chip8Emulator
Chip8 Emulator (actually an interpreter), written in Java
You can give it a try on the go by downloading the .jar file and try some of the roms I've provided.

### Key Features:
#### 1. An emulator for the Chip8 written in java, meaning that it can run on any machine with the Java22SDK on it.
#### 2. A file navigator to choose your roms wherever you've stored them on your PC.

## How to use:
The keyboard of the original hardware was a hexadecimal keypad, and I've mapped the keys in the same format that they were, here's a visual representation of how it's mapped:
```
Original:                Keyboard:
1 2 3 C                  1 2 3 4
4 5 6 D                  Q W E R
7 8 9 E                  A S D F
A 0 B F                  Z X C V
```
Regarding the emulator itself, you can either run the .jar and you'll prompted with a file navigator to choose a rom, or you can import the project and run it from a Java IDE with a maven plugin.

I've added a txt file with the controls for each game so you'll know how to navigate.

It's pretty straightforward, and I've tried to comment and document all of what's happening behind the scenes on each line in my source code.

### P.S
if you feel like the emulator is a little too fast, just change the **Thread.sleep()** value from **8** to **16**, which would be more inline to the original hardware but it was too slow for me to actively engage with the games.
