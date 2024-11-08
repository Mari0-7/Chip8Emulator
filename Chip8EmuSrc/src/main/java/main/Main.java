/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package main;

import chip.Chip;
import emulator.Frame;
import javax.swing.*;
import java.io.File;

/**
 *
 * @author Moustafa
 */
public class Main extends Thread {
    private Chip chip8;
    private Frame frame;
     String filePath = fileExplorer();
    public Main(){ // organisation
          chip8 = new Chip();
        chip8.init();
        
        chip8.loadProgram(filePath);
        frame = new Frame(chip8);
    }
    public void run(){ // basic loop for the thread
        // the emu runs at 60Hz, which translates to 60FPS
        while(true){
            chip8.setKeyBuffer(frame.getKeyBuffer());
            chip8.run();
            if(chip8.needsRedraw()){ //if we need to redraw the screen
                frame.repaint();
                chip8.removeDrawFlag();
            }
            try{ // frame will update if there's been a change
            Thread.sleep(8);
            }catch(InterruptedException e){
                /*
                While it's declared, since we won't be altering or interrupting any of the threads, an exception is very unlikely to occur.
                However, to stay true to the original system, it didn't have the power to handle the changes of frames, and threads may be interrupted which would make sense to handle such exception
                */
            }
        }
    }
      public static String fileExplorer() {
        //file explorer to choose the roms
        JFileChooser fileChooser = new JFileChooser();
        
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Chip8 roms", "c8", "ch8"));
        
        int returnValue = fileChooser.showOpenDialog(null);
        
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            return selectedFile.getAbsolutePath();
        } else {
            return null;
        }
    }

    public static void main(String[] args) {
      Main main = new Main(); //storing and accessing the main functions
      main.start();
    }
}
