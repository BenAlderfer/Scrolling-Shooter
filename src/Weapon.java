/**
 * Copyright (c) 2015 Ben Alderfer
 * See the file license.txt for copying permission.
 *
 * @author Ben Alderfer
 * The Weapon class
 */
public class Weapon extends ImageItem
{
	private String weaponName;	//the name of the weapon

	/**
	 * Constructs a Weapon with x, y, width, height, name, and path
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param wdt the width
	 * @param hgt the height
	 * @param name the Weapon's name
	 * @param path the path to the image
	 */
	public Weapon(int x, int y, int wdt, int hgt, String name, String path)
	{
		super(x, y, wdt, hgt);
		weaponName = name;
		setImage(path);
	}

	/**
	 * Returns the Weapon's name
	 * @return weaponName the Weapon's name 
	 */	
	public String getWeaponName()
		{return weaponName;}
}