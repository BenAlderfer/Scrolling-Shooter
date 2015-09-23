import java.awt.Graphics;

/**
 * Copyright (c) 2015 Ben Alderfer
 * See the file license.txt for copying permission.
 *
 * Ben Alderfer
 * The Piece class 
 */
public class Piece
{
	private int xPos, yPos, width, height;
	public static final int SCROLL_SPEED = 6;

	/**
	 * Default constructor
	 */
	public Piece()
	{
		xPos = 100;
		yPos = 150;
		width = 10;
		height = 10;
	}
	
	/**
	 * Constructs a Piece with position
	 * @param x the x coordinate
	 * @param y the y coordinate
	 */
	public Piece(int x, int y)
	{
		xPos = x;
		yPos = y;
		width = 10;
		height = 10;
	}
	
	/**
	 * Constructs a Piece with position and dimensions
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param wdt the width
	 * @param ht the height
	 */
	public Piece(int x, int y, int wdt, int ht)
	{
		xPos = x;
		yPos = y;
		width = wdt;
		height = ht;
	} 

   /**
    * Sets the x coordinate
    * @param x the x coordinate
    */
   public void setX(int x) 
		{xPos = x;}

   /**
    * Sets the y coordinate
    * @param y the y coordinate 
    */
   public void setY(int y) 
		{yPos = y;}
   
   /**
    * Sets the position
    * @param x the x coordinate
    * @param y the y coordinate
    */
   public void setPos(int x, int y) 
   {
		xPos = x;
		yPos = y;
   }
   
   /**
    * Sets the width
    * @param wdt the width
    */
   public void setWidth(int wdt)
   	{width = wdt;}
   
   /**
    * Sets the height
    * @param hgt the height
    */
   public void setHeight(int hgt)
   	{height = hgt;}
  
  /**
	 * Returns the x coordinate
	 * @return the x coordinate
	 */
	public int getX() 
		{return xPos;}

	/**
	 * Returns the y coordinate
	 * @return the y coordinate
	 */
	public int getY() 
		{return yPos;}
	
	/**
	 * Returns the width
	 * @return the width
	 */
	public int getWidth()
		{return width;}
	
	/**
	 * Returns the height
	 * @return the height
	 */
	public int getHeight()
		{return height;}

   /**
    * Draws a rectangle with the current color
    * @param window the graphics object
    */
   public void draw(Graphics window)
   		{window.fillRect(getX(), getY(), getWidth(), getHeight());}
   
   /**
    * Checks if the obj is not null, of the same class,
    * and has the same position, dimensions, and color
    * @param obj the object to compare
    * @return true if they are equal, false otherwise
    */
	public boolean equals(Object obj)
	{		
		if (obj == null) 
			return false;
		
		if (getClass() != obj.getClass()) 
			return false;
		
		Piece other = (Piece) obj;
		
		if (other.getX() == xPos &&
			other.getY() == yPos &&
			other.getWidth() == width &&
			other.getHeight() == height)
			return true;
		else 
			return false;
	}
	
	/**
	 * Shifts the object to make motion
	 */
	public void scroll()
		{setX(getX() - SCROLL_SPEED);}
	
	/**
	 * Checks if a Piece is already there
	 * @param other the object to check if it's already there
	 * @return true if other is there, false otherwise
	 */
	public boolean pieceIsThere(Piece other)
	{
		return  getX() < other.getX() + other.getWidth() &&							//check x's
				getX() + getWidth() > other.getX() &&
				getY() < other.getY() + other.getHeight() &&			 			//check y's
				getY() + getHeight() > other.getY();	
	}
	
	/**
	  * Checks if the Piece hit the left side of other
	  * @param other the object to check
	  * @return true if hit, false otherwise
	  */
	public boolean didCollideLeft(Piece other) 
	{		    
		return  getX() + getWidth() < other.getX() - 3 && 							//check x's
				getX() + getWidth() >= other.getX() + other.getWidth() &&
				getY() < other.getY() + other.getHeight() &&						//check y's
				getY() + getHeight() > other.getY();
	}
	
	/**
	  * Checks if the Piece hit the left side of other
	  * @param other the object to check
	  * @param speed the speed of the Piece
	  * @return true if hit, false otherwise
	  */
	public boolean didCollideLeft(Piece other, int speed) 
	{		    
		return  getX() + getWidth() >= other.getX() - Math.abs(speed) && 			//check x's
				getX() + getWidth() < other.getX() + other.getWidth() &&
				getY() < other.getY() + other.getHeight() &&						//check y's
				getY() + getHeight() > other.getY();
	}
	
	/**
	  * Checks if the Piece hit the right side of other
	  * @param other the object to check
	  * @return true if hit, false otherwise
	  */
	public boolean didCollideRight(Piece other) 
	{	
		return  getX() <= other.getX() + other.getWidth() + 3 &&					//check x's 
				getX() > other.getX() + other.getWidth() && 
				getY() < other.getY() + other.getHeight() &&						//check y's
				getY() + getHeight() > other.getY();
	}

	/**
	  * Checks if the Piece hit the right side of other
	  * @param other the object to check
	  * @param speed the speed of the Piece
	  * @return true if hit, false otherwise
	  */
	public boolean didCollideRight(Piece other, int speed) 
	{	
		return  getX() <= other.getX() + other.getWidth() + Math.abs(speed) &&		//check x's 
				getX() > other.getX() && 
				getY() < other.getY() + other.getHeight() &&						//check y's
				getY() + getHeight() > other.getY();
	}
		
	/**
	  * Checks if the Piece hit the top of other
	  * @param other the object to check
	  * @return true if hit, false otherwise
	  */
	public boolean didCollideTop(Piece other) 
	{			
		return  getX() < other.getX() + other.getWidth() &&							//check x's
				getX() + getWidth() > other.getX() &&	
				getY() + getHeight() >= other.getY() - 3 && 						//check y's	
				getY() + getHeight() <= other.getY();
	}
	
	/**
	  * Checks if the Piece hit the bottom of other
	  * @param other the object to check
	  * @return true if hit, false otherwise
	  */
	public boolean didCollideBottom(Piece other) 
	{			
		return  getX() < other.getX() + other.getWidth() &&							//check x's
				getX() + getWidth() > other.getX() &&	
				getY() <= other.getY() + other.getHeight() + 3 && 					//check y's	
				getY() >= other.getY() + other.getHeight();
	}
	
	/**
	 * Checks if the Piece is above other
	 * @param other the piece to see if it is below the current Piece
	 * @return true if current Piece is above other, false otherwise
	 */
	public boolean isAbove(Piece other)
	{
		return  getX() + getWidth() >= other.getX() && 
				getX() <= other.getX() + other.getWidth() &&
				getY() + getHeight() <= other.getY();
	}
	
	/**
	 * Returns the object's attributes
	 * @return the object's attributes
	 */
	public String toString()
		{return xPos + " " + yPos + " " + width + " " + height;}
}