/**
 * Copyright (c) 2015 Ben Alderfer
 * See the file license.txt for copying permission.
 *
 * @author Ben Alderfer
 * The Soldier enemy
 * Can either shoot or stab
 */
public class Soldier extends Being
{
	/**
	 * Default constructor
	 */
	public Soldier()
		{setImage("Enemies/soldier0.png");}

	/**
	 * Constructs a Soldier with x, y, width, height
	 * @param x the x coordinate
 	 * @param y the y coordinate
	 * @param wdt the width
	 * @param hgt the height
	 */
	public Soldier(int x, int y, int wdt, int hgt)
	{
		super(x, y, wdt,hgt);
		setImage("Enemies/soldier0.png");
	}

	/**
	  * Decides how to attack the player
	  * @param player the player
	  * @return true for a gunshot, false for a knife
	  */
	 public boolean willShoot(Player player)
	 {
		if (player.getX() + player.getWidth() >= getX() - 25)
			return false;
		
		return true;
	 }
}
