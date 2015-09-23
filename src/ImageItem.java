import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Copyright (c) 2015 Ben Alderfer
 * See the file license.txt for copying permission.
 *
 * @author Ben Alderfer
 * The generic class for things with images
 * Reads the current directory
 */
public class ImageItem extends Piece
{
	private Image item = null;
	private String path;
	public final int GRAVITY = 5;	//how fast the player falls
	private int tempGravity = GRAVITY;	//the temporary gravity, increases with time	
		
	/**
	 * Default constructor
	 * Calls Block's default constructor
	 */
	public ImageItem()
		{super();}
	
	/**
	 * Constructs an ImageItem with x, y
	 * @param x the x coordinate
	 * @param y the y coordinate
	 */
	public ImageItem(int x, int y)
		{super(x, y);}
	
	/**
	 * Constructs an ImageItem with x, y, width, height
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param wdt the width
	 * @param hgt the height
	 */
	public ImageItem(int x, int y, int wdt, int hgt)
		{super(x, y, wdt, hgt);}
	
	/**
	 * Constructs an ImageItem with x, y, width, height
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param wdt the width
	 * @param hgt the height
	 * @param path the partial path to the image
	 */
	public ImageItem(int x, int y, int wdt, int hgt, String path)
	{
		super(x, y, wdt, hgt);
		setImage(path);
	}
	
	/**
	  * Sets the ImageItem's image
	  * Only the end of the path is passed
	  * @param path the path to the picture
	  */
	 public void setImage(String path)
	 {
		try 
		{
			item = ImageIO.read(new File(path));
			this.path = path;
		}
		catch (IOException e)
			{e.printStackTrace();}
	 }
	 
	 /**
	  * Returns the path to see what the image is
	  * @return path the image path
	  */
	 public String getPath()
	 	{return path;}
	 
	 /**
	  * Draws the ImageItem from file
	  * @param window the graphics object
	  */
	 public void draw(Graphics window)
	 	{window.drawImage(item, getX(), getY(), getWidth(), getHeight(), null);}	
	 
	 /**
	  * Returns the temp gravity which increases with time
	  * @return tempGravity the gravity to add
	  */
	 public int getGravity()
	 	{return tempGravity;}
	 
	 /**
	  * Sets a new gravity	
	  * @param g the new gravity
	  */
	 public void setGravity(int g)
		{tempGravity = g;}
	 
	 /**
	  * Resets the gravity back to the default value
	  */
	 public void resetGravity()
	 	{tempGravity = GRAVITY;}
}
