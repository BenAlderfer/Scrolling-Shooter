/**
 * Copyright (c) 2015 Ben Alderfer
 * See the file license.txt for copying permission.
 *
 * @author Ben Alderfer
 * The generic class for living things
 * ex) player, enemy
 */
public abstract class Being extends ImageItem
{
	protected int health;
	protected String beingType;
	
	/**
	 * Default constructor
	 * Calls Block's default constructor
	 */
	public Being()
	{
		super();
		health = 1;
	}
	
	/**
	 * Constructs a Block with x, y, width, height
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param wdt the width
	 * @param hgt the height
	 */
	public Being(int x, int y, int wdt, int hgt)
	{
		super(x, y, wdt, hgt);
		health = 1;
	}
	
	/**
	 * Removes health
	 * Will not go below 0
	 * @param h the health to remove
	 */
	public void removeHealth(int h)
	{
		if (health - h > 0)
			health -= h;
		else 
			health = 0;
	}
	
	/**
	 * Sets the Being's health
	 * @param h the new health
	 */
	public void setHealth(int h)
		{health = h;}
	
	/**
	 * Returns the being's health
	 * @return health the being's health
	 */
	public int getHealth()
		{return health;}
	
	/**
	 * Returns the Being's type
	 * @return beingType the Being's type
	 */
	public String getType()
		{return beingType;}
}
