import java.awt.Graphics;

/**
 * Copyright (c) 2015 Ben Alderfer
 * See the file license.txt for copying permission.
 *
 * @author Ben Alderfer
 * The HoverBot class
 * Moves up and down on the side and sometimes fires a big fire blast thing
 */
public class HoverBot extends Being
{
	private int speed = 2;		//the speed and direction the HoverBot is moving in, - for up, + for down
	
	/**
	 * Default constructor
	 */
	public HoverBot()
	{
		setImage("Pictures/Enemies/hoverbot0.png");
		setHealth(2);
		beingType = "hoverbot";
	}
	
	/**
	 * Constructs a HoverBot with x, y, width, height
	 * @param x the x coordinate 
	 * @param y the y coordinate
	 * @param wdt the width
	 * @param hgt the height
	 */
	public HoverBot(int x, int y, int wdt, int hgt)
	{
		super(x, y, wdt, hgt);
		setImage("Pictures/Enemies/hoverbot0.png");
		setHealth(2);
		beingType = "hoverbot";
	}
	
	 /**
	  * Draws the HoverBot from file
	  * overrides ImageItem's method to reset image after firing
	  * @param window the graphics object
	  */
	 public void draw(Graphics window)
	 {		
		super.draw(window);
		setImage("Pictures/Enemies/hoverbot0.png");
	 }
	 
	 /**
	  * Overrides scroll to move up and down instead of left
	  * Will move left until its barely in view
	  * @param width the width of the frame
	  */
	 public void scroll(int width)
	 {
		 if (getX() + getWidth() > width)
			 setX(getX() - SCROLL_SPEED);
		 setY(getY() + speed);
	 }
	 
	 /**
	  * Returns the speed of the HoverBot
	  * @return speed the speed of the HoverBot
	  */
	 public int getSpeed()
	 	{return speed;}
	 
	 /**
	  * Reverses the HoverBot's direction if it hits the ground or roof
	  * @param p the highest Piece below HoverBot if speed is negative or the roof if speed is positive
	  */
	 public void checkDirection(Piece p)
	 {		 
		 if (didCollideTop(p) || pieceIsThere(p) || didCollideBottom(p))
			 	speed = -speed;
	 }
}
