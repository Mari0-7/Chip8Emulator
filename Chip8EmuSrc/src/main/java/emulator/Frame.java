/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package emulator;

import chip.Chip;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.*;

/**
 *
 * @author Moustafa
 */
public class Frame extends JFrame implements KeyListener, ActionListener{
    private Panel panel;
    private int[] keyBuffer;//actual key state, assigned at each operation
    private int[] keyID;
    public Frame(Chip c){
        setPreferredSize(new Dimension(1024,512));
        pack();
       setPreferredSize(new Dimension(1024+getInsets().left + getInsets().right,512+getInsets().top+getInsets().bottom));
         panel = new Panel(c);
         setLayout(new BorderLayout());
         add(panel,BorderLayout.CENTER);
         setTitle("Chip8 emu trial1");
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         pack();
         setVisible(true);
         addKeyListener(this);
         
         
         keyID = new int[256];
         keyBuffer = new int[16];
         fillKeyIDs();
    }
    private void fillKeyIDs(){
        for(int i = 0; i<keyID.length;i++){
            keyID[i] = -1;//sets all keys as -1 as a default, meaning it hasn't been pressed
        }
        keyID['1'] = 1;
        keyID['2'] = 2;
        keyID['3'] = 3;
        keyID['Q'] = 4;
        keyID['W'] = 5;
        keyID['E'] = 6;
        keyID['A'] = 7;
        keyID['S'] = 8;
        keyID['D'] = 9;
        keyID['Z'] = 0xA;
        keyID['X'] = 0;
        keyID['C'] = 0xB;
        keyID['4'] = 0xC;
        keyID['R'] = 0xD;
        keyID['F'] = 0xE;
        keyID['V'] = 0xF;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(keyID[e.getKeyCode()]!=-1){//checks if a key has been pressed, therefore, it won't be -1
            keyBuffer[keyID[e.getKeyCode()]]=1;//if it's pressed it'll set it to 1
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if(keyID[e.getKeyCode()]!=-1){//Released key/any unused key is set to 0
            keyBuffer[keyID[e.getKeyCode()]]=0;
        }
    }

    public int[] getKeyBuffer() {
        return keyBuffer;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }
    
}
