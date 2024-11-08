/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package emulator;

import chip.Chip;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;

/**
 *
 * @author Moustafa
 */
public class Panel extends JPanel{
    private Chip chip;
    public Panel(Chip chip){
        this.chip = chip;
    }
    @Override
    public void paint(Graphics g){
        byte[] display = chip.getDisplay();
        for(int i = 0; i<display.length;i++){
            if(display[i]==0){
                g.setColor(Color.BLACK);
            }else{
                g.setColor(Color.WHITE);
            }
            int x = (i%64);
            int y = (int)Math.floor(i/64);
            
            g.fillRect(x*16, y*16, 16, 16);
        }
    }
}
