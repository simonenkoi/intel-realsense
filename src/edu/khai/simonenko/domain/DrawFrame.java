package edu.khai.simonenko.domain;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

public class DrawFrame extends JComponent {

    public BufferedImage image;

    public DrawFrame(int width, int height) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, this);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension superSize = super.getPreferredSize();
        int w = image == null ? superSize.width : Math.max(superSize.width, image.getWidth(null));
        int h = image == null ? superSize.height : Math.max(superSize.height, image.getHeight(null));
        return new Dimension(w, h);
    }
}
