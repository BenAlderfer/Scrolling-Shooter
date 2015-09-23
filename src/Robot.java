import java.awt.Graphics;

/**
 * Copyright (c) 2015 Ben Alderfer
 * See the file license.txt for copying permission.
 *
 * @author Ben Alderfer
 * The Robot enemy
 * Can only shoot, explodes when hit
 * Has a 50% chance to jump over bullets
 */

public class Robot extends Being
{
	/**
	 * Default constructor
	 */
	public Robot()
	{
		setImage("Pictures/Enemies/robot0.png");
		setHealth(2);
		beingType = "robot";
	}
	
	/**
	 * Constructs a Robot with x, y, width, height
	 * @param x the x coordinate 
	 * @param y the y coordinate
	 * @param wdt the width
	 * @param hgt the height
	 */
	public Robot(int x, int y, int wdt, int hgt)
	{
		super(x, y, wdt, hgt);
		setImage("Pictures/Enemies/robot0.png");
		setHealth(2);
		beingType = "robot";
	}
	
	/**
	 * The enemy has a 50% chance of jumping over a bullet
	 * @param b the bullet it might jump
	 */
	public void attemptEvadeBullet(Bullet b)
	{
		if (b.getX() + b.getWidth() >= getX() - b.getXSpeed() && b.getX() < getX() && 
			b.getY() > getY() && b.getY() + b.getHeight() < getY() + getHeight())
				if ((int) (Math.random() * 2) == 0)
					setY(b.getY() - getHeight() - 40);
	}
	
	/**
	  * Draws the Robot from file
	  * Overrides the super class version to reset image
	  * @param window the graphics object
	  */
	 public void draw(Graphics window)
	 {
		 super.draw(window);
		 setImage("Pictures/Enemies/robot0.png");
	 }	
}
