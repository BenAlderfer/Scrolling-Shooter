import java.awt.Graphics;

/**
 * Copyright (c) 2015 Ben Alderfer
 * See the file license.txt for copying permission.
 *
 * @author Ben Alderfer
 * The Soldier enemy
 * Can either shoot or stab
 */
public class Rogue extends Being
{
	private Weapon rogueWeapon = new Weapon(getX() - 25, getY() + 20, 30, 20, "gun", "Pictures/gun3Flipped.png");			//x, y, width, height, name, path
	private int walkDelay = 10;				//slows down the walking speed
	private int knifeDelay = -1;			//delays the switch back to gun so the knife will be seen
	
	/**
	 * Default constructor
	 */
	public Rogue()
	{
		setImage("Pictures/Enemies/rogue0.png");
		setHealth(1);
		beingType = "rogue";
	}

	/**
	 * Constructs a Rogue with x, y, width, height
	 * @param x the x coordinate
 	 * @param y the y coordinate
	 * @param wdt the width
	 * @param hgt the height
	 */
	public Rogue(int x, int y, int wdt, int hgt)
	{
		super(x, y, wdt,hgt);
		setImage("Pictures/Enemies/rogue0.png");
		setHealth(1);
		beingType = "rogue";
	}
	
	/**
	 * Draws the Rogue from file
	 * Makes it look like its walking
	 * Draws the Rogue's weapon
	 * @param window the graphics object
	 */
	public void draw(Graphics window)
	{
		walkDelay--;
		knifeDelay--;
		
		if (walkDelay == 0)
		{
			if (getPath().equals("Pictures/Enemies/rogue0.png"))		//changes the Rogue's image to make it look like walking
				setImage("Pictures/Enemies/rogue1.png");
			else if (getPath().equals("Pictures/Enemies/rogue1.png"))
				setImage("Pictures/Enemies/rogue2.png");
			else	//if (getPath().equals("Pictures/Enemies/rogue2.png"))
				setImage("Pictures/Enemies/rogue0.png");
			
			walkDelay = 10;
		}
		
		if (knifeDelay == 0)
			switchWeapon();
		
		rogueWeapon.setPos(getX() - 25, getY() + 20);		
		rogueWeapon.draw(window);
		
		super.draw(window);
	}	
	
	/**
	 * Switches the weapon between a gun and a knife
	 */
	public void switchWeapon()
	{
		if (rogueWeapon.getWeaponName().equalsIgnoreCase("gun"))
		{
			rogueWeapon = new Weapon(getX() - 25, getY() + 20, 30, 20, "knife", "Pictures/knifeFlipped.png");			//x, y, width, height, name, path
			knifeDelay = 10;
		}
		else
			rogueWeapon = new Weapon(getX() - 25, getY() + 20, 30, 20, "gun", "Pictures/gun3Flipped.png");			//x, y, width, height, name, path
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
	 
	 /**
	  * Returns the Rogue's weapon
	  * @return rogueWeapon the Rogue's weapon
	  */
	 public Weapon getWeapon()
	 	{return rogueWeapon;}
}
