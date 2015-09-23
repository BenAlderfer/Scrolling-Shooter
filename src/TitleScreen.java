import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Copyright (c) 2015 Ben Alderfer
 * See the file license.txt for copying permission.
 *
 * Ben Alderfer
 * The Title Screen
 */
@SuppressWarnings("serial")
public class TitleScreen extends JPanel implements KeyListener
{
	private static final String instructions = "start typing";
	
	private JFrame frame;					//the JFrame
	private ImageItem background;			//the background
	private String name = instructions;		//the player's name
	private Clip music;						//the music clip, used to play sound
	private int loopCount = 1;				//how many times the music will loop, increases as the game runs so it doesn't stop
	
	/**
	 * Constructs a title screen
	 * @param f the JFrame
	 */
	public TitleScreen (JFrame f)
	{
		frame = f;
		background = new ImageItem(0, 0, frame.getWidth(), frame.getHeight(), "Pictures/splashscreenshrunk2.png"); 	//makes background stretch between the top and bottom walls
		
		try
		{
			File soundFile = new File("Music/Our Story Begins.wav");
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
			music = AudioSystem.getClip();
	        music.open(audioIn);
		}
		catch (UnsupportedAudioFileException e) {e.printStackTrace();}
		catch (IOException e) {e.printStackTrace();} 
		catch (LineUnavailableException e) {e.printStackTrace();}
		
		setVisible(true);
		setFocusable(true);
		addKeyListener(this);	
		
		int delay = 0; //milliseconds
		ActionListener taskPerformer = new ActionListener(){public void actionPerformed(ActionEvent evt) {repaint();} };
		new Timer(delay, taskPerformer).start();		//repaints the screen
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
		
		background.draw(g);
		if (!(name.equals("")))
		{
			g.setFont(g.getFont().deriveFont(80f));
			g.drawString(name, (int) (frame.getWidth() / 3.2), frame.getHeight() - (int) (frame.getHeight() / 13));		//draws name
		}
	}
	
	/**
	 * When a key is pressed it is added to the name 
	 * Backspace deletes the last key
	 * Enter makes moves to the game if the name is ok
	 * @param key the key pressed
	 */
	public void keyPressed(KeyEvent key)
	{
		if (name.equals(instructions))
			name = "";
		
		int x = key.getKeyChar();
		if (x >= 32 && x <= 126)		//checks for everything from space to ~
			if (name.length() < 20)
				name += key.getKeyChar();
		
		if (name.length() > 0)				//deletes last value if there is something there
			if (key.getKeyCode() == KeyEvent.VK_BACK_SPACE)
				name = name.substring(0, name.length() - 1);
		
		if (key.getKeyCode() == KeyEvent.VK_ENTER)
		{
			boolean isValid = false;				
			if (!name.equals(""))
			{
				try {if (!hasBannedWord(name)) isValid = true;} 		//breaks the loop if there are no banned words in the name
				catch (FileNotFoundException e) {e.printStackTrace();}
			}
			
			if (!isValid)
			{
				JOptionPane.showMessageDialog(frame, "Bad name. Try again.");
				name = "";
			}
			
			if (isValid)			//moves to the game, sets the name, and stops things running in TitleScreen
			{
				UI.nextScreen();
				UI.game.player.setName(name);
				music.stop();
				removeKeyListener(this);
			}
		}	
	}
	
	/**
	  * Checks the name for a banned word
	  * @param name the name input
	  * @return true if there is a banned word in the name
	  * @throws FileNotFoundException if the blacklist cannot be found
	  */
	 private boolean hasBannedWord(String name) throws FileNotFoundException
	 {
		   File file = new File("blacklist.txt");
		   Scanner sc = new Scanner(file, "UTF-8");
		   ArrayList<String> words = new ArrayList<String>();
		   while(sc.hasNextLine())
			   words.add(sc.nextLine());
			
			sc.close();
			
			boolean match = false;		//if the name matches one on the blacklist, will not allow it
			for (String s : words)		//checks name as is
				if (name.toLowerCase().contains(s.toLowerCase()))
				{
					match = true;
					break;
				}
			
			name = removeDoubles(name);		//removes double letters then checks
			for (String s : words)
				if (name.toLowerCase().contains(s.toLowerCase()))
				{
					match = true;
					break;
				}
			
			String tempName = changeToLetters(name);	//changes symbols to letters for checking
			for (String s : words)
				if (tempName.toLowerCase().contains(s.toLowerCase()))
				{
					match = true;
					break;
				}
			
			if (!match)					//tries checking with symbols removed
			{
				tempName = removeExtras(name);
				for (String s : words)
					if (tempName.toLowerCase().contains(s.toLowerCase()))
					{
						match = true;
						break;
					}
			}
			
			if (!match)					//tries checking by converting symbols to letters and removing the rest
			{
				tempName = changeToLetters(name);
				tempName = removeExtras(tempName);
				for (String s : words)
					if (tempName.toLowerCase().contains(s.toLowerCase()))
					{
						match = true;
						break;
					}
			}

			if (!match)					//tries switching z to s, tempName already has symbols converted/removed from before
			{
				tempName = switchToS(tempName);
				for (String s : words)
					if (tempName.toLowerCase().contains(s.toLowerCase()))
					{
						match = true;
						break;
					}
			}
			
			if (!match)					//tries switching l to i if there is no match yet
			{
				tempName = changeToLetters(name);
				tempName = removeExtras(tempName);
				tempName = switchToI(tempName);
				for (String s : words)
					if (tempName.toLowerCase().contains(s.toLowerCase()))
					{
						match = true;
						break;
					}
			}

			if (!match)					//tries switching l to i and z to s, tempName already keeps actions from above
			{
				tempName = switchToS(tempName);
				for (String s : words)
					if (tempName.toLowerCase().contains(s.toLowerCase()))
					{
						match = true;
						break;
					}
			}
			
			return match;
	   }
	   
	   /**
	    * Removes the extra characters *, /, \, (space), -, `, ~, &, ", :, ', <, >
	    * @param s the String to remove the extras from
	    * @return s the String without any extras
	    */
	   private String removeExtras(String s)
	   {
		   String[] remove = new String[13];		//all the things to remove
		   remove[0] = "*";
		   remove[1] = "/";
		   remove[2] = "\\";
		   remove[3] = " ";
		   remove[4] = "-";
		   remove[5] = "`";
		   remove[6] = "~";
		   remove[7] = "&";
		   remove[8] = "\"";
		   remove[9] = ":";
		   remove[10] = "'";
		   remove[11] = "<";
		   remove[12] = ">";
		   
		   for (int i = 0; i < remove.length; i++)				//removes all unwanted symbols
			   if (s.contains(remove[i]))
			   {
				   String start = s.substring(0, s.indexOf(remove[i]));
				   String end = s.substring(s.indexOf(remove[i]) + remove[i].length());
				   s = start + end;
			   }
		   
		   return s;
	   }
	   
	   /**
	    * Changes all numbers/symbols to letters for bad word checking
	    * 0 = o
	    * |\| = n
	    * |<, !< = k
	    * 1, ! = i
	    * 3 = e
	    * 5, $ = s
	    * 8 = b
	    * \/\/ = w
	    * \/ = v
	    * >< = x
	    * + = t
	    * |_| = u
	    * |-| = h
	    * @ = a
	    * |) = d
	    * | = l
	    * @param s the name to modify
	    * @return s the modified name
	    */
	   private String changeToLetters(String s)
	   {
		   String[][] replacements = new String[19][2];			//1st column is what to look for, 2nd is the replacement, 3rd is extra length from escape sequences
		   replacements[0][0] = "0";
		   replacements[1][0] = "|\\|";
		   replacements[2][0] = "|<";
		   replacements[3][0] = "!<";
		   replacements[4][0] = "1";
		   replacements[5][0] = "!";
		   replacements[6][0] = "3";
		   replacements[7][0] = "5";
		   replacements[8][0] = "$";
		   replacements[9][0] = "8";
		   replacements[10][0] = "\\/\\/";
		   replacements[11][0] = "\\/";
		   replacements[12][0] = "><";
		   replacements[13][0] = "+";
		   replacements[14][0] = "|_|";
		   replacements[15][0] = "|-|";
		   replacements[16][0] = "@";
		   replacements[17][0] = "|)";
		   replacements[18][0] = "|";
		   
		   replacements[0][1] = "o";
		   replacements[1][1] = "n";
		   replacements[2][1] = "k";
		   replacements[3][1] = "k";
		   replacements[4][1] = "i";
		   replacements[5][1] = "i";
		   replacements[6][1] = "e";
		   replacements[7][1] = "s";
		   replacements[8][1] = "s";
		   replacements[9][1] = "b";
		   replacements[10][1] = "w";
		   replacements[11][1] = "v";
		   replacements[12][1] = "x";
		   replacements[13][1] = "t";
		   replacements[14][1] = "u";
		   replacements[15][1] = "h";
		   replacements[16][1] = "a";
		   replacements[17][1] = "d";
		   replacements[18][1] = "l";	   
		   	   
		   for (int i = 0; i < replacements.length; i++)
			   if (s.contains(replacements[i][0]))
			   {
				   String start = s.substring(0, s.indexOf(replacements[i][0]));
				   String end = "";
				   if (replacements[i][0].contains("|\\|"))		//this one doesnt work right for some reason
					   end = s.substring(s.indexOf(replacements[i][0]) + replacements[i][0].length() - 1);
				   else
					   end = s.substring(s.indexOf(replacements[i][0]) + replacements[i][0].length());
				   s = start + replacements[i][1] + end;  
			   }
		   
		   return s;   
	   }
	   
	/**
	 * Switches lowercase l to i
	 * @param s the name to modify
	 * @return s the modified name
	 */
	private String switchToI(String s)
	{
		for (int i = s.length() - 1; i >= 0; i--)
			if (s.substring(i, i + 1).equals("l"))
			{
				String start = s.substring(0, i);
				String end = s.substring(i + 1);
				s = start + "i" + end;  
			}
		   
		return s;  
	}

	/**
	 * Switches lowercase z to s
	 * @param s the name to modify
	 * @return s the modified name
	 */
	private String switchToS(String s)
	{
		for (int i = s.length() - 1; i >= 0; i--)
			if (s.substring(i, i + 1).equals("z"))
			{
			   String start = s.substring(0, i);
			   String end = s.substring(i + 1);
			   s = start + "s" + end;  
			}
		   
		return s;  
	}
	   
	/**
	 * Removes all the doubles from s
	 * @param s the word to modify
	 * @return s the word with no double letters
	 */
	private String removeDoubles(String s)
	{
		for (int i = s.length() - 1; i > 0; i--)
			if (s.substring(i, i + 1).equalsIgnoreCase(s.substring(i - 1, i)))
			{
				String start = s.substring(0, i - 1);
				String end = s.substring(i);
				s = start + end;
			}
		   
		return s;
	}
	
    /**
	 * Unneeded because key typed doesn't allow enter or backspace
	 * @param key the key typed
	 */
	public void keyTyped(KeyEvent key) {}
	   
	/**
	 * Unneeded because key release is irrelevant
	 * @param key the key released
	 */
	public void keyReleased(KeyEvent key) {}
}
