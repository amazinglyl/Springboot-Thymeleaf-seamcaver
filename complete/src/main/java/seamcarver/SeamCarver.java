package seamcarver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class SeamCarver {
    private BufferedImage image;
    private int width;
    private int height;
    private double[][] energy;

    public SeamCarver(String fileName) {
        try {
            File file = new File(fileName);
            image = ImageIO.read(file);
            update();
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }

    public void update() {
        width = image.getWidth();
        height = image.getHeight();
        energy = Energy.energy(image);
    }

    public int[] findHorizontalSeam() {
        int[][] pre = new int[height][width];
        for (int i = 1; i < width; i++) {
            for (int j = 0; j < height; j++) {
                double min = Double.MAX_VALUE;
                for (int k = -1; k <= 1; k++) {
                    int x = j + k;
                    int a=0;
                    if (x >= 0 && x < height) {
                        if (energy[x][i - 1] < min) {
                            pre[j][i] = x;
                            min = energy[x][i - 1];
                        }
                    }
                }
                energy[j][i] += min;
            }
        }
        int end = -1;
        double min = Double.MAX_VALUE;
        for (int i = 0; i < height; i++) {
            if (energy[i][width - 1] < min) {
                end = i;
                min = energy[i][width - 1];
            }
        }
        int[] res = new int[width];
        res[width - 1] = end;
        for (int i = width - 1; i > 0; i--) {
            res[i - 1] = pre[end][i];
            end = res[i - 1];
        }
        return res;
    }

    public void removeHorizontalSeam() {
        int[] path = findHorizontalSeam();
        BufferedImage newImage = new BufferedImage(width, height - 1, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < width; i++) {
            int pos = 0;
            for (int j = 0; j < height; j++) {
                if (path[i] != j) {
                    newImage.setRGB(i, pos, image.getRGB(i, j));
                    pos++;
                }
            }
        }
        image = newImage;
        update();
    }

    public int[] findVerticalSeam() {
        int[][] pre = new int[height][width];
        for (int i = 1; i < height; i++) {
            for (int j = 0; j < width; j++) {
                double min = Double.MAX_VALUE;
                for (int k = -1; k <= 1; k++) {
                    int y = j + k;
                    if (y >= 0 && y < width) {
                        if (energy[i - 1][y] < min) {
                            pre[i][j] = y;
                            min = energy[i - 1][y];
                        }
                    }
                }
                energy[i][j] += min;
            }
        }
        int end = -1;
        double min = Double.MAX_VALUE;
        for (int i = 0; i < width; i++) {
            if (energy[height - 1][i] < min) {
                end = i;
                min = energy[height - 1][i];
            }
        }
        int[] res = new int[height];
        res[height - 1] = end;
        for (int i = height - 1; i > 0; i--) {
            res[i - 1] = pre[i][end];
            end = res[i - 1];
        }
        return res;
    }

    public void removeVerticalSeam() {
        int[] path = findVerticalSeam();
        BufferedImage newImage = new BufferedImage(width - 1, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < height; i++) {
            int pos = 0;
            for (int j = 0; j < width; j++) {
                if (path[i] != j) {
                    newImage.setRGB(pos, i, image.getRGB(j, i));
                    pos++;
                }
            }
        }
        image = newImage;
        update();
    }

    public BufferedImage seamCarver(int seamH, int seamV) {
        for (int i = 0; i < seamH; i++) {
            removeHorizontalSeam();
        }
        for (int i = 0; i < seamV; i++) {
            removeVerticalSeam();
        }
        return image;
    }
}
