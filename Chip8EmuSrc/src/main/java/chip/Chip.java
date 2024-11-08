/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package chip;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.event.*;
import java.util.Random;

/**
 *
 * @author Moustafa
 */
public class Chip{
    private static final int WIDTH_OF_SCREEN = 64;
    //4kbytes of memory, 1024bytes each
    private char[] memory;
    //the chip has 16 registers
    private char[] V;
    //address pointer, 16 bits but only 12 in use
    private char I;
    //program counter, assigned at 200hex after the part of memory that the system occupies
    private char pc;
    //pointer stack, the stack can only call up to 16 routines/subroutines, therefore, 16 bits
    private char stack[];
    //points to the next free slot in the stack
    private int stackpointer;
    //to ensure 60 FPS, we add a delaytimer that ticks at 60Hz, 
    private int delay_timer;
    // beep sound
    private int sound_timer;
    //the keyboard, it was a hexadecimal 16-character keyboard
    private byte[] key;
    //display, resolution according to docs is 64*32
    private byte[] display;
    //if the frame needs a redraw
    private boolean needRedraw;

    public void init(){
        memory = new char[4096];
        V = new char[16];
        I=0x0;
        pc = 0x200;
        stack = new char[16];
        stackpointer = 0;
        
        delay_timer = 0;
        sound_timer = 0;
        key = new byte[16];
        display = new byte[64*32];
        needRedraw = false;
        loadFontSet();
    }
   public void run(){
       //fetch opcodes
       char opcode = (char)((memory[pc] << 8) | memory[pc+1]);
       System.out.print(Integer.toHexString(opcode).toUpperCase()+": ");
       //decode opcodes
       switch(opcode & 0xF000){ //uses the AND operation on the first nibble to determine the opcode, multiple opcodes share the first nibble
           //The reason we used the bitwise AND along with "F" as the value to mask, is because F is composed of 1111, and by using the AND operator
           //It'll transfer everything that gives an ON state value (1&1 = 1) and anything else would be disregarder (0&1 = 0), and the rest of the nibbles
           //are being AND'ed with zero so it'll be dicarded as well, (1&0 = 0)
           case 0x0000:
               switch(opcode&0x00FF){
                   case 0x00E0://00E0: Clears the screen
                       for(int index1 = 0; index1<display.length; index1++){
                           display[index1] = 0;
                       }
                       pc+=2;
                       needRedraw = true;
                       break;
                   case 0x00EE://00EE: Return from the subroutine
                       stackpointer--;
                       pc = (char)(stack[stackpointer]+2);//return the pointer to the last subroutine stored on the stack
                       System.out.println("Returning to " + Integer.toHexString(pc).toUpperCase());
                       break;
                   default://0NNN: calls RCA 1802 program at address NNN, usually written in machine code of the actual chip, so I'll not handle it
                       System.err.println("Opcode unsupoorted");
                       System.exit(0);
                       break;
               }
               break;
           case 0x1000: //1NNN: jump to address NNN
               int nnn1 = (opcode & 0x0FFF);
               pc = (char)nnn1;
               System.out.println("Jumping to " + Integer.toHexString(pc).toUpperCase());
               break;
            case 0x2000://2NNN: Calls a subroutine at address NNN
               //get address
                char address = (char)(opcode & 0x0FFF); //to extract the address at NNN using the last 3 nibbles
                //store your current address to the stack
               stack[stackpointer] = pc;
                stackpointer++; //move the pointer to avoid overwriting
                pc = address;//move to the desired address for the subroutine
                System.out.println("Calling " + Integer.toHexString(pc).toUpperCase() + " from " + Integer.toHexString(stack[stackpointer - 1]).toUpperCase());
               break;
            case 0x3000://3XNN: Skip next instruction if VX = NN
                int x3 = ((opcode & 0x0F00)>>8);
                int nn3 = (opcode & 0x00FF);
                if(V[x3] == nn3){
                    pc+=4;
                    System.out.println("Skipping next instruction (V[" + x3 +"] == " + nn3 + ")");
                }else{
                    pc+=2;
                    System.out.println("Not skipping next instruction (V[" + x3 +"] != " + nn3 + ")");
                }
                
               break;
            case 0x4000://4XNN: skip if V[X] != NN;
                int x4 = ((opcode & 0x0F00)>>8);
                int nn4 = (opcode & 0x00FF);
                if(V[x4] != nn4){
                    pc+=4;
                }else{
                    pc+=2;
                }
                
               break;
            case 0x5000://5XY0: skip if V[X] == V[y]
                int x5 = ((opcode & 0x0F00)>>8);
                int y5 = ((opcode & 0x00F0)>>4);
                if(V[x5] == V[y5]){
                    pc+=4;
                }else{
                    pc+=2;
                }
                break;
            case 0x6000://6XNN set VX to NN
                int x6 = (opcode& 0x0F00) >> 8; // get index for the registry, then shift it by 8 bits
                V[x6] = (char)(opcode&0x00FF);//get NN and set it to VX
                pc +=2; //move the PC by 2 bytes, which is eequal to 1 opcode
                System.out.println("Setting V[" + x6 + "] to " + (int)V[x6]);
                break;
            case 0x7000://7XNN: Adds NN to VX
                int x7 = (opcode & 0x0F00) >> 8;
                int nn= (opcode&0x00FF);
                V[x7] = (char)((V[x7]+nn)&0xFF);// Add then and with 0xFF to prevent it from overflowing
                pc += 2;
                System.out.println("Adding " + nn + " to V["+ x7 + "] = " + (int)V[x7]);
               break;
           case 0x8000: //Contains more data in the last nibble
               switch(opcode & 0x000F){ // uses the AND operation with the last bibble to determine the action
                   case 0x0000:{//8XY0: set VX to value of VY
                    int x8 = (opcode & 0x0F00)>>8;
                    int y8 = (opcode & 0x00F0)>>4;
                    V[x8] = V[y8];
                    pc+=2;
                    break;
                   }
                   case 0x0001:{//8XY1: set VX to value of binary disjunction (bitwise OR) between VX and VY
                    int x8 = (opcode & 0x0F00)>>8;
                    int y8 = (opcode & 0x00F0)>>4;
                    V[x8] = (char)((V[x8] | V[y8])&0xFF);
                    pc+=2;
                    break;
                   }
                   case 0x0002:{//8XY1: set VX to value of binary conjunction(bitwise AND) between VX and VY
                    int x8 = (opcode & 0x0F00)>>8;
                    int y8 = (opcode & 0x00F0)>>4;
                    V[x8] = (char)(V[x8] & V[y8]);
                    pc+=2;
                    break;
                   }
                   case 0x0003:{//8XY1: set VX to value of binary exclusive OR (bitwise XOR) between VX and VY
                    int x8 = (opcode & 0x0F00)>>8;
                    int y8 = (opcode & 0x00F0)>>4;
                     V[x8] = (char)((V[x8] ^ V[y8])&0xFF);
                    pc+=2;
                    break;
                   }
                   case 0x0004:{//8XY4: set VX to value of VX + VY; however, if the sum is over 255 (which would overflow) then set the flag V[F] to true
                    int x8 = (opcode & 0x0F00)>>8;
                    int y8 = (opcode & 0x00F0)>>4;
                    if((V[x8]+V[y8])>255){
                         V[0xF] = 1;
                    }else{
                        V[0xF]=0;
                    }
                    V[x8] = (char)((V[x8] + V[y8])&0xFF);
                    pc+=2;
                    break;
                   }
                   case 0x0006:{//8XY6: shift VX by 1 bit to the right, and set V[F] as the Least Significant Bit (LSB) of VX
                    int x8 = (opcode & 0x0F00)>>8;
                   V[0xF] = (char)(V[x8]&0x1); 
                    V[x8] = (char) (V[x8]>>1);
                    pc+=2;
                    break;
                   }
                   case 0x0005:{//8XY1: set VX to value of VX -VY
                    int x8 = (opcode & 0x0F00)>>8;
                    int y8 = (opcode & 0x00F0)>>4;
                    V[0xF] = 1;
                    if(V[y8]>V[x8]){
                        V[0xF] = 0;
                    }else{
                        V[0xF] = 1;
                    }
                    V[x8] = (char)((V[x8] - V[y8])&0xFF);//we AND it with 0xFF to ensure it doesn't exceed the byte size
                    pc+=2;
                    break;
                   }
                   case 0x0007:{//8XY1: set VX to value of VY -VX
                    int x8 = (opcode & 0x0F00)>>8;
                    int y8 = (opcode & 0x00F0)>>4;
                    V[0xF] = 1;
                    if(V[x8]>V[y8]){
                        V[0xF] = 0;
                        
                    }else{
                      V[0xF] = 1;
                    }
                    V[x8] = (char)((V[y8] - V[x8])&0xFF);
                    pc+=2;
                    break;
                   }
                   case 0x000E:{//8XYE:  shift VX by 1 bit to the left, and set V[F] as the Most Significant Bit (MSB) of VX
                    int x8 = (opcode & 0x0F00)>>8;
                   V[0xF] = (char)(V[x8]&0x80); //0x80, which is 256 the max byte size, would allow use to access the MSB
                    V[x8] = (char) (V[x8]<<1);
                    pc+=2;
                    break;
                   }
                   default: System.err.println("OPCODE not supported");
                    System.exit(0);
                    break;
               }
               break;
           case 0x9000://9XY0: skip if VX != VY
               int x9 = ((opcode & 0x0F00)>>8);
                int y9 = ((opcode & 0x00F0)>>4);
                if(V[x9] != V[y9]){
                    pc+=4;
                }else{
                    pc+=2;
                }
                break;
           case 0xA000:// ANNN: set I to NNN
               char address1 = (char)(opcode & 0x0FFF); //to extract the address at NNN using the last 3 nibbles
               I = address1;// Assign the address to the address pointer I
               pc +=2;//moving the PC
               System.out.println("Set I to " + Integer.toHexString(I).toUpperCase());
               break;
           case 0xB000:
               int nnnB = (opcode & 0x0FFF);
               int addition = V[0]&0xFF;//We AND it with 0xFF(since it's a char, we used 0xFF) to ensure we avoid exceeding the byte size and avoid any sign issues
               pc+= (char)(nnnB + addition);
               break;
           case 0xC000://CXNN: gets a random number, then binary ANDs it with NN. then store it in VX
               int xc=(opcode&0x0F00)>>8;
               int nnC=(opcode&0x00FF);
               int randomNum = new Random().nextInt(256)&nnC;//the range is 0 to 255, the number specified (256) is excluded from the ranger
               System.out.println("V[" + xc + "] has been set to (randomised) " + randomNum);
               V[xc]= (char)randomNum;
               pc+=2;
               break;
           case 0xD000://DXYN: draw a sprite (X,Y), size (8,N). Sprite is located at I. 8 is a default width because 8pixels are stored in a byte, N is height
               //1: Draw by XOR-ing to the screen
               //2:Check for collisions and set it to V[0xF]
               //3:Read sprite located at I
               int xD = V[(opcode & 0x0F00)>>8];
               int yD = V[(opcode & 0x00F0)>>4];
               int height = opcode & 0x000F;
               
               V[0xF] = 0; //initialise the collision flag to false
               
               for(int _y = 0; _y<height; _y++){
                   int line = memory[I+_y];//the line to read from
                   for(int _x = 0; _x <8;_x++){
                      int pixel = line &(0x80>>_x);
                       if(pixel != 0){
                       int totalX= xD + _x;
                       int totalY = yD + _y;
                       totalX %= 64;//to ensure screen wrapping, we get the modulus of X for each iteration to ensure it's not larger than 64
                       totalY %= 32;//The same will be done to Y as well, but using 32 instead
                       int index =  (totalY * 64) + totalX; //the index at which we'll display the pixels, we multiply by 64 
                       // because that's the standard width of the screen, it multiplies accordingly to each line
                       if(display[index]==1)
                           V[0xF] = 1;// if the pixel at said index is ON, then apply collision
                       
                           display[index]^= 1;
                       }
                   }
               }
               pc +=2;
               needRedraw = true;
               System.out.println("Drawing at V[" + ((opcode & 0x0F00) >> 8) + "] = " + xD + ", V[" + ((opcode & 0x00F0) >> 4) + "] = " + yD);
               break;
            case 0xE000:// EXNN: set I to NNN
               switch(opcode & 0x00FF){
                   case 0x009E:{//EX9E: Skip instruction if key at VX is pressed
                       int xE = (opcode&0x0F00)>>8;
                       int keyE = V[xE];
                       if(key[keyE]==1){
                           pc+=4;
                       }else{
                           pc+=2;
                       }
                       break;
                   }
                   case 0x00A1:{//EXA1: Skip instruction if key at VX is not pressed
                       int xE = (opcode&0x0F00)>>8;
                       int keyE = V[xE];
                       if(key[keyE]==0){
                           pc+=4;
                       }else{
                           pc+=2;
                       }
                       break;
                   }
                   default: System.err.println("No more E codes");
                    System.exit(0);
                    break;
                       
               }
               break;   
           case 0xF000: //multi-commands
               switch(opcode & 0x00FF){
                   case 0x0033:{//FX33: store decimal value from VX into I,I+1,I+2
                       int xf = (opcode&0x0F00)>>8;
                       int num;
                       int[] nums = new int[3];
                       num = V[xf];
                       nums[0] = num/100;
                       nums[1] = (num/10)%10;
                       nums[2] = num%10;
                       for(int i = 0; i<3;i++){
                           memory[I+i] = (char) nums[i];
                       }
                       System.out.println("Storing Binary-Coded Decimal V[" + xf + "] = " + (int)(V[(opcode & 0x0F00) >> 8]) + " as { " + nums[0]+ ", " + nums[1] + ", " + nums[2] + "}");
                       pc+=2;
                       break;
                   }
                    case 0x0015:{
                       int xf = (opcode&0x0F00)>>8;
                       delay_timer =  V[xf];
                       pc+=2;
                       System.out.println("Set delay_timer to V[" + xf + "] = " + (int)V[xf]);
                       break;
                   }
                   case 0x0055:{//FX55: store memory from the register V[0] through VX starting at memory address I through I+X
                       int xf = (opcode&0x0F00)>>8;
                       char tempo = I;
                       for(int i = 0; i<=xf;i++){
                           memory[I+i] = (char)V[i];
                       }
                      // I = tempo; //this command is more suitable for modern chip48 and superchip apps
                       I=(char)(I+xf+1);
                       pc+=2;
                       break;
                   }
                   case 0x0065:{//FX65: Load from the memory addresses I through I+X and stores them at registers V[0] through VX
                       int xf = (opcode&0x0F00)>>8;
                       char tempo = I;
                       for(int i = 0; i<=xf;i++){
                           V[i] = memory[I+i];
                       }
                       //I = tempo;
                       I=(char)(I+xf+1);
                       System.out.println("Setting V[0] to V[" + xf + "] to the values of merory[0x" + Integer.toHexString(I & 0xFFFF).toUpperCase() + "]");
                       pc+=2;
                       break;
                   }
                    case 0x0007:{//FX07: set VX to current value of delay timer
                       int xf = (opcode&0x0F00)>>8;
                       V[xf] = (char) delay_timer;
                       pc+=2;
                       System.out.println("V[" + xf + "] has been set to " + delay_timer);
                       break;
                   }
                   case 0x0018:{//FX18: set VX to current value of delay timer
                       int xf = (opcode&0x0F00)>>8;
                       sound_timer = (int)V[xf];
                       pc+=2;
                       break;
                   }
                  
                  
                   case 0x0029:{//FX18: set VX to current value of delay timer
                       int xf = (opcode&0x0F00)>>8;
                       int character = V[xf];
                       I = (char) (0x050+(character*5));
                       System.out.println("Setting I to Character V[" + xf + "] = " + (int)V[xf] + " Offset to 0x" + Integer.toHexString(I).toUpperCase());
                       pc+=2;
                       break;
                   }
                   case 0x000A:{//FX0A: A key will be pressed and store it's value to VX
                       int xf = (opcode&0x0F00)>>8;
                       for(int i = 0; i<key.length; i++){
                           if(key[i]==1){
                               V[xf] = (char)i;
                               pc+=2;
                               break;
                           }
                       }
                       System.out.println("Awaiting key input");
                       break;
                   }
                   case 0x001E:{//FX1E: Add VX to the value of I
                       int xf = (opcode&0x0F00)>>8;
                       I += V[xf];
                       if(I >0x0FFF){
                           V[0xF]=1;
                       }
                       pc+=2;
                       break;
                   }
                  default: System.err.println("OPCODE not supported");
                    System.exit(0); 
                    break;
               }
               break;
           default: System.err.println("OPCODE not supported");
           System.exit(0);
       }
       //execute opcodes
       if(sound_timer >0){
           sound_timer--;
       }
       if(delay_timer >0){
           delay_timer--;
       }
   }
   public byte[] getDisplay(){
       return display;
   }

    public void loadProgram(String file) {
        DataInputStream input = null;
        try {
            //loading program into memory
            input = new DataInputStream(new FileInputStream(new File(file)));
            int offset = 0;
            while(input.available() > 0){ // loading a program
                memory[0x200 + offset] = (char)(input.readByte() & 0xFF);
                offset++;
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }finally{
            if(input != null){
                try{
                    input.close(); //closing, to ensure no resource leakage is occuring
                }catch(IOException e){
                    
                }
            }
        }
    }

    public boolean needsRedraw() {
        return needRedraw;
    }

    public void removeDrawFlag() {
        needRedraw = false;
    }
    public void loadFontSet(){
        for(int i = 0; i< ChipData.fontset.length; i++){
            memory[0x50 +i] = (char)(ChipData.fontset[i] & 0xFF);
        }
    }
    public void setKeyBuffer(int[] keyBuffer){//copy value of the key buffer into the actual memory of the chip
        for(int i=0; i<key.length;i++){
            key[i] = (byte)keyBuffer[i];
        }
    }
}
