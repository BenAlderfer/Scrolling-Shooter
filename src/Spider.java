import java.awt.Graphics;
import java.util.ArrayList;

/**
 * Copyright (c) 2015 Ben Alderfer
 * See the file license.txt for copying permission.
 *
 * @author Ben Alderfer 
 * The Spider enemy
 * Moves 2x the speed of other enemies
 * Can climb over blocks
 * Poisons the player on contact
 */
public class Spider extends Being
{	
	private int walkDelay = 5;			//slows down the walking speed
	
	/**
	 * Default constructor
	 */
	public Spider()
	{
		setImage("Pictures/Enemies/spider0.png");
		setHealth(1);
		beingType = "spider";
	}
	
	/**
	 * Constructs a Spider with x, y, width, height
	 * @param x the x coordinate 
	 * @param y the y coordinate
	 * @param wdt the width
	 * @param hgt the height
	 */
	public Spider(int x, int y, int wdt, int hgt)
	{
		super(x, y, wdt, hgt);
		setImage("Pictures/Enemies/spider0.png");
		setHealth(1);
		beingType = "spider";
	}
	
	/**
	 * Checks the ArrayList of items and jumps a block if it needs to
	 * @param a the ArrayList of items
	 */
	public void chooseMove(ArrayList<ImageItem> a)
	{
		ImageItem closest = a.get(0);
		for (int i = 1; i < a.size(); i++)		//determines closest block
			if (a.get(i).getX() + a.get(i).getWidth() <= getX() + getWidth())
				if (a.get(i).getX() > closest.getX())
					closest = a.get(i);
		
		if (didCollideRight(closest, 2 * SCROLL_SPEED) || pieceIsThere(closest))
			setY(closest.getY() - getHeight());	
		
		scroll();
		scroll();
	}
	
	 /**
	  * Draws the Spider from file
	  * overrides ImageItem's method to make Spider walk
	  * @param window the graphics object
	  */
	 public void draw(Graphics window)
	 {
		 walkDelay--;
			
		if (walkDelay == 0)
		{
			if (getPath().equals("Pictures/Enemies/spider0.png"))		//changes the Spider's image to make it look like walking
				setImage("Pictures/Enemies/spider1.png");
			else	//if (getPath().equals("Pictures/Enemies/spider1.png"))
				setImage("Pictures/Enemies/spider0.png");
				
			walkDelay = 5;
		}
		
		super.draw(window);
	 }
}
