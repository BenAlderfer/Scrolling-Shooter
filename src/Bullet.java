import java.awt.Graphics;

/**
 * Copyright (c) 2015 Ben Alderfer
 * See the file license.txt for copying permission.
 *
 * Ben Alderfer
 * The Bullet class
 */
public class Bullet extends ImageItem
{
	private int xSpeed;

	/**
	 * Default constructor
	 */
	public Bullet()
	{
		super(200, 200);
		xSpeed = 3;
	}
	
	/**
	 * Constructs a Bullet with position, dimension, and x and y speeds
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param wdt the width
	 * @param ht the height
	 * @param xSpd the x speed
	 * @param path the partial path to the image
	 */
	public Bullet(int x, int y, int wdt, int ht, int xSpd, String path)
	{
		super(x, y, wdt, ht);
		xSpeed = xSpd;
		setImage(path);
	}
	
	/**
	 * Sets the x speed
	 * @param xSpd the x speed
	 */
	public void setXSpeed(int xSpd)
		{xSpeed = xSpd;}
	
	/**
	 * Returns the x speed
	 * @return xSpeed the x speed
	 */
	public int getXSpeed()
		{return xSpeed;}

	/**
	 * Moves and draws the object
	 * @param window the graphics object
	 */
   public void moveAndDraw(Graphics window)
   {	
		setX(getX() + getXSpeed());
		super.draw(window);
   }
   
   /**
    * Checks if obj equals the current object
    * @param obj the object to check
    * @return true if they are equal, false otherwise
    */
   public boolean equals(Piece obj)
   {
		Bullet other = (Bullet) obj;
		
		if(super.equals(obj) && 
		   other.getXSpeed() == xSpeed &&
		   getPath().equals(other.getPath()))
			return true;
		else
			return false;
   } 
	
   /**
	* Returns the object's attributes
	* @return the object's attributes
	*/
   public String toString()
   		{return getX() + " " + getY() + " " + getWidth() + " " + getHeight() + " " + xSpeed;}
}