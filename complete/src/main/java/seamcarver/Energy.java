package seamcarver;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Energy {
    public Energy(){}

    public static double[][] energy(BufferedImage image){
        int width = image.getWidth();
        int height=image.getHeight();
        double[][] energy = new double[height][width];

        for(int x=0;x<height;x++){
            for(int y=0;y<width;y++) {
                int x1 = (x - 1 + height) % height;
                int x2 = (x + 1) % height;
                int y1 = (y - 1 + width) % width;
                int y2 = (y + 1) % width;

                Color left = new Color(image.getRGB(y, x1));
                Color right = new Color(image.getRGB(y, x2));
                Color top = new Color(image.getRGB(y1, x));
                Color bottom = new Color(image.getRGB(y2, x));

                int gx = right.getGreen() - left.getGreen();
                int rx = right.getRed() - left.getRed();
                int bx = right.getBlue() - left.getBlue();

                int gy = bottom.getGreen() - top.getGreen();
                int ry = bottom.getRed() - top.getRed();
                int by = bottom.getBlue() - top.getBlue();

                energy[x][y] = Math.pow(gx, 2) + Math.pow(rx, 2) + Math.pow(bx, 2) + Math.pow(gy, 2) + Math.pow(ry, 2) + Math.pow(by, 2);
            }
        }
        return energy;
    }
}
