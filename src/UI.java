import java.awt.CardLayout;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Copyright (c) 2015 Ben Alderfer
 * See the file license.txt for copying permission.
 *
 * Ben Alderfer
 * The UI class
 */
public class UI
{
	private static boolean firstRun = true;
	private static final int WIDTH = ((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth());  
	private static final int HEIGHT = ((int) Toolkit.getDefaultToolkit().getScreenSize().getHeight());  
	private static JFrame frame;
	private static JPanel cards;
	private static CardLayout cardLayout;
	private static String currentCard;
	private static TitleScreen title;
	public static ScrollingShooter game;
	
	public static void main(String args[])
		{new UI();}
	
	public UI()
	{
		frame = new JFrame("Scrolling Shooter");
		frame.setResizable(false);
	    frame.setSize(WIDTH, HEIGHT);
	     
	    reset();
	    
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setLocationRelativeTo(null);
	    frame.setVisible(true);
	}
	
	/**
	 * Switches to the next screen
	 * Keeps track of the currentCard to assign focus
	 */
	public static void nextScreen()
	{
		cardLayout.next(cards);
		switch(currentCard)
		{
			case "title" : currentCard = "game"; game.requestFocusInWindow(); break;
			case "game" : currentCard = "title"; title.requestFocusInWindow(); break;
		}
	}
	
	/**
	 * Returns to the first screen
	 */
	public static void firstScreen()
	{
		cardLayout.first(cards);
		title.requestFocusInWindow();
	}
	
	/**
	 * Resets the game for multiple plays
	 * Also used on first setup
	 */
	public static void reset()
	{	
		//removes previous game if its not the first time through
		if (!firstRun)
		{
			frame.remove(title);
			frame.remove(game);
			cards.removeAll();
			cardLayout.removeLayoutComponent(cards);
		}
		
		title = new TitleScreen(frame);
		game = new ScrollingShooter(frame);

		cards = new JPanel(new CardLayout());
		cards.add(title, "title");
		cards.add(game, "game");
		cards.setOpaque(true);
		frame.getContentPane().add(cards);
	        
		cardLayout = (CardLayout) cards.getLayout();
		cardLayout.first(cards);
		currentCard = "title";
		title.requestFocusInWindow();
		
		firstRun = false;
	}
}
