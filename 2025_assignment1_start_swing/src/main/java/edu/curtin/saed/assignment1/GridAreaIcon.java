// WARNING: don't modify this file, unless you're sure you know what you're doing!

package edu.curtin.saed.assignment1;

import java.awt.geom.AffineTransform;
import java.awt.Image;
import javax.swing.ImageIcon;
import java.net.URL;

/**
 * Represents an image to be displayed in a GridArea panel. If you change any of the properties,
 * be sure to call 'repaint()' on the GridArea after you're done.
 */
public class GridAreaIcon
{
    private AffineTransform transform = new AffineTransform();
    private boolean redoTransform = true;

    private double x;
    private double y;
    private double rotation;
    private double scale;
    private Image image;
    private String caption;
    private boolean shown = true;

    public GridAreaIcon(double x, double y, double rotation, double scale,
                        Image image, String caption)
    {
        this.x = x;
        this.y = y;
        this.rotation = rotation;
        this.scale = scale;
        this.image = image;
        this.caption = caption;
    }

    public GridAreaIcon(double x, double y, double rotation, double scale,
                        URL imageUrl, String caption)
    {
        this(x, y, rotation, scale, new ImageIcon(imageUrl).getImage(), caption);
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }

    public double getRotation()
    {
        return rotation;
    }

    public double getScale()
    {
        return scale;
    }

    public AffineTransform getTransform()
    {
        if(redoTransform)
        {
            transform.setToIdentity();
            transform.translate(x, y);
            double pixelWidth = image.getWidth(null);
            double pixelHeight = image.getHeight(null);
            double radians = rotation * Math.PI / 180.0;
            if(pixelWidth > pixelHeight)
            {
                transform.translate(0.0, (1.0 - pixelHeight / pixelWidth) / 2.0);
                transform.rotate(radians, 0.5, 0.5 * pixelHeight / pixelWidth);
                transform.scale(scale / pixelWidth, scale / pixelWidth);
            }
            else
            {
                transform.translate((1.0 - pixelWidth / pixelHeight) / 2.0, 0.0);
                transform.rotate(radians, 0.5 * pixelWidth / pixelHeight, 0.5);
                transform.scale(scale / pixelHeight, scale / pixelHeight);
            }
            redoTransform = false;
        }
        return transform;
    }

    public Image getImage()
    {
        return image;
    }

    public String getCaption()
    {
        return caption;
    }

    public boolean isShown()
    {
        return shown;
    }

    public void setPosition(double x, double y)
    {
        this.x = x;
        this.y = y;
        redoTransform = true;
    }

    public void setRotation(double rotation)
    {
        this.rotation = rotation;
        redoTransform = true;
    }

    public void setScale(double scale)
    {
        this.scale = scale;
        redoTransform = true;
    }

    public void setImage(Image image)
    {
        this.image = image;
    }

    public void setCaption(String caption)
    {
        this.caption = caption;
    }

    public void setShown(boolean shown)
    {
        this.shown = shown;
    }
}
