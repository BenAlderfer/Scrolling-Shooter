import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import static java.lang.Character.toUpperCase;

/**
 * Copyright (c) 2015 Ben Alderfer
 * See the file license.txt for copying permission.
 *
 * Ben Alderfer
 * The ScrollingShooter class
 * Does the graphics part of the game
 */
@SuppressWarnings({ "rawtypes", "serial"})
public class ScrollingShooter extends JPanel implements KeyListener, MouseListener
{	
	private final int PIECE_SIZE = 30;		//the standard size of a Piece
	protected Player player;                //the player
	private boolean[] keys;					//the keys pressed down
	private BufferedImage back;				//the previous image, used for double buffering
	private ArrayList<ArrayList> arrays = new ArrayList<ArrayList>();			//all the arrays that scroll and do not replace themselves
	private ArrayList<ImageItem> topWalls = new ArrayList<ImageItem>();			//top wall border
	private ArrayList<ImageItem> bottomWalls = new ArrayList<ImageItem>();		//bottom wall border
	private ArrayList<Bullet> bullets = new ArrayList<Bullet>();				//bullets
	private ArrayList<Bullet> enemyBullets = new ArrayList<Bullet>();			//enemy bullets (to prevent friendly fire)
	private ArrayList<Being> enemies = new ArrayList<Being>();					//enemies
	private ArrayList<Being> tempEnemies = new ArrayList<Being>();				//enemies during explosion/dying act
	private ArrayList<ImageItem> explosions = new ArrayList<ImageItem>();		//explosions of Robots and HoverBots
	private ArrayList<ImageItem> grass = new ArrayList<ImageItem>();			//grass
	private ArrayList<ImageItem> extraGrass = new ArrayList<ImageItem>();		//stacked grass
	private ArrayList<ImageItem> swordPlatform = new ArrayList<ImageItem>();	//the Pieces that make up the mini game to get the sword, 0 is boost, last is sword
	private ArrayList<ImageItem> spikes = new ArrayList<ImageItem>();			//spikes
	private ArrayList<ImageItem> powerUps = new ArrayList<ImageItem>();			//power ups, health boosts or shields
	private ImageItem gunDrop = null;        //the ray gun dropped by a Golem
	private int kills = 100;						//how many kills
	private int score;						//the game score
	private int swordTries = 2;				//how many tries the player has to get the sword
	private int numHoverBots;				//the number of HoverBots on the screen (max 2)
	private boolean didSaveScores = false;	//if the scores have been saved yet to prevent multiple savings
	private ImageItem background;			//the background image
	private Graphics g, graphToBack;		//Graphics objects
	private JFrame frame;					//the JFrame, used for popups and getting window dimensions
	private Clip music;						//the music clip, used to play sound
	private int loopCount = 1;				//how many times the music will loop, increases as the game runs so it doesn't stop
	private int numGolems;					//how many Golems are on the screen, max = 1
	private int playerExplosionIndex = -1;		//keeps track of which explosion was the player so it won't scroll and will remove player at end
	
	/**
	 * Scrolling Shooter constructor
	 * Makes wall, grass, and player objects 
	 * @param f the JFrame object
	 */
	public ScrollingShooter(JFrame f)
	{		
		frame = f;
		
		try
		{
			File soundFile = new File("Music/8bit Dungeon Level.wav");
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
			music = AudioSystem.getClip();
	        music.open(audioIn);
		}
		catch (UnsupportedAudioFileException e) {e.printStackTrace();}
		catch (IOException e) {e.printStackTrace();} 
		catch (LineUnavailableException e) {e.printStackTrace();}
		
		//makes the top walls
		for (int i = 0; i < f.getWidth() + 6 * PIECE_SIZE; i += PIECE_SIZE)
			topWalls.add(new ImageItem(i, 0, PIECE_SIZE, PIECE_SIZE, "Pictures/Resources/Stone/ground5.png"));
		
		//makes the bottom walls and grass
		for (int i = 0; i < f.getWidth() + 6 * PIECE_SIZE; i += PIECE_SIZE)
		{
			bottomWalls.add(new ImageItem(i, f.getHeight() - 65, PIECE_SIZE, PIECE_SIZE, "Pictures/Resources/Stone/ground5.png"));
			grass.add(new ImageItem(i, bottomWalls.get(0).getY() - PIECE_SIZE, PIECE_SIZE, PIECE_SIZE, "Pictures/Resources/Ground/ground5.png"));
		}
		
		//fills a gap
		grass.add(new ImageItem(grass.get(grass.size() - 1).getX() + PIECE_SIZE, f.getHeight() - (int)(3.5 * PIECE_SIZE), PIECE_SIZE, PIECE_SIZE, "Pictures/Resources/Ground/ground5.png"));
		
		player = new Player(50, grass.get(0).getY() - 3 * PIECE_SIZE, (int) (1.3 * PIECE_SIZE), 3 * PIECE_SIZE);			//x, y, width, height, name	
		player.setImage("Pictures/player0.png");
		player.setMaxHealth(2000);
		player.setHealth(1000);
						
		keys = new boolean[8];
		background = new ImageItem(0, PIECE_SIZE, f.getWidth(), bottomWalls.get(1).getY() - PIECE_SIZE + 5, "Pictures/space7.jpg"); 	//makes background stretch between the top and bottom walls
		setVisible(true);
		setFocusable(true);
		addKeyListener(this);
		addMouseListener(this);
		
		int delay = 20; //milliseconds
		ActionListener taskPerformer = new ActionListener(){public void actionPerformed(ActionEvent evt) {repaint();} };
		new Timer(delay, taskPerformer).start();		//runs the game
		
		//puts all arrays into an array of arrays (called arrays) for easy scrolling
		//excludes things that replace themselves or have special cases
		arrays.add(grass);
		arrays.add(extraGrass);
		arrays.add(swordPlatform);
		arrays.add(spikes);
		arrays.add(powerUps);
		arrays.add(bullets);
		arrays.add(enemyBullets);		
		arrays.add(tempEnemies);
		arrays.add(explosions);
	}

   /**
    * Draws the stuff on the screen
    * @param g some Graphics object
    */
	protected void paintComponent(Graphics g)
	{		
		if (music.getFramePosition() == music.getFrameLength())
			loopCount++;
		music.loop(loopCount);
		
		this.g = g;
		Graphics2D twoDGraph = (Graphics2D) g;
		
		back = null;
		if (back == null)	//take a snap shop of the current screen and save it as an image
		   back = (BufferedImage)(createImage(frame.getWidth(), frame.getHeight()));
		
		//create a graphics reference to the back ground image
		//draw all changes on the background image
		graphToBack = back.createGraphics();
		
		background.draw(graphToBack);
		
		player.advanceTimers();		//advances the player's timers and adds bullets, resets image, etc...		separate so shakeScreen() doesn't advance things early
		drawStuff();				///draws all the stuff on the screen, in a separate method so it can be used in shaking the screen
		
		if (explosions.size() > 0)
			advanceExplosions();							//advances explosions to next image
		
		if (player.getLifeline() < 1)
		{
			removeKeyListener(this);
			try {endGame(g);} 
			catch (IOException e) {e.printStackTrace();}
		}	
		else		//only makes and moves things if there is still lifeline left
		{  
			if (player.isPoisoned)			//hurts player and shakes screen if poisoned
				damagePlayer(5);
			
			if (player.getHealth() > 0)			//increases score and lifeline if player has health
			{
			    score++;
			    player.incrementLifeline();
			}
			
			if (gunDrop != null)				
				if (player.pieceIsThere(gunDrop))		//if the player hits the ray gun drop
				{
					player.hasRayGun = true;
					player.addRays(40);
					gunDrop = null;
				}
			
			shiftUpIfNeeded();				//moves up any blocks stuck in stuff
			shiftObjects();					//scroll all objects to make it seem like movement
			
			checkGolemAttack();				//checks if the Golem attacked and hit the player
			checkGolemDeath();				//checks if the Golem died and drops a ray gun
			
			if ((int) (Math.random() * 25) == 1)				//makes stacked grass randomly
				makeExtraGrass();
			
			if ((int) (Math.random() * 75) == 1)				//randomly makes spike traps
				makeSpike();
				
			makeEnemies();										//randomly makes enemies			
			makePowerUps();										//randomly makes healthBoosts or shields
			
			for (int i = powerUps.size() - 1; i >= 0; i--)	//checks healthBoosts or shields for collision with player
			{
				if (player.didCollideLeft(powerUps.get(i)) || player.pieceIsThere(powerUps.get(i)) || player.didCollideRight(powerUps.get(i)))
				{
					if (powerUps.get(i).getPath().equals("Pictures/health.png"))
					{
						player.addHealth(50);
						powerUps.remove(i);
					}
					else if (powerUps.get(i).getPath().equals("Pictures/shield2.png"))
					{
						player.addShield();
						powerUps.remove(i);
					}
				}
			}
			
			for (int i = 0; i < enemies.size(); i++)			//robots have a 50% chance of jumping over bullets
				if (enemies.get(i) instanceof Robot)
					if (bullets.size() > 0)
					{
						Bullet closest = bullets.get(0);
						for (int j = 1; j < bullets.size(); j++)
							if (bullets.get(j).getX() > closest.getX() && bullets.get(j).didCollideLeft(enemies.get(i), bullets.get(j).getXSpeed()))
								closest = bullets.get(j);
								
						((Robot)enemies.get(i)).attemptEvadeBullet(closest);
					}
					
			applyGravity(player);		//checks if the player needs to fall
			
			for (int i = 0; i < grass.size(); i++)				//applies gravity to grass
				applyGravity(grass.get(i));
			
			for (int i = 0; i < extraGrass.size(); i++)			//applies gravity to extraGrass
				applyGravity(extraGrass.get(i));
			
			for (int i = 0; i < spikes.size(); i++)				//applies gravity to spikes
				applyGravity(spikes.get(i));
			
			for (int i = 0; i < powerUps.size(); i++)			//applies gravity to healthBoosts
				applyGravity(powerUps.get(i));
			
			for (int i = 0; i < enemies.size(); i++)			//applies gravity to most enemies, reverses the direction of HoverBots if they hit something
				if (!(enemies.get(i) instanceof HoverBot))
					applyGravity(enemies.get(i));
				else
				{
					if (((HoverBot) enemies.get(i)).getSpeed() > 0)
						((HoverBot) enemies.get(i)).checkDirection(getGround(enemies.get(i)));
					else
						for (int j = topWalls.size() - 1; j > 0; j--)
							if (topWalls.get(j).isAbove(enemies.get(i)))
							{
								((HoverBot) enemies.get(i)).checkDirection(topWalls.get(j));
								break;
							}	
				}
			
			if (gunDrop != null)				//applies gravity to the ray gun drop
				applyGravity(gunDrop);
			
			if (swordPlatform.size() > 0)
				applyGravity(swordPlatform.get(0));					//applies gravity to the first part of sword platform, affects rest as one is removed
			if (swordPlatform.size() == 2)
				applyGravity(swordPlatform.get(1));					//drops down sword with last platform
			
			if (swordTries > 0)
				if ((int) (Math.random() * 1000) == 1 && swordPlatform.size() == 0)		//sets up the mini game to get the sword
				{	
					makeSwordPlatform();
					swordTries--;
				}
			
			if (swordPlatform.size() > 0)
				checkHitBoost();	 			//sends player up if they hit the boost
			
			if (swordPlatform.size() > 0)
				checkGrabbedSword(); 			//clears the platform if the player gets the sword
			
			if (spikes.size() > 0)
				checkHitSpike();				//checks if the player or an enemy hit a spike
			
			checkBullets();						//checks if a bullet needs to be removed
			checkKeys();						//checks for keypresses and does the related action
			checkStab();						//checks if an enemy is stabbed
			removeDead();						//removes dead enemies
			enemyAttack();						//enemies attack, placed down here so they don't attack if they should be dead
			
			for (int i = 0; i < enemyBullets.size(); i++)		//increases rocket speed each time, swaps fireBlast images
			{
				if (enemyBullets.get(i).getPath().equals("Pictures/rocket.png"))
					enemyBullets.get(i).setXSpeed(enemyBullets.get(i).getXSpeed() - 1);
				
				else if (enemyBullets.get(i).getPath().equals("Pictures/rightFireBlast0.png"))
					enemyBullets.get(i).setImage("Pictures/rightFireBlast1.png");
				
				else if (enemyBullets.get(i).getPath().equals("Pictures/rightFireBlast1.png"))
					enemyBullets.get(i).setImage("Pictures/rightFireBlast0.png");
			}
						
			if (player.getHealth() <= 0 || player.getWeapon().getX() + player.getWeapon().getWidth() <= 0)	//if the player runs out of health or goes off the screen
				player.decreaseLifeline(5);
		}
		
		twoDGraph.drawImage(back, null, 0, 0);
	}
      
   /**
    * Generates a random color
    * @return a random color
    */
   private Color randomColor()
   {
	   	int r = (int) (1 + Math.random() * 255);
 		int g = (int) (1 + Math.random() * 255);
 		int b = (int) (1 + Math.random() * 255);
 		return new Color(r, g, b);
   }
   
   /**
    * Shifts all the objects left to show motion
    */
   private void shiftObjects()
   {
	   //top walls
	   for (int i = 0; i < topWalls.size(); i++)
	   {
		    topWalls.get(i).scroll();
			if (topWalls.get(i).getX() + topWalls.get(i).getWidth() < 0)		//replaces the wall if it goes off the screen
			{
				topWalls.remove(i);
				topWalls.add(new ImageItem(topWalls.get(topWalls.size() - 1).getX() + PIECE_SIZE, 0, PIECE_SIZE, PIECE_SIZE, "Pictures/Resources/Stone/ground5.png"));
				break;		//only 1 will be removed at most per run
			}
	   }
	   
	   //bottom walls, replaces grass here because grass may be blown up earlier
	   for (int i = 0; i < bottomWalls.size(); i++)
	   {
		    bottomWalls.get(i).scroll();
			if (bottomWalls.get(i).getX() + bottomWalls.get(i).getWidth() < -PIECE_SIZE)		//replaces the wall and grass if it goes off the screen, goes farther so it can carry the player
			{
				bottomWalls.remove(i);
				bottomWalls.add(new ImageItem(bottomWalls.get(bottomWalls.size() - 1).getX() + PIECE_SIZE, frame.getHeight() - 65, PIECE_SIZE, PIECE_SIZE, "Pictures/Resources/Stone/ground5.png"));
				grass.add(new ImageItem(bottomWalls.get(bottomWalls.size() - 1).getX() + PIECE_SIZE, bottomWalls.get(0).getY() - PIECE_SIZE, PIECE_SIZE, PIECE_SIZE, "Pictures/Resources/Ground/ground5.png"));
			}
	   }
	   
	   for (int i = 0; i < arrays.size(); i++)			//moves everything else besides enemies
		   for (int j = 0; j < arrays.get(i).size(); j++)
		   {
			   ((Piece) arrays.get(i).get(j)).scroll();
			   
			   if (((Piece) arrays.get(i).get(j)).getX() + ((Piece) arrays.get(i).get(j)).getWidth() < -PIECE_SIZE)		//removes stuff as it goes off screen, goes farther so it can carry the player
				   arrays.get(i).remove(j);
		   }
	   
	   if (playerExplosionIndex >= 0)
		   explosions.get(playerExplosionIndex).setX(explosions.get(playerExplosionIndex).getX() + Piece.SCROLL_SPEED);		//moves the player's explosion right to counter the scrolling, easier than looking for it above
	  
	   for (int i = 0; i < enemies.size(); i++)						//shifts the enemies, may have special actions
	   {
		   if (enemies.get(i) instanceof Spider)					//Spiders move 2x as fast and climb things
		   {
			   for (int j = 0; j < grass.size(); j++)
			   {
				   if (enemies.get(i).isAbove(grass.get(j)))		//if the Spider above the bottom layer of grass only the stacked part will be checked
				   {
					      if (extraGrass.size() > 0)
					      		((Spider)enemies.get(i)).chooseMove(extraGrass);
					      break;
				   }
				   
				   else if (j == grass.size() - 1 && !enemies.get(i).isAbove(grass.get(j)))		//if its on the last item and above grass, must be on bottomWalls
					   ((Spider)enemies.get(i)).chooseMove(grass);
			   }
		   }
		   else if (enemies.get(i) instanceof HoverBot)						//HoverBots will only move left until into view
			   ((HoverBot) enemies.get(i)).scroll(getWidth());		//sends width so it doesn't move too far
		   
		   else
			   enemies.get(i).scroll();
		   
		   if (enemies.get(i).getX() + enemies.get(i).getWidth() < 0)
			   enemies.remove(i);
	   }
	   
	   for (int i = 0; i < grass.size(); i++)			//moves the player back if they get stuck inside
		   if (grass.get(i).pieceIsThere(player))
		   {
			   if (player.getX() < grass.get(i).getX() + grass.get(i).getWidth() / 2)	//if the player is stuck on the left side
				   player.setX(grass.get(i).getX() - player.getWidth());
			   else		//player stuck on the right side
				   player.setX(grass.get(i).getX() + grass.get(i).getWidth());
			   break;
		   }
	   
	   for (int i = 0; i < extraGrass.size(); i++)		//moves the player back if they get stuck inside
		   if (extraGrass.get(i).pieceIsThere(player))
		   {
			   if (player.getX() < extraGrass.get(i).getX() + extraGrass.get(i).getWidth() / 2)	//if the player is stuck on the left side
				   player.setX(extraGrass.get(i).getX() - player.getWidth());
			   else		//player stuck on the right side
				   player.setX(extraGrass.get(i).getX() + extraGrass.get(i).getWidth());
			   break;
		   }
	   
	   golemshift: 
		   for (int i = 0; i < enemies.size(); i++)
			   for (int j = 0; j < grass.size(); j++)			//moves the Golem up if stuck inside
				   if (grass.get(j).pieceIsThere(enemies.get(i)))
				   {
					   enemies.get(i).setY(grass.get(j).getY() - enemies.get(i).getHeight());
					   break golemshift;
				   }
	   
	   if (gunDrop != null)
	   {
		   gunDrop.scroll();
		   if (gunDrop.getX() + gunDrop.getWidth() < 0)
			   gunDrop = null;
	   }
   }
   
   /**
    * Finds the highest ImageItem below p
    * @param p the ImageItem to check below
    * @return ground the highest ImageItem below p
    */
   private ImageItem getGround(ImageItem p)
   {
		 //find ground height below p
		 ImageItem ground = bottomWalls.get(0);					//bottom walls are the ground height if nothing else is there
			 
		 for (int i = 0; i < swordPlatform.size() - 1; i++)		//checks sword platform
				 if (p.isAbove(swordPlatform.get(i)))
					 	ground = swordPlatform.get(i);
			 	
		 if (extraGrass.size() > 0 && extraGrass.get(0).getY() < ground.getY())
			 for (int i = 0; i < extraGrass.size(); i++)		//checks stacked grass
				 if (p.isAbove(extraGrass.get(i)))
					 	ground = extraGrass.get(i);
			 
		 if ((powerUps.contains(p) || p.getPath().equalsIgnoreCase("Pictures/Resources/Ground/ground5.png")) && spikes.size() > 0)
			 for (int i = 0; i < spikes.size(); i++)			//checks spikes
				 if (spikes.get(i).getY() < ground.getY() && p.isAbove(spikes.get(i)))
					 	ground = spikes.get(i);
			
		 if (grass.get(0).getY() < ground.getY())
			 for (int i = 0; i < grass.size(); i++)				//checks bottom level of grass
				 if (p.isAbove(grass.get(i)))
					 	ground = grass.get(i);
		 
		 return ground;
   }
   
   /**
    * Applies gravity to the param p
    * Adjusts the image and number of jumps if p is the player
    * @param p the piece to apply gravity to
    */
   private void applyGravity(ImageItem p)
   {
		int groundHeight = getGround(p).getY();
		 		
		if (p.getY() + p.getHeight() + p.getGravity() <= groundHeight)		//falls if not on ground
		{
			if (!(p instanceof Player))
			{
				p.setY(p.getY() + p.getGravity());				//moves down
				p.setGravity(p.getGravity() + 1);				//increases fall speed
			}
				
			else		//if (p instanceof Player)
			{
				if (player.jumpingTime < 1)			//player will not drop until they reach max jump height
				{
					p.setY(p.getY() + p.getGravity());				//moves down
					p.setGravity(p.getGravity() + 1);				//increases fall speed
				}
				
				if (p.getY() + p.getHeight() == groundHeight)		//resets values for player if they hit the ground
				{
					player.jumps = 0;
					player.jumpDelay = 5;
					p.resetGravity();
				}
			}
		}
			 
		else if (p.getY() + p.getHeight() < groundHeight)		//if not on ground but adding gravity would put the item in something
		{
			p.setY(groundHeight - p.getHeight());		//set after player's image change because the flying image is bigger
			
			if (p instanceof Player)		//resets values for player
			{		 
				player.jumps = 0;
				player.jumpDelay = 5;
				p.resetGravity();
			} 
		}
   }
   
   /**
    * Shifts the objects so they are in the proper order (shown below as they appear in game
    * 
    * healthBoosts, shields (same plane but not touching)
    * spikes, enemies (same plane but not touching)
    * extraGrass
    * grass
    * bottomWalls (will always be on bottom, no need to check)
    * 
    */
   private void shiftUpIfNeeded()
   {
	   for (int i = 0; i < grass.size(); i++)		//shifts everything above grass
	   {
		     for (int j = 0; j < extraGrass.size(); j++)		//shifts extraGrass
		    	 if (grass.get(i).pieceIsThere(extraGrass.get(j)))
		    	 {
		    		 extraGrass.get(j).setY(grass.get(i).getY() - extraGrass.get(j).getHeight());
		    		 break;
		    	 }
		     
		     for (int j = 0; j < spikes.size(); j++)			//shifts spikes
		    	 if (grass.get(i).pieceIsThere(spikes.get(j)))
		    	 {
		    		 spikes.get(j).setY(grass.get(i).getY() - spikes.get(j).getHeight());
		    		 break;
		    	 }
		     
		     for (int j = 0; j < enemies.size(); j++)			//shifts enemies
		    	 if (grass.get(i).pieceIsThere(enemies.get(j)) && !(enemies.get(j) instanceof HoverBot) && !(enemies.get(j) instanceof Golem))
		    	 {
		    		 enemies.get(j).setY(grass.get(i).getY() - enemies.get(j).getHeight());
		    		 break;
		    	 }
		     
		     for (int j = 0; j < powerUps.size(); j++)		//shifts powerUps
		    	 if (grass.get(i).pieceIsThere(powerUps.get(j)))
		    	 {
		    		 powerUps.get(j).setY(grass.get(i).getY() - powerUps.get(j).getHeight());
		    		 break;
		    	 }
	   }
	   
	   for (int i = 0; i < extraGrass.size(); i++)		//shifts everything above extraGrass
	   {		     
		     for (int j = 0; j < spikes.size(); j++)			//shifts spikes
		    	 if (extraGrass.get(i).pieceIsThere(spikes.get(j)) || extraGrass.get(i).isAbove(spikes.get(j)))
		    	 {
		    		 spikes.get(j).setY(extraGrass.get(i).getY() - spikes.get(j).getHeight());
		    		 break;
		    	 }
		     
		     for (int j = 0; j < enemies.size(); j++)			//shifts enemies
		    	 if (extraGrass.get(i).pieceIsThere(enemies.get(j)) && !(enemies.get(j) instanceof HoverBot) && !(enemies.get(j) instanceof Golem))
		    	 {
		    		 enemies.get(j).setY(extraGrass.get(i).getY() - enemies.get(j).getHeight());
		    		 break;
		    	 }
		     
		     for (int j = 0; j < powerUps.size(); j++)		//shifts powerUps
		    	 if (extraGrass.get(i).pieceIsThere(powerUps.get(j)))
		    	 {
		    		 powerUps.get(j).setY(extraGrass.get(i).getY() - powerUps.get(j).getHeight());
		    		 break;
		    	 }
	   }
	   
	   for (int i = 0; i < spikes.size(); i++)		//removes or shifts everything above spikes
	   {		     		     
		     for (int j = enemies.size() - 1; j >= 0; j--)			//removes enemies
		    	 if (spikes.get(i).pieceIsThere(enemies.get(j)))
		    	 {
		    		 if (!(enemies.get(j) instanceof HoverBot || enemies.get(j) instanceof Golem))
		    			 enemies.remove(j);
		    		 break;
		    	 }
		     
		     for (int j = 0; j < powerUps.size(); j++)		//shifts healthBoosts
		    	 if (spikes.get(i).pieceIsThere(powerUps.get(j)))
		    	 {
		    		 powerUps.get(j).setY(spikes.get(i).getY() - powerUps.get(j).getHeight());
		    		 break;
		    	 }
	   }
		 
   }
   
   /**
    * Draws the score board on the bottom
    * @param g the graphics object
    * @param g2 the background graphics object
    */
   private void drawScores(Graphics g, Graphics g2)
   {		 
	 g2.setColor(Color.yellow);
	 g2.setFont(g.getFont().deriveFont(30f));
	 
	 g2.drawString("Score: " + score, 28, 25);  	 					//score  
	 g2.drawString("Health: " + player.getHealth(), 250, 25);  			//health
	   g2.drawString("Lifeline: " + player.getLifeline(), 475, 25);        //lifeline (gives the player more time to collect health)
	 g2.drawString("Kills: " + kills, 725, 25); 						//kills
	 g2.drawString("Ammo: " + player.getAmmo(), 900, 25);				//bullets
   }
   
   /**
    * Checks the bullets to see if they hit/need to be removed
    */
   private void checkBullets()
   {
	  bulletCheck: for (int i = 0; i < bullets.size(); i++)		//checks player bullets
	  {
		 for (int j = 0; j < grass.size(); j++)				//checks for collision with grass
		 	if (bullets.get(i).pieceIsThere(grass.get(j)))
		 	{
		 		bullets.remove(i);
		 		break bulletCheck;
		 	}
		 	
		 for (int j = 0; j < extraGrass.size(); j++)			//checks for collision with extra grass
		 	if (bullets.get(i).pieceIsThere(extraGrass.get(j)))
		 	{
		 		bullets.remove(i);
		 		break bulletCheck;
		 	}
		 			
		 //see if a bullet went off the screen
		 if (bullets.get(i).getX() > frame.getWidth() || bullets.get(i).getX() + bullets.get(i).getWidth() < 0)
		 {
		 	bullets.remove(i);
		 	break;
		 }  
		  
	 	//see if a bullet hit an enemy
	 	for (int j = 0; j < enemies.size(); j++)
	 		if (bullets.get(i).didCollideLeft(enemies.get(j), bullets.get(i).getXSpeed()) || bullets.get(i).pieceIsThere(enemies.get(j)))	
	 		{	 			
	 			if (enemies.get(j) instanceof Golem)			//bullets do 5x damage to Golem
	 			{
	 				if (bullets.get(i).getPath().equals("Pictures/laser.png"))		//lasers do double damage
		 				enemies.get(j).removeHealth(10); 
		 			else
		 				enemies.get(j).removeHealth(5); 
	 			}
	 			else
	 			{
	 				if (bullets.get(i).getPath().equals("Pictures/laser.png"))		//lasers do double damage
		 				enemies.get(j).removeHealth(2); 
		 			else
		 				enemies.get(j).removeHealth(1); 
	 			}
	 			
	 			bullets.remove(i);					
	 			break bulletCheck;
	 		}
	 	
	 	
	  }
		 
	  bulletCheck2: for (int i = 0; i < enemyBullets.size(); i++)			//checks enemy bullets
	  {
		for (int j = 0; j < grass.size(); j++)					//checks for collision with grass
			if (enemyBullets.get(i).pieceIsThere(grass.get(j)) || enemyBullets.get(i).didCollideRight(grass.get(j), enemyBullets.get(i).getXSpeed()))
			{
				//no rocket explosions because they won't hit the grass

				enemyBullets.remove(i);
			 	break bulletCheck2;
			}
			 	
		for (int j = 0; j < extraGrass.size(); j++)			//checks for collision with extraGrass
		 	if (enemyBullets.get(i).pieceIsThere(extraGrass.get(j)) || enemyBullets.get(i).didCollideRight(extraGrass.get(j), enemyBullets.get(i).getXSpeed()))
		 	{
		 		if (enemyBullets.get(i).getPath().equals("Pictures/rocket.png"))			//rockets explode
					explosions.add(new ImageItem(extraGrass.get(i).getX() + (int) (.5 * extraGrass.get(i).getWidth()) - (int) (1.5 * PIECE_SIZE), extraGrass.get(i).getY() + (int) (.5 * extraGrass.get(i).getHeight()) - (int) (1.5 * PIECE_SIZE), 3 * PIECE_SIZE, 3 * PIECE_SIZE, "Pictures/explosions/exp1.png"));
		 		
		 		enemyBullets.remove(i);
				break bulletCheck2;
			}
			 			
		//see if a bullet went off the screen
		if (enemyBullets.get(i).getX() > frame.getWidth() || enemyBullets.get(i).getX() + enemyBullets.get(i).getWidth() < 0)
		{
			enemyBullets.remove(i);
			break;
		}  
		  
		//checks for collision with shield 
		if (player.getShield() != null)
		{
			if (enemyBullets.get(i).didCollideRight(player.getShield(), enemyBullets.get(i).getXSpeed()) || enemyBullets.get(i).pieceIsThere(player.getShield()))
			{
				if (enemyBullets.get(i).getPath().equals("Pictures/rocket.png"))			//rockets explode
					explosions.add(new ImageItem(player.getShield().getX() + (int) (.5 * player.getShield().getWidth()) - (int) (1.5 * PIECE_SIZE), player.getShield().getY() + (int) (.5 * player.getShield().getHeight()) - (int) (1.5 * PIECE_SIZE), 3 * PIECE_SIZE, 3 * PIECE_SIZE, "Pictures/explosions/exp1.png"));
		 		
				if (enemyBullets.get(i).getPath().equals("Pictures/rightBullet.png"))
					player.removeShieldHealth(1);
				else if (enemyBullets.get(i).getPath().equals("Pictures/rightFireBlast0.png") || enemyBullets.get(i).getPath().equals("Pictures/rightFireBlast1.png"))
					player.removeShieldHealth(5);
				
				enemyBullets.remove(i);
				break;
			}
		}
			  
		//see if a bullet hit the player
		else if (enemyBullets.get(i).didCollideRight(player, enemyBullets.get(i).getXSpeed()) || enemyBullets.get(i).pieceIsThere(player))	
		{
			if (enemyBullets.get(i).getPath().equals("Pictures/rocket.png"))			//rockets explode
				explosions.add(new ImageItem(player.getX() + (int) (.5 * player.getWidth()) - (int) (1.5 * PIECE_SIZE), player.getY() + (int) (.5 * player.getHeight()) - (int) (1.5 * PIECE_SIZE), 3 * PIECE_SIZE, 3 * PIECE_SIZE, "Pictures/explosions/exp1.png"));
	 		
			if (enemyBullets.get(i).getPath().equals("Pictures/rightBullet.png"))
				damagePlayer(5);
		 	else if (enemyBullets.get(i).getPath().equals("Pictures/rightFireBlast0.png") || enemyBullets.get(i).getPath().equals("Pictures/rightFireBlast1.png"))
		 		damagePlayer(25);
		 	
		 	enemyBullets.remove(i);
		 	break;
		}
	 }		 
   }
   
   /**
    * Checks if an enemy has been stabbed and hurts them
    * Sword does double damage
    */
   private void checkStab()
   {
	   for (int i = 0; i < enemies.size(); i++)							//hurts enemy if stabbed
			if ((player.getWeapon().getWeaponName().equalsIgnoreCase("knife") || 
				 player.getWeapon().getWeaponName().equalsIgnoreCase("sword")) &&
				(player.getWeapon().pieceIsThere(enemies.get(i))))
			{				
				if (enemies.get(i) instanceof Spider)
					player.poisonPlayer();
				
				if (player.getWeapon().getWeaponName().equals("knife"))
					enemies.get(i).removeHealth(1);
				else		//sword
					enemies.get(i).removeHealth(2);				//sword does double damage
			}
   }
   
   /**
    * Removes an enemy if it has no health left
    */
   private void removeDead()
   {
	   for (int i = enemies.size() - 1; i >= 0; i--)		//removes enemies if they are out of health
			if (enemies.get(i).getHealth() < 1)
			{
				if (player.getLifeline() > 0)		//to prevent scoring after death
				{
					kills++;
					
					switch(enemies.get(i).getType())	//different # points for each type of enemy
					{
						case "rogue" : score += 100;
						case "spider" : score += 200;
						case "robot" : score += 300;
						case "hoverbot" : score += 400;
						case "golem" : score += 20000;
					}
				}
				
				if (enemies.get(i) instanceof Robot || enemies.get(i) instanceof HoverBot)
				{
					explosions.add(new ImageItem(enemies.get(i).getX() + (int) (.5 * enemies.get(i).getWidth()) - (int) (1.5 * PIECE_SIZE), enemies.get(i).getY(), 3 * PIECE_SIZE, 3 * PIECE_SIZE, "Pictures/explosions/exp1.png"));
	 				tempEnemies.add(enemies.get(i));
				}
				
				if (enemies.get(i) instanceof HoverBot)
					numHoverBots--;
			
				if (enemies.get(i) instanceof Golem)			//Golem has dying images
				{
					((Golem) enemies.get(i)).die();				//begins dying process (melts)
					enemies.get(i).setHealth(100000);			//sets a high health so no additional kills/points will be added as it dies
					numGolems--;
				}
				
				else
					enemies.remove(i);							//rest are removed
			}	
   }
   
   /**
    * Creates an enemy
    * Spawns them halfway up and lets them fall
    * Note: actual chances are lower than posted, 
    * all previous enemies must fail and then they have a chance
    */
   private void makeEnemies()
   {
	   if ((int) (Math.random() * 75) == 1)						// 1.333% chance of making a Rogue
		   enemies.add(new Rogue(bottomWalls.get(bottomWalls.size() - 1).getX() + 2 * PIECE_SIZE, frame.getHeight() / 2, PIECE_SIZE, 2 * PIECE_SIZE));

	   else if ((int) (Math.random() * 100) == 1)				// 1% chance of making a Robot
		   enemies.add(new Robot(bottomWalls.get(bottomWalls.size() - 1).getX() + 2 * PIECE_SIZE, frame.getHeight() / 2, (int) (1.3 * PIECE_SIZE), (int) (2.7 * PIECE_SIZE)));	
		   
	   else if ((int) (Math.random() * 125) == 1)				// .8% chance of making a Spider, starts farther back so it has time to fall
		   enemies.add(new Spider(bottomWalls.get(bottomWalls.size() - 1).getX() + 7 * PIECE_SIZE, frame.getHeight() / 2, PIECE_SIZE, PIECE_SIZE));
		   
	   else if (numHoverBots < 2 && (int) (Math.random() * 250) == 1)				// .4% chance of making a HoverBot
	   {
		   enemies.add(new HoverBot(bottomWalls.get(bottomWalls.size() - 1).getX(), PIECE_SIZE, 2 * PIECE_SIZE, 2 * PIECE_SIZE));
		   numHoverBots++;
	   }	  
	   
	   for (int i = enemies.size() - 1; i > 0; i--)			//removes duplicates (only ones off screen because some move when in play)
	   {
		  if (enemies.get(i).getX() > frame.getWidth() && enemies.get(i - 1).getX() > frame.getWidth())
		  {
			  if (!(enemies.get(i) instanceof HoverBot) &&
					(enemies.get(i).isAbove(enemies.get(i - 1)) ||
					 enemies.get(i).pieceIsThere(enemies.get(i - 1))))
				   		if (!(enemies.get(i) instanceof Golem) && !(enemies.get(i - 1) instanceof Golem))	//Golems are not removed and do not remove anything
							enemies.remove(i);
		  }
		  else
			  break;			//since it starts at the end of the ArrayList, if one is not outside, none of the others will be
	   }
	   
	   if (swordPlatform.size() > 0)
		   for (int i = enemies.size() - 1; i >= 0; i--)										//removes any touching sword platform
				if (!(enemies.get(i) instanceof HoverBot || enemies.get(i) instanceof Golem))
					if (swordPlatform.get(0).pieceIsThere(enemies.get(i)))
						enemies.remove(i);
	   
	  if (kills > 0 && kills % 100 == 0 && numGolems == 0)			//makes a Golem every 100 kills if there are none on the screen
	  {
		  enemies.add(new Golem(bottomWalls.get(bottomWalls.size() - 1).getX() + PIECE_SIZE, bottomWalls.get(bottomWalls.size() - 1).getY() - 10 * PIECE_SIZE, 3 * PIECE_SIZE, 8 * PIECE_SIZE));
		  numGolems++;
	  }
   }
   
   /**
    * Makes the stacked grass
    * Spawns them halfway up and lets them fall
    */
   private void makeExtraGrass()
   {
	   	extraGrass.add(new ImageItem(bottomWalls.get(bottomWalls.size() - 1).getX() + 2 * PIECE_SIZE, frame.getHeight() / 2, PIECE_SIZE, PIECE_SIZE, "Pictures/Resources/Ground/ground5.png"));
		
	   	if (extraGrass.size() > 1)			//removes overlapping ones
	   		for (int i = extraGrass.size() - 1; i > 0; i--)
	   			if (extraGrass.get(i).pieceIsThere(extraGrass.get(i - 1)))
	   				extraGrass.remove(i);
	   	
	   	if (extraGrass.size() > 0 && swordPlatform.size() > 0)		//removes any above the boost block
			   if (extraGrass.get(extraGrass.size() - 1).isAbove(swordPlatform.get(0)))
				   extraGrass.remove(extraGrass.size() - 1);
   }
   
   /**
    * Makes a spike
    * Spawns them halfway up and lets them fall
    */
   private void makeSpike()
   {
		spikes.add(new ImageItem(bottomWalls.get(bottomWalls.size() - 1).getX() + 2 * PIECE_SIZE, frame.getHeight() / 2, PIECE_SIZE, PIECE_SIZE, "Pictures/spikes.png"));
			
		if (spikes.size() > 1 && //prevents overlap, must be at least 1 apart
		    spikes.get(spikes.size() - 2).getX() + spikes.get(spikes.size() - 2).getWidth() + 1 >= spikes.get(spikes.size() - 1).getX())
				spikes.remove(spikes.size() - 1);
				
		if (spikes.size() > 0 && swordPlatform.size() > 0 && spikes.get(spikes.size() - 1).isAbove(swordPlatform.get(0)))		//removes any above the boost block
			spikes.remove(spikes.size() - 1);
   }
   
   /**
    * Makes a health boost or shield
    * Spawns them halfway up and lets them fall
    */
   private void makePowerUps()
   {
	   //spawns them halfway up and lets them fall, easier than shifting everything up
	   if ((int) (Math.random() * 150) == 1)			//healthBoost
	   		powerUps.add(new ImageItem(bottomWalls.get(bottomWalls.size() - 1).getX() + 2 * PIECE_SIZE, frame.getHeight() / 2, 20, 20, "Pictures/health.png"));
	   
	   else if ((int) (Math.random() * 300) == 1)		//shield
		   powerUps.add(new ImageItem(bottomWalls.get(bottomWalls.size() - 1).getX() + 2 * PIECE_SIZE, frame.getHeight() / 2, 20, 20, "Pictures/shield2.png"));

	   if (powerUps.size() > 1 && //prevents overlap, must be at least 50 apart
		   powerUps.get(powerUps.size() - 2).getX() + powerUps.get(powerUps.size() - 2).getWidth() + 50 >= powerUps.get(powerUps.size() - 1).getX())
		   			powerUps.remove(powerUps.size() - 1);
	   
	   if (powerUps.size() > 0 && swordPlatform.size() > 0 && powerUps.get(powerUps.size() - 1).isAbove(swordPlatform.get(0)))		//removes any above the boost block
		   powerUps.remove(powerUps.size() - 1);
   }
   
   /**
    * Sets up the Pieces to try for the sword
    * 1 ground level boost Piece
    * 7 floating Pieces
    * sword in final Piece
    * 
    * Removes anything in the way
    */
   private void makeSwordPlatform()
   {
	  //makes the ground level boost Piece
	  swordPlatform.add(new ImageItem(bottomWalls.get(bottomWalls.size() - 1).getX() + 2 * PIECE_SIZE, frame.getHeight() / 2, PIECE_SIZE, PIECE_SIZE, "Pictures/boostBlock.png"));	//x, y, width, height, path
	  
	  //first floating Piece, needs special placement for boost to work
	   swordPlatform.add(new ImageItem(swordPlatform.get(0).getX() + 15, frame.getHeight() / 3, PIECE_SIZE, 5, "Pictures/redBlock.png"));
	  
	  //rest of floating Pieces
	  for (int i = 1; i <= 6; i++)
		  swordPlatform.add(new ImageItem(swordPlatform.get(swordPlatform.size() - 1).getX() + 100, swordPlatform.get(swordPlatform.size() - 1).getY() - 20, PIECE_SIZE, 5, "Pictures/redBlock.png"));
		  
	  //the sword
	  swordPlatform.add(new Weapon(swordPlatform.get(swordPlatform.size() - 1).getX() + 6, swordPlatform.get(swordPlatform.size() - 1).getY() - 55, 20, 60, "sword", "Pictures/Swords/swordDown.png"));			//x, y, width, height, name, path
	  
	  //removes all objects in the way
	  for (int i = extraGrass.size() - 1; i >= 0; i--)
		if (swordPlatform.get(0).pieceIsThere(extraGrass.get(i)))
			extraGrass.remove(i);
		
	  for (int i = spikes.size() - 1; i >= 0; i--)
		if (swordPlatform.get(0).pieceIsThere(spikes.get(i)))
			spikes.remove(i);
		
	  for (int i = enemies.size() - 1; i >= 0; i--)
		if (!(enemies.get(i) instanceof HoverBot || enemies.get(i) instanceof Golem))
			if (swordPlatform.get(0).pieceIsThere(enemies.get(i)))
				enemies.remove(i);
		
	  for (int i = powerUps.size() - 1; i >= 0; i--)
		if (swordPlatform.get(0).pieceIsThere(powerUps.get(i)))
			powerUps.remove(i);
   }
   
   /**
    * Checks if the player hit a spike
    * Removes an enemy that touches one
    */
   private void checkHitSpike()
   {
	   for (int i = 0; i < spikes.size(); i++)			//if the player hit a spike
		   if (player.pieceIsThere(spikes.get(i)))
		   {
			   damagePlayer(10);
			   break;
		   }
   }
   
   /**
    * Gives each enemy a chance to attack
    */
   private void enemyAttack()
   {
	   for (int i = 0; i < enemies.size(); i++)
	   {
		   if (enemies.get(i).getX() < frame.getWidth() && enemies.get(i).getX() + enemies.get(i).getWidth() > player.getX())		//won't attack if off screen or the player is past them
		   {
			   if (enemies.get(i) instanceof Rogue) 
			   {
				   if ((int) (Math.random() * 50) == 0) 
				   {
					     if (((Rogue)enemies.get(i)).willShoot(player))
					    	 enemyBullets.add(new Bullet(((Rogue)enemies.get(i)).getWeapon().getX() - 20, ((Rogue)enemies.get(i)).getWeapon().getY(), 20, 5, -25, "Pictures/rightBullet.png"));		//x, y, width, height, speed, path
					     else
					     {
					    	 ((Rogue)enemies.get(i)).switchWeapon();
					    	 damagePlayer(25);
					     }
				   }				   
			   }
			   
			   else if (enemies.get(i) instanceof Robot)   
			   {
				   if ((int) (Math.random() * 100) == 0)
					   if (player.getY() + player.getHeight() <= enemies.get(i).getY() + enemies.get(i).getHeight() && 			//only fires if lined up
						player.getY() + player.getHeight() >= enemies.get(i).getY() + enemies.get(i).getHeight() / 2 &&
						player.getX() < enemies.get(i).getX())
						{
							enemyBullets.add(new Bullet(enemies.get(i).getX(), enemies.get(i).getY() + 10, 20, 5, -5, "Pictures/rocket.png"));		//x, y, width, height, speed, path
							enemies.get(i).setImage("Pictures/Enemies/robotFire.png");
						}
			   }
			   
			   else if (enemies.get(i) instanceof Spider) 
			   {
				   if (enemies.get(i).pieceIsThere(player))
					   player.poisonPlayer();
			   }
			   
			   else if (enemies.get(i) instanceof HoverBot) 
			   {
				   if ((int) (Math.random() * 250) == 0)
				   {
					   enemyBullets.add(new Bullet(enemies.get(i).getX(), enemies.get(i).getY() + 25, 30, 50, -25, "Pictures/rightFireBlast0.png"));		//x, y, width, height, speed, path					   
					   enemies.get(i).setImage("Pictures/Enemies/hoverbotFire.png");
				   }
			   }
			   
			   else if (enemies.get(i) instanceof Golem) 
				   if (((Golem)enemies.get(i)).isWalking())			//only attacks if it was just walking
					   ((Golem)enemies.get(i)).attack(player);
		   } 
	   }	
   }
   
   /**
    * Advances the explosions to next image
    * Removes enemy and explosion if finished
    * Hurts the player or other enemies around
    */
   private void advanceExplosions()
   {
	   for (int i = explosions.size() - 1; i >= 0; i--)
		   switch(explosions.get(i).getPath())
		   {
		   		case "Pictures/explosions/exp1.png": explosions.get(i).setImage("Pictures/explosions/exp2.png"); break;
		   		case "Pictures/explosions/exp2.png": explosions.get(i).setImage("Pictures/explosions/exp3.png"); break;
		   		case "Pictures/explosions/exp3.png": explosions.get(i).setImage("Pictures/explosions/exp4.png"); break;
		   		case "Pictures/explosions/exp4.png": explosions.get(i).setImage("Pictures/explosions/exp5.png"); break;
		   		case "Pictures/explosions/exp5.png": explosions.get(i).setImage("Pictures/explosions/exp6.png"); break;
		   		case "Pictures/explosions/exp6.png": explosions.get(i).setImage("Pictures/explosions/exp7.png"); break;
		   		case "Pictures/explosions/exp7.png": explosions.get(i).setImage("Pictures/explosions/exp8.png"); break;
		   		case "Pictures/explosions/exp8.png": explosions.get(i).setImage("Pictures/explosions/exp9.png"); break;
		   		case "Pictures/explosions/exp9.png": explosions.get(i).setImage("Pictures/explosions/exp10.png"); break;
		   		case "Pictures/explosions/exp10.png": explosions.get(i).setImage("Pictures/explosions/exp11.png"); break;
		   		case "Pictures/explosions/exp11.png": explosions.get(i).setImage("Pictures/explosions/exp12.png"); break;
		   		case "Pictures/explosions/exp12.png": explosions.get(i).setImage("Pictures/explosions/exp13.png"); break;
		   		case "Pictures/explosions/exp13.png": explosions.get(i).setImage("Pictures/explosions/exp14.png"); break;
		   		case "Pictures/explosions/exp14.png": explosions.get(i).setImage("Pictures/explosions/exp15.png"); break;
		   		case "Pictures/explosions/exp15.png": explosions.get(i).setImage("Pictures/explosions/exp16.png"); break;
		   		case "Pictures/explosions/exp16.png": explosions.get(i).setImage("Pictures/explosions/exp17.png"); break;
		   		case "Pictures/explosions/exp17.png": explosions.get(i).setImage("Pictures/explosions/exp18.png"); break;
		   		case "Pictures/explosions/exp18.png": explosions.get(i).setImage("Pictures/explosions/exp19.png"); break;
		   		case "Pictures/explosions/exp19.png": explosions.get(i).setImage("Pictures/explosions/exp20.png"); break;
		   		
		   		case "Pictures/explosions/exp20.png": 	//final image, removes stuff touching it and hurts player
		   		{		
		   			if (playerExplosionIndex == i)		//if the current explosion is the player's
		   				player.isDead = true;
		   			
		   			else if (player.getShield() != null)
		   			{
		   				if (player.getShield().pieceIsThere(explosions.get(i)))
		   					player.removeShieldHealth(5);
		   			}
		   			else if (player.pieceIsThere(explosions.get(i)))
		   				damagePlayer(50);
		   			
		   			for (int j = tempEnemies.size() - 1; j >= 0; j--)
		   				if (tempEnemies.get(j).pieceIsThere(explosions.get(i)))
		   					tempEnemies.remove(j);
		   			
			   		for (int j = enemies.size() - 1; j >= 0; j--)
			   			if (enemies.get(j).pieceIsThere(explosions.get(i)))
			   				enemies.get(j).removeHealth(25);
		   			
		   			for (int j = powerUps.size() - 1; j >= 0; j--)
		   				if (powerUps.get(j).pieceIsThere(explosions.get(i)))
		   					powerUps.remove(j);
		   			
		   			for (int j = extraGrass.size() - 1; j >= 0; j--)
		   				if (extraGrass.get(j).pieceIsThere(explosions.get(i)))
		   					extraGrass.remove(j);
		   			
		   			for (int j = grass.size() - 1; j >= 0; j--)
		   				if (grass.get(j).pieceIsThere(explosions.get(i)))
		   					grass.remove(j);
		   			
		   			for (int j = spikes.size() - 1; j >= 0; j--)
		   				if (spikes.get(j).pieceIsThere(explosions.get(i)))
		   					spikes.remove(j);
		   			
		   			if (explosions.size() > 0)
		   			{
		   				explosions.remove(i); 
		   				playerExplosionIndex--;
		   			}
		   			break;
		   		}
		   } 
   }
   
   /**
    * Damages the player and shakes the screen
    * @param damage the amount of damage
    */
   private void damagePlayer(int damage)
   {
	   player.removeHealth(damage);
	   shakeScreen();   
   }
   
   /**
    * Shakes the screen
    */
   private void shakeScreen()
   {
		 //shifts all up
		 for (ArrayList a : arrays)
			 for (Object x : a)
				 ((Piece) x).setY(((Piece) x).getY() - 2);
			   
		 for (Piece x : topWalls)
			 x.setY(x.getY() - 2);
		 
		 for (Piece x : bottomWalls)
			 x.setY(x.getY() - 2);
			   
		 for (Piece x : enemies)
			 x.setY(x.getY() - 2);
			   
		 player.setY(player.getY() - 2);  
		   
		 //draws all so the shift can be seen
		 drawStuff();  		   
			   
		 //shifts all down
		 for (ArrayList a : arrays)
			 for (Object x : a)
		   ((Piece) x).setY(((Piece) x).getY() + 2);
			   
		 for (Piece x : topWalls)
			 x.setY(x.getY() + 2);
			   
		 for (Piece x : bottomWalls)
			 x.setY(x.getY() + 2);
			   
		 for (Piece x : enemies)
			 x.setY(x.getY() + 2);
			   
		 player.setY(player.getY() + 2);
   }
   
   /**
    * Checks for key presses and does the related action
    */
   private void checkKeys()
   {
	   if (keys[0] && player.getX() > 0)					//move player left
		{
			if (extraGrass.size() == 0)
				player.setX(player.getX() - 2 * Piece.SCROLL_SPEED);
			else
			{
				boolean hitGrassRight = false;				//checks if the player hit any grass on the right (of the grass)
				for (int i = 0; i < extraGrass.size(); i++)		
					if (player.didCollideRight(extraGrass.get(i)) || player.pieceIsThere(extraGrass.get(i)))
					{
						hitGrassRight = true;
						break;
					}
				
				if (hitGrassRight == false)
					for (int i = 0; i < grass.size(); i++)		
						if (player.didCollideRight(grass.get(i)) || player.pieceIsThere(grass.get(i)))
						{
							hitGrassRight = true;
							break;
						}
				
				if((grass.size() == 0 && extraGrass.size() == 0) || !hitGrassRight)
					player.setX(player.getX() - 2 * Piece.SCROLL_SPEED);
			}			
		}
		
		boolean hitGrassLeft = false;						//checks if the player hit any grass on the left (of the grass), checks outside key press so it can be used later
		for (int i = 0; i < extraGrass.size(); i++)
			if (player.didCollideLeft(extraGrass.get(i)) || player.pieceIsThere(extraGrass.get(i)))
			{
				hitGrassLeft = true;
				break;
			}
		
		if (hitGrassLeft == false)
			for (int i = 0; i < grass.size(); i++)
				if (player.didCollideLeft(grass.get(i)) || player.pieceIsThere(grass.get(i)))
				{
					hitGrassLeft = true;
					break;
				}
		
		if (keys[1] && player.getX() + player.getWidth() + player.getWeapon().getWidth() < frame.getWidth())		//move player right			
			if ((grass.size() == 0 && extraGrass.size() == 0) || !hitGrassLeft)
				player.setX(player.getX() + Piece.SCROLL_SPEED);
		
		if (hitGrassLeft)		//shifts player if they hit left of grass
			player.scroll();

		if (player.jumpDelay > 0)				//jumpDelay is time between jumps
			player.jumpDelay--;
		
		if (keys[2] && player.canJump())		//jump
		{
			boolean hitGrassBottom = false;
			for (int i = 0; i < extraGrass.size(); i++)				//if the player will jump into a block
				if (player.didCollideBottom(extraGrass.get(i)))
				{
					player.setY(extraGrass.get(i).getY() + extraGrass.get(i).getHeight());
					hitGrassBottom = true;
					break;
				}
			
			if (!hitGrassBottom)
				player.jump();
		}		
		
		if (keys[3] && player.getAmmo() > 0 && player.fireDelay < 1)		//fires
			if (player.getWeapon().getWeaponName().equalsIgnoreCase("gun") || player.getWeapon().getWeaponName().equalsIgnoreCase("raygun"))			//can only fire a bullet if a gun is equipped
			{
				player.removeAmmo(1);
				player.fireDelay = 10;
				if (player.getWeapon().getWeaponName().equals("gun"))
					bullets.add(new Bullet(player.getWeapon().getX() + player.getWeapon().getWidth() - 20, player.getWeapon().getY(), 20, 5, 32, "Pictures/bullet.png"));			//x, y, width, height, speed, path
				else
					bullets.add(new Bullet(player.getWeapon().getX() + player.getWeapon().getWidth() - 20, player.getWeapon().getY(), 20, 5, 55, "Pictures/laser.png"));			//x, y, width, height, speed, path
			}
		
		if (keys[4])			//melee attack
		{
			if (!player.hasSword)
				player.setWeapon("knife");
			else
				player.setWeapon("sword");
		}

	   player.isCrouched = keys[5];
		
		if (keys[6])
			player.setWeapon("gun");
		
		if (keys[7] && player.hasRayGun)
			player.setWeapon("raygun");
   }
   
   /**
    * Checks if the Golem attacked and hit the player
    * Does 250 damage and sends player up 100 pixels
    */
   private void checkGolemAttack()
   {
	   for (int i = 0; i < enemies.size(); i++)
			if (enemies.get(i) instanceof Golem)
				if (enemies.get(i).getPath().equals("Pictures/Enemies/golemAtk6.png"))			//resets isKnockedUp when the Golem begins to stand up
					player.isKnockedUp = false;
				else if (enemies.get(i).getPath().equals("Pictures/Enemies/golemAtk4.png"))		//if the Golem is on the last attack image
					if (enemies.get(i).getX() - player.getX() - player.getWidth() <= 100)		//if the player is within 100 pixels
						if (player.getY() > enemies.get(i).getY() + enemies.get(i).getHeight() / 4  && !player.isKnockedUp)
						{
							damagePlayer(250);
							player.setY(player.getY() - 100);
							player.isKnockedUp = true;
						}
   }
   
   /**
    * Checks if the Golem died and drops a ray gun
    */
   private void checkGolemDeath()
   {
	   for (int i = enemies.size() - 1; i >= 0; i--)
			if (enemies.get(i) instanceof Golem)
				if (enemies.get(i).getPath().equals("Pictures/Enemies/golemDie7.png"))
				{
					if (!(player.hasRayGun))			//drops ray gun in center of Golem's body
					{
						gunDrop = player.getRayGun();
						gunDrop.setPos(enemies.get(i).getX() + enemies.get(i).getWidth() / 2, bottomWalls.get(bottomWalls.size() - 1).getY() - 2 * PIECE_SIZE - gunDrop.getHeight());
					}
					
					enemies.remove(i);						
					break;			//no need to continue since there can only be one
				}
   }
   
   /**
    * Ends the game
    * Displays failure message
    * Saves high scores to a file and displays them
    * @param g the Graphics object
    * @throws IOException throws exceptions for things like "file not found"
    */
   private void endGame(Graphics g) throws IOException
   {
	   	graphToBack.setColor(randomColor());
		graphToBack.setFont(g.getFont().deriveFont(200f));
	   graphToBack.drawString("FAILURE", frame.getWidth() / 4, frame.getHeight() / 2);
		
		if (!didSaveScores)			//so it only runs once
		{
			explosions.add(new ImageItem(player.getX() + (int) (.5 * player.getWidth()) - 3 * PIECE_SIZE, player.getY() + (int) (.5 * player.getHeight()) - 3 * PIECE_SIZE, 6 * PIECE_SIZE, 6 * PIECE_SIZE, "Pictures/explosions/exp1.png"));
			playerExplosionIndex = explosions.size() - 1;
			
			File file = new File("highscores.txt");
			Scanner sc = new Scanner(file, "UTF-8");
			String[] scores = {"", "", "", "", "", "", "", "", "", ""};
			for (int i = 0; i < scores.length; i++)			//reads the highscores
				if (sc.hasNextLine())
					scores[i] = sc.nextLine();
			
			sc.close();

			for (int i = 0; i < scores.length; i++)			//checks if the current score is a highscore
			{		
				if (scores[i].equals(""))
				{
					scores[i] = player.getName() + " " + score;
					break;
				}
								
				else if (score > Integer.parseInt(scores[i].substring(scores[i].lastIndexOf(" ") + 1)) || score == Integer.parseInt(scores[i].substring(scores[i].lastIndexOf(" ") + 1)))
				{
					if (i == scores.length - 1)
						scores[i] = player.getName() + " " + score;		//if last spot, replaces the score and forgets the old one
					else
					{
						for (int j = scores.length - 1; j >= i + 1; j--)
							scores[j] = scores[j - 1];					//shifts old scores down
						
						scores[i] = player.getName() + " " + score;		//puts current score in the newly empty spot
						break;
					}
				}
			}
		
			int outputLength = 0;			//saves how many scores were output for display below, prevents blank lines
			PrintWriter out = new PrintWriter("highscores.txt", "UTF-8");		//prints the highscores to a text file
			for (String s : scores)
				if (!s.equals(""))
				{ 
					out.println(s);
					outputLength++;
				}
					
			out.close();
			didSaveScores = true;
	
			String nl = System.getProperty("line.separator"); 			//need to use a line separator because Strings don't work with \n
			String highScores = "High Scores" + nl + "1) " + scores[0];
			for (int i = 1; i < 10; i++)
				if (outputLength > i)
					highScores += nl + (i + 1) + ") " + scores[i];
			
			JOptionPane.showMessageDialog(frame, highScores);
			
			int retry = JOptionPane.showConfirmDialog (this, "Play Again?");
			if (retry == JOptionPane.YES_OPTION)		//yes plays again
			{
				UI.reset();
				music.stop();
				removeKeyListener(this);
			}
			else if (retry == JOptionPane.NO_OPTION || retry == JOptionPane.CANCEL_OPTION)		//no or cancel ends the game
				System.exit(0);
		}
   }
   
   /**
    * Draws all the things on the screen
    */
   private void drawStuff()
   {
	   for (int i = 0; i < topWalls.size(); i++) 			//draw top walls
			topWalls.get(i).draw(graphToBack);
		
		for (int i = 0; i < bottomWalls.size(); i++)	 	//draw bottom walls
			bottomWalls.get(i).draw(graphToBack);
		
		for (int i = 0; i < grass.size(); i++) 				//draw grass
			grass.get(i).draw(graphToBack);
		
		for (int i = 0; i < bullets.size(); i++)			//draw bullets
			bullets.get(i).moveAndDraw(graphToBack);
		
		for (int i = 0; i < enemyBullets.size(); i++)		//draw enemy bullets
			enemyBullets.get(i).moveAndDraw(graphToBack);
		
		if (player != null)
			player.draw(graphToBack);						//draw player and gun
		
		for (int i = 0; i < extraGrass.size(); i++)			//draws extra grass
			extraGrass.get(i).draw(graphToBack);
		
		for (int i = 0; i < spikes.size(); i++)				//draws spikes
			spikes.get(i).draw(graphToBack);
		
		for (int i = 0; i < enemies.size(); i++)			//draw enemies
			enemies.get(i).draw(graphToBack);	
		
		for (int i = 0; i < tempEnemies.size(); i++)		//draw temp enemies
			tempEnemies.get(i).draw(graphToBack);
		
		for (int i = 0; i < explosions.size(); i++)			//draw explosions
			explosions.get(i).draw(graphToBack);
		
		for (int i = 0; i < powerUps.size(); i++)			//draws health boosts
			powerUps.get(i).draw(graphToBack);
		
		for (int i = swordPlatform.size() - 1; i >= 0; i--)	//draws sword platform
			swordPlatform.get(i).draw(graphToBack);
		
		drawScores(g, graphToBack);							//draws the scores
			
		if (gunDrop != null)								//draws the ray gun drop
			gunDrop.draw(graphToBack);
   }
   
   /**
    *  Sends player up if they hit the boost
    */
   private void checkHitBoost()
   {
	 if (swordPlatform.get(0).getPath().equals("Pictures/boostBlock.png") && 
		(player.pieceIsThere(swordPlatform.get(0)) || player.didCollideTop(swordPlatform.get(0)) || 
		 player.didCollideLeft(swordPlatform.get(0)) || player.didCollideRight(swordPlatform.get(0))))	
		{
			player.setPos(swordPlatform.get(1).getX() - 10, swordPlatform.get(1).getY() - player.getHeight());
			player.setImage("Pictures/player0.png");
		}
   }
   
   /**
    *  Clears the platform if the player grabbed the sword
    */
   private void checkGrabbedSword()
   {
	   if ((player.didCollideLeft(swordPlatform.get(swordPlatform.size() - 1)) || 
		    player.didCollideRight(swordPlatform.get(swordPlatform.size() - 1)) || 
			player.didCollideTop(swordPlatform.get(swordPlatform.size() - 1)) || 
			player.pieceIsThere(swordPlatform.get(swordPlatform.size() - 1))))
			{
				player.hasSword = true;
				swordTries = 0;
				while (swordPlatform.size() > 0)		//clears the platform
					swordPlatform.remove(0);
			}
   }
   
   /**
    * When a key is pressed the value becomes true
    * @param e some KeyEvent
    */
  public void keyPressed(KeyEvent e)
  {
	  switch(toUpperCase(e.getKeyChar()))		//wasd
	  {
	  	 case 'A' : keys[0] = true; break;	//left
	   	 case 'D' : keys[1] = true; break;	//right
	   	 case 'W' : keys[2] = true; break;	//jump
	   	 case ' ' : keys[3] = true; break;	//fire
	   	 case 'F' : keys[4] = true; break;	//melee
	   	 case 'S' : keys[5] = true; break;	//crouch
	   	 case '1' : keys[6] = true; break;	//normal gun
	   	 case '2' : keys[7] = true; break;	//ray gun
	  }
	  
	  switch(e.getKeyCode())			//arrow keys
	  {
	  	case KeyEvent.VK_LEFT : keys[0] = true; break;		//left
	  	case KeyEvent.VK_RIGHT : keys[1] = true; break;		//right
	  	case KeyEvent.VK_UP : keys[2] = true; break;		//up
	  	case KeyEvent.VK_DOWN : keys[5] = true; break;		//crouch
	  }
  }

  /**
   * When a key is released the value becomes false
   * @param e some KeyEvent
   */
  public void keyReleased(KeyEvent e)
  {
	switch(toUpperCase(e.getKeyChar()))		//wasd
	{
		case 'A' : keys[0] = false; break;
		case 'D' : keys[1] = false; break;
		case 'W' : keys[2] = false; break;
	   	case ' ' : keys[3] = false; break;
	   	case 'F' : keys[4] = false; break;
	   	case 'S' : keys[5] = false; break;
		case '1' : keys[6] = false; break;	
	   	case '2' : keys[7] = false; break;	
	}
	
	switch(e.getKeyCode())			//arrow keys
	  {
	  	case KeyEvent.VK_LEFT : keys[0] = false; break;
	  	case KeyEvent.VK_RIGHT : keys[1] = false; break;
	  	case KeyEvent.VK_UP : keys[2] = false; break;
	  	case KeyEvent.VK_DOWN : keys[5] = false; break;
	  }
  }

  	/**
	 * When a mouse button is clicked
	 * mouseButton gets set to the button pressed
	 * Checks if a mouse action can be performed
	 */
	public void mousePressed(MouseEvent e) 
	{
		int mouseButton = e.getModifiers();
		mouseActions(mouseButton);
	}
	
	/**
	 * Performs the various mouse actions
	 * LMB --> fire
	 * RMB --> knife
	 * MMB --> switch guns
	 * @param mouseButton the mouse button pressed
	 */
	public void mouseActions(int mouseButton)
	{		
		if (mouseButton == MouseEvent.BUTTON1 || mouseButton == MouseEvent.BUTTON1_MASK)		//fire bullet
			if (player.getAmmo() > 0 && player.fireDelay < 1)
				if (player.getWeapon().getWeaponName().equalsIgnoreCase("gun") || player.getWeapon().getWeaponName().equalsIgnoreCase("raygun"))			//can only fire a bullet if a gun is equipped
				{
					player.removeAmmo(1);
					player.fireDelay = 10;
					if (player.getWeapon().getWeaponName().equals("gun"))
						bullets.add(new Bullet(player.getWeapon().getX() + player.getWeapon().getWidth() - 20, player.getWeapon().getY(), 20, 5, 32, "Pictures/bullet.png"));			//x, y, width, height, speed, path
					else
						bullets.add(new Bullet(player.getWeapon().getX() + player.getWeapon().getWidth() - 20, player.getWeapon().getY(), 20, 5, 55, "Pictures/laser.png"));			//x, y, width, height, speed, path
				}
		
		if (mouseButton == MouseEvent.BUTTON3 || mouseButton == MouseEvent.BUTTON3_MASK)		//knife 
		{
			if (!player.hasSword)
				player.setWeapon("knife");
			else
				player.setWeapon("sword");
		}
		
		if (mouseButton == MouseEvent.BUTTON2 || mouseButton == MouseEvent.BUTTON2_MASK)		//change guns
			switch (player.getLastGun().getWeaponName())
			{
				case "raygun": player.setWeapon("gun"); break;						//switches to normal gun if raygun was last	
				default: if (player.hasRayGun) player.setWeapon("raygun"); break;	//otherwise gun was last and switches to raygun if its available
			}
	}
	
	/**
	 * Unused methods that needed to be added
	 */
	public void keyTyped(KeyEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
}