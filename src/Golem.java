import java.awt.Graphics;

/**
 * Copyright (c) 2015 Ben Alderfer
 * See the file license.txt for copying permission.
 *
 * @author Ben Alderfer
 * The Golem boss
 * Is big and slow
 * Walks in front of things
 */
public class Golem extends Being
{
	private int moveDelay = 20;				//slows down the image change to make it easier to see
	private String moveType = "walking";	//what the Golem is currently doing, default = walking
	
	/**
	 * Default constructor
	 */
	public Golem()
	{
		setImage("Pictures/Enemies/golem0.png");
		setHealth(400);
		beingType = "golem";
	}

	/**
	 * Constructs a Golem with x, y, width, height
	 * @param x the x coordinate
 	 * @param y the y coordinate
	 * @param wdt the width
	 * @param hgt the height
	 */
	public Golem(int x, int y, int wdt, int hgt)
	{
		super(x, y, wdt,hgt);
		setImage("Pictures/Enemies/golem0.png");
		setHealth(400);
		beingType = "golem";
	}
	
	/**
	 * Draws the Golem from file
	 * Makes it look like its walking, attacking, or dying
	 * @param window the graphics object
	 */
	public void draw(Graphics window)
	{				
		if (moveType.equals("walking"))
		{
			if (moveDelay == 0)
			{
				switch(getPath())		//changes the Golem's image to make it look like walking
				{
					case "Pictures/Enemies/golem0.png": setImage("Pictures/Enemies/golem1.png"); break;
					case "Pictures/Enemies/golem1.png": setImage("Pictures/Enemies/golem2.png"); break;
					case "Pictures/Enemies/golem2.png": setImage("Pictures/Enemies/golem3.png"); break;
					case "Pictures/Enemies/golem3.png": setImage("Pictures/Enemies/golem4.png"); break;
					case "Pictures/Enemies/golem4.png": setImage("Pictures/Enemies/golem5.png"); break;
					case "Pictures/Enemies/golem5.png": setImage("Pictures/Enemies/golem6.png"); break;
					case "Pictures/Enemies/golem6.png": setImage("Pictures/Enemies/golem1.png"); break;
				}
				
				moveDelay = 20;
			}
		}
		
		else if (moveType.equals("attacking"))
		{
			if (moveDelay == 0)
			{
				switch(getPath())		//changes the Golem's image to make it look like attacking
				{
					case "Pictures/Enemies/golemAtk0.png": setImage("Pictures/Enemies/golemAtk1.png"); break;
					case "Pictures/Enemies/golemAtk1.png": setImage("Pictures/Enemies/golemAtk2.png"); break;
					case "Pictures/Enemies/golemAtk2.png": setImage("Pictures/Enemies/golemAtk3.png"); break;
					case "Pictures/Enemies/golemAtk3.png": setImage("Pictures/Enemies/golemAtk4.png"); setX(getX() - getWidth()); setWidth(getWidth() * 2); break;			//increases size to look like stretching out
					case "Pictures/Enemies/golemAtk4.png": setImage("Pictures/Enemies/golemAtk5.png"); break;
					case "Pictures/Enemies/golemAtk5.png": setImage("Pictures/Enemies/golemAtk6.png"); setWidth(getWidth() / 2); break;
					case "Pictures/Enemies/golemAtk6.png": setImage("Pictures/Enemies/golem0.png"); moveType = "walking"; moveDelay = 20; break;
				}
				
				moveDelay = 20;
			}
		}
			
		
		else if (moveType.equals("dying"))
			if (moveDelay == 0)
			{
				switch(getPath())		//changes the Golem's image to make it look like dying
				{
					case "Pictures/Enemies/golemDie0.png": setImage("Pictures/Enemies/golemDie1.png"); setHeight((int)(.78 * getHeight())); break;
					case "Pictures/Enemies/golemDie1.png": setImage("Pictures/Enemies/golemDie2.png"); setHeight((int)(.85 * getHeight())); break;
					case "Pictures/Enemies/golemDie2.png": setImage("Pictures/Enemies/golemDie3.png"); setHeight((int)(.82 * getHeight())); break;
					case "Pictures/Enemies/golemDie3.png": setImage("Pictures/Enemies/golemDie4.png"); setHeight((int)(.77 * getHeight())); break;
					case "Pictures/Enemies/golemDie4.png": setImage("Pictures/Enemies/golemDie5.png"); setHeight((int)(.83 * getHeight())); break;
					case "Pictures/Enemies/golemDie5.png": setImage("Pictures/Enemies/golemDie6.png"); setHeight((int)(.75 * getHeight())); break;
					case "Pictures/Enemies/golemDie6.png": setImage("Pictures/Enemies/golemDie7.png"); setHeight((int)(.67 * getHeight())); break;
				}
				
				moveDelay = 10;
			}
		
		super.draw(window);
	}
	
	/**
	 * Begins the attack if the player is close
	 * @param p the player
	 */
	public void attack(Player p)
	{
		if (p.didCollideLeft(this, getWidth()))
		{
			setImage("Pictures/Enemies/golemAtk0.png");
			moveType = "attacking";
			moveDelay = 20;
		}
	}
	
	/**
	 * Begins the Golem's death
	 */
	public void die()
	{
		if (!(moveType.equals("dying")))			//so it doesnt get stuck on first image
		{
			setImage("Pictures/Enemies/golemDie0.png");
			moveType = "dying";
			moveDelay = 10;
		}
	}
	
	/**
	 * Shifts the object to make motion
	 * Slowed down for the Golem
	 */
	public void scroll()
	{
		moveDelay--;
		
		if (moveType.equals("walking") && moveDelay == 0)
			setX(getX() - (int)(2 * SCROLL_SPEED));
	}
	
	/**
	 * If the Golem is currently walking
	 * Used to check the Golem is not dying before attacking
	 * @return if the Golem is walking
	 */
	public boolean isWalking()
		{return moveType.equals("walking");}
}
