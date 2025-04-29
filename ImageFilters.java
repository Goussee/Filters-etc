import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.awt.Desktop;
import javax.imageio.ImageIO;

public class ImageFilters{

    //2x2 dither pattern
    public static int[] vals2 = {32,224,160,96};

    //4x4 dither pattern
    public static int[] vals4 = {12,204,60,252,140,76,188,124,44,236,28,220,172,108,156,92};
    
    //8x8 dither pattern
    public static int[] vals8 = {2,130,34,162,10,138,42,170,194,66,226,98,202,74,234,106,50,178,18,146,58,186,26,154,242,114,210,82,250,122,218,90,14,142,46,174,6,134,38,166,206,78,238,110,198,70,230,102,62,190,30,158,54,182,22,150,254,126,222,94,246,118,214,86};

    //different gradient pattern
    public static int[] simpleVals = {8,24,40,56,72,88,104,120,136,152,168,184,200,216,232,248,264};

    public static void main(String[] args) throws IOException{

        String fileNames = "miku1.png";
        
        //could prob also do "for(String fileName : args)"
        //but this is easier to just write it above in the IDE
        for(String fileName : fileNames.split(" ")){
            System.out.printf("Processing %s...%n", fileName);
            File file = getGradient("Images/" + fileName);
            openImage(file);
        }

        System.out.println("Done!");

    }

    public static void openImage(File file) throws IOException{
        Desktop.getDesktop().open(file);
    }

    public static int rotateX(int x, int y, int w, int h, double a){
        double r = Math.sqrt(Math.pow(x - w/2, 2) + Math.pow(y - h/2, 2));
        double theta = Math.atan((y-h/2)/(x-w/2)) + (x<w/2?Math.PI:0);
        theta += a;
        System.out.printf("r = %f, theta = %f", r, theta);
        return w/2 + (int)(r*Math.cos(theta));
    }

    public static int rotateY(int x, int y, int w, int h, double a){
        double r = Math.sqrt(Math.pow(x - w/2, 2) + Math.pow(y - h/2, 2));
        double theta = Math.atan((y-h/2)/(x-w/2)) + (x<w/2?Math.PI:0);
        theta += a;
        System.out.printf("r = %f, theta = %f", r, theta);
        return h/2 + (int)(r*Math.sin(theta));
    }

    public static File getEdges(String files){
        File gradientFile = getGradient(files);
        BufferedImage temp = null;
        try {
            temp = ImageIO.read(gradientFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(int i = 0; i < temp.getWidth(); i++){
            for(int j = 0; j < temp.getHeight(); j++){
                temp.setRGB(i, j, getGrey(getLuminance(temp.getRGB(i, j))));
            }
        }
        File file = new File(String.format("%s_edges.png",files.split("\\.")[0]));
        try {
            ImageIO.write(temp, "png", file);
        } catch(IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public static File getGradient(String files){
        int size = 0;
        BufferedImage og;
        try {
            og = ImageIO.read(new File(files));
        } catch(IOException e) {
            e.printStackTrace();
            return null;
        }
        BufferedImage fixed = new BufferedImage(og.getWidth(), og.getHeight(), BufferedImage.TYPE_INT_RGB);
        int t = 0;
        for(int x = 0; x < og.getWidth(); x++){
            for(int y = 0; y < og.getHeight(); y++){
                size = 3 + 2*(int)Math.random();
                int c = getEdgeGradient(og, x, y, size);
                // fixed.setRGB(x, y, add(c,og.getRGB(x, y)));
                fixed.setRGB(x, y, c);
                t++;
            }
            System.out.printf("Progress: %f%%\n", (100.0*t)/(og.getWidth()*og.getHeight()));
        }
        File file = new File(String.format("%s_normals.png",files.split("\\.")[0]));
        try {
            ImageIO.write(fixed, "png", file);
        } catch(IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public static int getEdgeGradient(BufferedImage img, int x, int y, int size){
        double red = 0, green = 0, blue = 0;
        int numAngles = 32;
        for(double theta = 0; theta <= 2*Math.PI; theta+=2*Math.PI/numAngles){
            int xp = (int)(x+size*Math.cos(theta));
            int yp = (int)(y+size*Math.sin(theta));
            if(xp < 0 || xp >= img.getWidth() || yp < 0 || yp >= img.getHeight())
                continue;
            double fac = 0.005/numAngles * getColorDifference(img.getRGB(x, y), img.getRGB((int)(x+size*Math.cos(theta)), (int)(y+size*Math.sin(theta))));
            red += getHSV(180/Math.PI * theta, 100, 100).getRed() * fac;
            green += getHSV(180/Math.PI * theta, 100, 100).getGreen() * fac;
            blue += getHSV(180/Math.PI * theta, 100, 100).getBlue() * fac;
        }
        return new Color(Math.min(255, (int)red), Math.min(255, (int)green), Math.min(255, (int)blue)).getRGB();
    }

    public static Color getHSV(double h, int s, int v){
        //hue: 0-360, saturation: 0-100, value: 0-100
        double r = 0, g = 0, b = 0;
        double c = s/100.0 * v/100.0;
        double x = c * (1 - Math.abs((h/60)%2 - 1));
        double m = v/100.0 - c;
        if(h < 60){
            r = c; g = x; b = 0;
        }else if(h < 120){
            r = x; g = c; b = 0;
        }else if(h < 180){
            r = 0; g = c; b = x;
        }else if(h < 240){
            r = 0; g = x; b = c;
        }else if(h < 300){
            r = x; g = 0; b = c;
        }else{
            r = c; g = 0; b = x;
        }
        return new Color((int)(255*(r+m)), (int)(255*(g+m)), (int)(255*(b+m)));
    }

    public static File tinyRGBK(String files) {
        BufferedImage og;
        try {
            og = ImageIO.read(new File(files));
        } catch(IOException e) {
            e.printStackTrace();
            return null;
        }
        BufferedImage fixed = new BufferedImage(og.getWidth(), og.getHeight(), BufferedImage.TYPE_INT_RGB);

        //red loop
        for(int x = 0; x < og.getWidth(); x+=2)
            for(int y = 0; y < og.getHeight(); y+=2){
                int c = (int)(4*Math.random());
                int t = (int)(255*Math.random());
                switch(c){
                    case 0:
                        fixed.setRGB(x, y, getRed(og.getRGB(x, y),t));
                        break;
                    case 1:
                        fixed.setRGB(1+x, y, getGreen(og.getRGB(x, y),t));
                        break;
                    case 2:
                        fixed.setRGB(1+x, 1+y, getBlue(og.getRGB(x, y),t));
                        break;
                    case 3:
                        fixed.setRGB(x, 1+y, getMaxRGB(og.getRGB(x, y),t));
                        break;
                }
            }
        
        File file = new File(String.format("RGBK_%s_tiny.png",files.split("\\.")[0]));
        try {
            ImageIO.write(fixed, "png", file);
        } catch(IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public static int getMaxRGB(int col, int t){
        Color temp = new Color(col);
        int r = temp.getRed();
        int g = temp.getGreen();
        int b = temp.getBlue();
        int max = Math.max(r, Math.max(g, b));
        if(max < 128)
            return 0;
        if(max == r)
            return new Color(255, 0, 0).getRGB();
        if(max == g)
            return new Color(0, 255, 0).getRGB();
        return new Color(0, 0, 255).getRGB();
    }

    public static int getRed(int col, int t){
        Color temp = new Color(col);
        Color adj = new Color(temp.getRed()>t?255:0,0,0);
        return adj.getRGB();
    }

    public static int getGreen(int col, int t){
        Color temp = new Color(col);
        Color adj = new Color(0, temp.getGreen()>t?255:0,0);
        return adj.getRGB();
    }

    public static int getBlue(int col, int t){
        Color temp = new Color(col);
        Color adj = new Color(0,0,temp.getBlue()>t?255:0);
        return adj.getRGB();
    }

    public static int getEdgeBrightness(BufferedImage img, int x, int y, int size){
        return getColorDifference(img.getRGB(x-size, y-size), img.getRGB(x+size, y+size)) + getColorDifference(img.getRGB(x+size, y-size), img.getRGB(x-size, y+size));
    }

    public static int add(int col1, int col2){
        Color c1 = new Color(col1);
        Color c2 = new Color(col2);
        return new Color(Math.min(255, c1.getRed()+c2.getRed()), Math.min(255, c1.getGreen()+c2.getGreen()), Math.min(255, c1.getBlue()+c2.getBlue())).getRGB();
    }

    public static int getLuminance(int col){
        Color c = new Color(col);
        return (int)(0.2126*c.getRed() + 0.7152*c.getGreen() + 0.0722*c.getBlue());
    }

    public static int getColorDifference(int col1, int col2){
        Color c1 = new Color(col1);
        Color c2 = new Color(col2);
        return (int)Math.sqrt(Math.pow(c1.getRed()-c2.getRed(),2) + Math.pow(c1.getGreen()-c2.getGreen(),2) + Math.pow(c1.getBlue()-c2.getBlue(),2));
    }

    public static int getGrey(int lum){
        if(lum <= 0)
            return Color.BLACK.getRGB();
        if(lum >= 255)
            return Color.WHITE.getRGB();
        Color temp = new Color(lum, lum, lum);
        return temp.getRGB();
    }

    public static File noDither(String files) {
        BufferedImage og;
        try {
            og = ImageIO.read(new File(files));
        } catch(IOException e) {
            e.printStackTrace();
            return null;
        }
        BufferedImage fixed = new BufferedImage(og.getWidth(), og.getHeight(), BufferedImage.TYPE_INT_RGB);
        for(int x = 0; x < og.getWidth(); x++){
            for(int y = 0; y < og.getHeight(); y++){
                fixed.setRGB(x, y, get8bit(og.getRGB(x, y),128));
            }
        }
        File file = new File(String.format("8_bit_%s_bad.png",files.split("\\.")[0]));
        try {
            ImageIO.write(fixed, "png", file);
        } catch(IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public static File weirdDither(String files) {
        BufferedImage og;
        try {
            og = ImageIO.read(new File(files));
        } catch(IOException e) {
            e.printStackTrace();
            return null;
        }
        BufferedImage fixed = new BufferedImage(og.getWidth(), og.getHeight(), BufferedImage.TYPE_INT_RGB);
        int i = (int)(256*Math.random());
        for(int x = 0; x < og.getWidth(); x++){
            for(int y = 0; y < og.getHeight(); y++){
                fixed.setRGB(x, y, get8bit(og.getRGB(x, y),i));
                i = (int)(i + 40*Math.random()) % 256;
            }
        }
        File file = new File(String.format("8_bit_%s_weird.png",files.split("\\.")[0]));
        try {
            ImageIO.write(fixed, "png", file);
        } catch(IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public static File tinyDither(String files) {
        BufferedImage og;
        try {
            og = ImageIO.read(new File(files));
        } catch(IOException e) {
            e.printStackTrace();
            return null;
        }
        BufferedImage fixed = new BufferedImage(og.getWidth(), og.getHeight(), BufferedImage.TYPE_INT_RGB);
        for(int x = 0; x < og.getWidth()-1; x+=2){
            for(int y = 0; y < og.getHeight()-1; y+=2){
                int i = 0;
                for(int xoff = 0; xoff < 2; xoff++)
                    for(int yoff = 0; yoff < 2; yoff++)
                        fixed.setRGB(x+xoff, y+yoff, get8bit(og.getRGB(x+xoff, y+yoff),vals2[i++]));
            }
        }
        File file = new File(String.format("8_bit_%s_tiny.png",files.split("\\.")[0]));
        try {
            ImageIO.write(fixed, "png", file);
        } catch(IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public static File greyscale(String fileName) throws IOException {
        BufferedImage og;
        try {
            og = ImageIO.read(new File(fileName));
        } catch(IOException e) {
            e.printStackTrace();
            return null;
        }
        BufferedImage fixed = new BufferedImage(og.getWidth(), og.getHeight(), BufferedImage.TYPE_INT_RGB);
        for(int x = 0; x < og.getWidth(); x++){
            for(int y = 0; y < og.getHeight(); y++){
                fixed.setRGB(x, y, getGrey(getLuminance(og.getRGB(x, y))));
            }
        }
        File file = new File(String.format("greyscale_%s.png",fileName.split("\\.")[0]));
        try {
            ImageIO.write(fixed, "png", file);
        } catch(IOException e) {
            e.printStackTrace();
        }
        
        return file;
    }

    public static File smallDither(String files) {
        BufferedImage og;
        try {
            og = ImageIO.read(new File(files));
        } catch(IOException e) {
            e.printStackTrace();
            return null;
        }
        BufferedImage fixed = new BufferedImage(og.getWidth(), og.getHeight(), BufferedImage.TYPE_INT_RGB);
        for(int x = 0; x < og.getWidth()-3; x+=4){
            for(int y = 0; y < og.getHeight()-3; y+=4){
                int i = 0;
                for(int xoff = 0; xoff < 4; xoff++)
                    for(int yoff = 0; yoff < 4; yoff++)
                        fixed.setRGB(x+xoff, y+yoff, get8bit(og.getRGB(x+xoff, y+yoff),vals4[i++]));
            }
        }
        File file = new File(String.format("8_bit_%s_small.png",files.split("\\.")[0]));
        try {
            ImageIO.write(fixed, "png", file);
        } catch(IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public static File bigDither(String files) {
        BufferedImage og;
        try {
            og = ImageIO.read(new File(files));
        } catch(IOException e) {
            e.printStackTrace();
            return null;
        }
        BufferedImage fixed = new BufferedImage(og.getWidth(), og.getHeight(), BufferedImage.TYPE_INT_RGB);
        for(int x = 0; x < og.getWidth()-7; x+=8){
            for(int y = 0; y < og.getHeight()-7; y+=8){
                int i = 0;
                for(int xoff = 0; xoff < 8; xoff++)
                    for(int yoff = 0; yoff < 8; yoff++)
                        fixed.setRGB(x+xoff, y+yoff, get8bit(og.getRGB(x+xoff, y+yoff),vals8[i++]));
            }
        }
        File file = new File(String.format("8_bit_%s_big.png",files.split("\\.")[0]));
        try {
            ImageIO.write(fixed, "png", file);
        } catch(IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public static int get8bit(int col, int t){
        Color temp = new Color(col);
        //makes an 8-bit color based on the given threshold
        Color adj = new Color(temp.getRed()>t?255:0,temp.getGreen()>t?255:0,temp.getBlue()>t?255:0);
        return adj.getRGB();
    }
}