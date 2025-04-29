import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import java.awt.Desktop;
import javax.imageio.ImageIO;

public class EightBitify{
    public static int[] vals = {12,204,60,252,140,76,188,124,44,236,28,220,172,108,156,92};
    public static void main(String[] args) throws IOException{
        for(String file : args)
            processImage(file);
        System.out.println("Done!");
    }

    public static void processImage(String fileName) throws IOException {
        BufferedImage og;
        try {
            og = ImageIO.read(new File(fileName));
        } catch(IOException e) {
            e.printStackTrace();
            return;
        }
        BufferedImage fixed = new BufferedImage(og.getWidth(), og.getHeight(), BufferedImage.TYPE_INT_RGB);
        for(int x = 0; x < og.getWidth()-3; x+=4){
            for(int y = 0; y < og.getHeight()-3; y+=4){
                int i = 0;
                for(int xoff = 0; xoff < 4; xoff++)
                    for(int yoff = 0; yoff < 4; yoff++)
                        fixed.setRGB(x+xoff, y+yoff, getColor(og.getRGB(x+xoff, y+yoff),vals[i++]));
            }
        }
        File file = new File(String.format("extra_sigma_%s.png",fileName.split("\\.")[0]));
        try {
            ImageIO.write(fixed, "png", file);
        } catch(IOException e) {
            e.printStackTrace();
            return;
        }
        Desktop.getDesktop().open(file);
    }

    public static int getColor(int col, int t){
        Color temp = new Color(col);
        //makes an 8-bit color based on the given threshold
        Color adj = new Color(temp.getRed()>t?255:0,temp.getGreen()>t?255:0,temp.getBlue()>t?255:0);
        return adj.getRGB();
    }
}