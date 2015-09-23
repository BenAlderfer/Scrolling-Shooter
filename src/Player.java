import java.awt.Graphics;

/**
 * Copyright (c) 2015 Ben Alderfer
 * See the file license.txt for copying permission.
 *
 * @author Ben Alderfer
 * The Player class
 */
public class Player extends Being
{	
	//all the possible weapon types	
	private final Weapon SWORD = new Weapon(getX() + getWidth(), getY(), 60, 50, "sword", "Pictures/Swords/sword2.png");					//x, y, width, height, name, path
	private final Weapon KNIFE = new Weapon(getX() + getWidth(), getY() + 10, 30, 25, "knife", "Pictures/knife.png");						//x, y, width, height, name, path
	private final Weapon GUN = new Weapon(getX() + getWidth(), getY() + 10, 39, 25, "gun", "Pictures/gun3.png");							//x, y, width, height, name, path
	private final Weapon RAYGUN = new Weapon(getX() + getWidth(), getY() + 10, 39, 25, "raygun", "Pictures/raygun.png");						//x, y, width, height, name, path
	private final ImageItem SHIELD = new ImageItem(getX() - 20, getY() - 10, getWidth() + 40, getHeight() + 20, "Pictures/orb.png");		//x, y, width, height, name, path
	private static final int JUMP_HEIGHT = 25;			//how high the player jumps each run
	
	private String name;					//the Player's name
	private int maxHealth;					//the max health of the player
	private int knifeDelay = -1;			//delays the switch back to gun so the knife shows
	private int poisonTimer = -1;			//the duration of poison remaining
	public int jumpDelay = 0;				//delay between jumps
	public int jumps;						//the number of times the player has jumped (max 2)
	public int jumpingTime;					//the duration the player will go up (breaks up jump to look smoother)
	public boolean hasSword = false;		//if the Player has picked up the sword
	public boolean hasRayGun = false;		//if the Player has picked up the ray gun
	public boolean isPoisoned = false;		//if the Player is poisoned
	public boolean isCrouched = false;		//if the Player is crouching
	public boolean isKnockedUp = false;		//if the Player has been knocked up by a Golem
	public boolean isDead = false;			//if the Player is dead
	private Weapon playerWeapon = GUN;		//the player's weapon, default = gun
	private ImageItem shield = null;		//the player's shield, only blocks bullets and explosions, default = null
	private int shieldHealth;				//how much health the shield has
	private int bullets = 1;				//how many bullets are available, used in the normal gun
	private int rays;						//how many rays are left for the ray gun
	private int bulletTimer;				//timer to delay getting another bullet
	private int lifeline = 1;				//the player's lifeline
	public int fireDelay = 10;				//the space in between each shot
	private Weapon lastGun = GUN;			//the last weapon equipped, saves which gun so it switches back after knifing
	
	/**
	 * Default constructor
	 */
	public Player()
	{
		super();
		setImage("Pictures/player.png");
		maxHealth = 200;
		beingType = "player";
	}
	
	/**
	 * Constructs a Player with position and dimensions
	 * @param x the x position
	 * @param y the y position
	 * @param wdt the width
	 * @param hgt the height
	 */
	public Player(int x, int y, int wdt, int hgt)
	{
		super(x, y, wdt, hgt);
		setImage("Pictures/player.png");
		maxHealth = 200;
		beingType = "player";
	}
	
	/**
	 * Sets the Player's name
	 * @param n the name
	 */
	public void setName(String n)
		{name = n;}
	
	/**
	 * Returns the Player's name
	 * @return name the name
	 */
	public String getName()
		{return name;}
	
	/**
	 * Sets the maximum health
	 * @param mh the max health
	 */
	public void setMaxHealth(int mh)
		{maxHealth = mh;}
	
	/**
	 * Sets the Player's health
	 * Set to max health if h >= maxHealth
	 * @param h the new health
	 */
	public void setHealth(int h)
	{
		if (h < maxHealth)
			health = h;
		else
			health = maxHealth;
	}
	
	/**
	 * Adds health
	 * Will not exceed maxHealth
	 * @param h the health to add
	 */
	public void addHealth(int h)
	{
		if (health + h < maxHealth)
			health += h;
		else
			health = maxHealth;
	}
	
	/**
	 * Sets the Player's weapon
	 * @param weaponType the type of the new weapon
	 */
	public void setWeapon(String weaponType)
	{
		if (playerWeapon.getWeaponName().equals("gun") || playerWeapon.getWeaponName().equals("raygun"))		//saves the last gun used so it switches back properly after knifing
			lastGun = playerWeapon;
		
		switch (weaponType)
		{
			case "gun" : playerWeapon = GUN; break;
			case "raygun" : playerWeapon = RAYGUN; break;
			case "knife" : playerWeapon = KNIFE; knifeDelay = 5; break;
			case "sword" : playerWeapon = SWORD; knifeDelay = 5; break;
		}
		
		if (weaponType.equals("gun") || weaponType.equals("raygun"))			//if the new weapon is a gun, the last weapon becomes the same thing so it doesn't switch to last gun
			lastGun = playerWeapon;
	}
	
	/**
	 * Returns the Player's weapon
	 * @return playerWeapon the player's weapon
	 */
	public Weapon getWeapon()
		{return playerWeapon;}
	
	/**
	 * Returns the last or current gun the player equipped
	 * @return lastGun the last or current gun the player equipped
	 */
	public Weapon getLastGun()
		{return lastGun;}
		
	/**
	 * Makes a shield around the player
	 */
	public void addShield()
	{
		shield = SHIELD;
		shieldHealth = 5;
	}
	
	/**
	 * Removes the player's shield
	 * @param rm the amount to remove from shieldHealth
	 */
	public void removeShieldHealth(int rm)
	{
		if (rm >= shieldHealth)
			shield = null;
		else
			shieldHealth -= rm;
	}
	
	/**
	 * Returns the player's shield
	 * @return shield the player's shield
	 */ 
	public ImageItem getShield()
		{return shield;}
	
	/**
	 * Increases the lifeline by 1
	 */
	public void incrementLifeline() 
		{lifeline++;}
	
	/**
	 * Decreases the player's lifeline
	 * @param n the amount to decrease by
	 */
	public void decreaseLifeline(int n)
	{
		if (lifeline - n > 0)
			lifeline -= n;
		else
			lifeline = 0;
	}
	
	/**
	 * Returns the player's lifeline
	 * @return lifeline the player's lifeline
	 */
	public int getLifeline()
		{return lifeline;}
	
	/**
	 * Adds n to the amount of bullets available
	 * @param n the amount to add
	 */
	public void addBullets(int n)
		{bullets += n;}
	
	/**
	 * Adds rays to the amount available
	 * @param num the amount to add
	 */
	public void addRays(int num)
		{rays += num;}
	
	/**
	 * Returns the amount of bullets or rays left
	 * @return the amount of rays left if ray gun is equipped, bullets otherwise
	 */
	public int getAmmo()
	{
		if (playerWeapon.equals(RAYGUN))
			return rays;
		else
			return bullets;
	}
	
	/**
	 * Removes n amount of ammo depending on the gun equipped
	 * @param n the amount to remove
	 */
	public void removeAmmo(int n)
	{
		if (playerWeapon.equals(RAYGUN))
			rays -= n;
		else
			bullets -= n;
	}
	
	/**
	 * Returns the ray gun
	 * Used for the weapon drop when a Golem is killed
	 * @return RAYGUN the ray gun
	 */
	public Weapon getRayGun()
		{return RAYGUN;}
	
	/**
	 * Advances the timers and delays
	 * Changes variables based on the timers
	 */
	public void advanceTimers()
	{
		fireDelay--;		
		if (playerWeapon.getWeaponName().equals("gun") && getAmmo() == 0)			//starts bulletTimer if player is out of bullets
			bulletTimer++;
		if (bulletTimer > 19)														//adds a bullet at 20
		{
			bulletTimer = 0;
			addBullets(1);
		}
		
		if (hasRayGun && rays < 1)													//if the player runs out of rays, lose ray gun and switch to normal gun
		{
			hasRayGun = false;
			setWeapon("gun");
		}
		
		if (jumpingTime > 0)														//moves the player up if mid-jump
		{
			setY(getY() - JUMP_HEIGHT);
			jumpingTime--;
		}
		
		if (jumpingTime == 0)														//changes back to normal image at max height
		{
			if (!isPoisoned)
				setImage("Pictures/player0.png");
			else
				setImage("Pictures/playerPoison.png");
		}
			
		knifeDelay--;
		if (knifeDelay  < 1)														//the delay between switch from knife to gun, included so the knife shows
			playerWeapon = lastGun;
		
		poisonTimer--;																//sets the image back to normal when poison wears off
		if (poisonTimer < 1)
		{
			if (getPath().equals("Pictures/playerPoison.png"))
				setImage("Pictures/player0.png");
			else if (getPath().equals("Pictures/playerPoisonFly.png"))
				setImage("Pictures/playerFly.png");
			
			isPoisoned = false;
		}
	}
	
	/**
	 * Draws nothing if the player is dead
	 * Else:
	 * Draws the Player from file
	 * Draws the Player's weapon
	 * Draws a shield around the player if they have one
	 * @param window the graphics object
	 */
	public void draw(Graphics window)
	{
		if (!isDead)
		{
			if (shield != null)
			{
				shield.setPos(getX() - 20, getY() - 10);
				shield.draw(window);
			}		

			switch(playerWeapon.getWeaponName())					//adjusts weapon coordinates based current player position and type of weapon
			{
				case "gun": playerWeapon.setPos(getX() + getWidth(), getY() + 40); break;
				case "raygun": playerWeapon.setPos(getX() + getWidth(), getY() + 40); break;
				case "knife": playerWeapon.setPos(getX() + getWidth() - 5, getY() + 40); break;
				case "sword": playerWeapon.setPos(getX() + getWidth() - 5, getY() + 15); break;
			}
			
			if (isCrouched)
				playerWeapon.setY(playerWeapon.getY() + 25);
			
			playerWeapon.draw(window);
			
			super.draw(window);
		}
	}	
	
	public void poisonPlayer()
	{
		poisonTimer = 5;
		isPoisoned = true;
		
		if (getPath().equals("Pictures/player0.png"))
			setImage("Pictures/playerPoison.png");
		else if (getPath().equals("Pictures/playerFly.png"))
			setImage("Pictures/playerPoisonFly.png");
	}
	 
	 /**
	  * Checks if the Player hit the left side of other
	  * **Modified to check with speed
	  * @param other the object to check
	  * @return true if hit, false otherwise
	  */
	public boolean didCollideLeft(Piece other) 
	{		 
		return  getX() + getWidth() < other.getX() && 							//check x's
				getX() + getWidth() >= other.getX() - SCROLL_SPEED &&						
				getY() < other.getY() + other.getHeight() &&					//check y's
				getY() + getHeight() > other.getY();
	}

	/**
	  * Checks if the Player hit the right side of other
	  * **Modified to check with speed
	  * @param other the object to check
	  * @return true if hit, false otherwise
	  */
	public boolean didCollideRight(Piece other) 
	{	
		return  getX() > other.getX() + other.getWidth() && 					//check x's
				getX() <= other.getX() + other.getWidth() + 2 * SCROLL_SPEED &&					
				getY() < other.getY() + other.getHeight() &&					//check y's
				getY() + getHeight() > other.getY();
	}
	
	/**
	  * Checks if the Player hit the bottom of other
	  * @param other the object to check
	  * @return true if hit, false otherwise
	  */
	public boolean didCollideBottom(Piece other) 
	{			
		return  getX() < other.getX() + other.getWidth() &&							//check x's
				getX() + getWidth() > other.getX() &&	
				getY() <= other.getY() + other.getHeight() + JUMP_HEIGHT && 					//check y's	
				getY() >= other.getY() + other.getHeight();
	}
	
	/**
	 * If the player can jump	
	 * @return true if the player can jump
	 */
	public boolean canJump()
	{
		return jumpDelay == 0 && 
			   jumps < 2 && 
			   jumpingTime == 0;
	}
	
	/**
	 * Changes the player's image and changes the jump timers and number of jumps
	 * The shifting up is done in draw()
	 */
	public void jump()
	{
		if (jumps == 0)
			jumpDelay = 3;			//prevents overlapping jumps
		jumpingTime = 3;
		jumps++;
		setImage("Pictures/playerFly.png");
	}
}