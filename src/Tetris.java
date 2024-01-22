
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
//import java.io.PrintStream;
import java.util.ArrayList;
//import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Tetris extends JPanel {

	private final Point[][][] Tetraminos = {
			// I-Piece
			{
				{ new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(3, 1) },
				{ new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(1, 3) },
				{ new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(3, 1) },
				{ new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(1, 3) }
			},
			
			// L-Piece
			{
				{ new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(2, 0) },
				{ new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(2, 2) },
				{ new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(0, 2) },
				{ new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(0, 0) }
			},
			
			// J-Piece (changed rotation order so it would start facing up rather than down)
			{
				{ new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(0, 0) }, //3
				{ new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(2, 0) }, //4
				{ new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(2, 2) }, //1
				{ new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(0, 2) }  //2
			},
			
			// O-Piece
			{
				{ new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1) },
				{ new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1) },
				{ new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1) },
				{ new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1) }
			},
			
			// S-Piece
			{
				{ new Point(1, 0), new Point(2, 0), new Point(0, 1), new Point(1, 1) },
				{ new Point(0, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2) },
				{ new Point(1, 0), new Point(2, 0), new Point(0, 1), new Point(1, 1) },
				{ new Point(0, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2) }
			},
			
			// T-Piece
			{
				{ new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(2, 1) },
				{ new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2) },
				{ new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(1, 2) },
				{ new Point(1, 0), new Point(1, 1), new Point(2, 1), new Point(1, 2) }
			},
			
			// Z-Piece
			{
				{ new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(2, 1) },
				{ new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(0, 2) },
				{ new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(2, 1) },
				{ new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(0, 2) }
			}
	};
	
	//Note: Switched colors of J and L to reflect tetris standard
	private final Color[] tetraminoColors = {
		Color.cyan, Color.orange, Color.blue, Color.yellow, Color.green, Color.pink, Color.red
	};
	
	private Point pieceOrigin;
	private int currentPiece;
	private boolean swapped = false;
	private boolean gameOver = false;
	private boolean keepPlaying = true;
	private boolean added = false;
	private static int storedPiece = -1;
	private static int tempPiece = -1;
	private int rotation;
	private ArrayList<Integer> nextPieces = new ArrayList<Integer>();

	private int score;
	private int phase = 1;
	private int timeDec = 200;
	private Color[][] well;
    private static int gamewidth = 12, gameheight = 24;
	private static JFrame f;
	
	private static JFrame endGame;
	private static JLabel endGameText;
	private static JButton newGame;
	private static JButton exit;
	
	private static int lowestScore;
	
	private static int sleepTime = 1000;
	
	private static String endGameString;
	
	// Creates a border around the well and initializes the dropping piece
	private void init() {
		
		well = new Color[12][25];
		for (int i = 0; i < gamewidth; i++) {
			for (int j = 0; j < gameheight; j++) {
				if (i == 0 || i == gamewidth - 1 || j == gameheight - 1 || j == 0) {
					well[i][j] = Color.GRAY;
				} else {
					well[i][j] = Color.BLACK;
				}
			}
		}
		newPiece();
	}
	
	
	//resets the game world
	private void restart() {
		//reset score and level variables
		added = false;
		endGame.setVisible(false);
		gameOver = false;
		phase = 1;
		timeDec = 200;
		sleepTime = 1000;
		storedPiece = -1;
		score = 0;
		
		//clear the game board
		for (int j = gameheight - 2; j > 0; j--) {
			for (int i = 1; i < gamewidth - 1; i++) {
				well[i][j] = Color.BLACK;
			}
		}
		
		
		newPiece();
		
		repaint();
		
	}
	
	
	// Put a new, random piece into the dropping position
	public void newPiece() {
		pieceOrigin = new Point(5, 1);
		
		rotation = 0;
		
		//Game Over Code
		if(collidesAt(5, 1, 0)) {
			gameOver = true;
			
		} else {
			if (nextPieces.isEmpty()) {
				Collections.addAll(nextPieces, 0, 1, 2, 3, 4, 5, 6);
				Collections.shuffle(nextPieces);
			}
			currentPiece = nextPieces.get(0);
			nextPieces.remove(0);
		}
		
	
	}
	
	//Put a predetermined piece into the starting position
	public void newPiece(int piece) {
		pieceOrigin = new Point(5, 1);
		
		rotation = 0;
		
		//Game Over Code
		if (collidesAt(5, 1, 0)) {
			gameOver = true;
			
		}
		

		currentPiece = piece;
	}
	
	// Collision test for the dropping piece
	private boolean collidesAt(int x, int y, int rotation) {
		for (Point p : Tetraminos[currentPiece][rotation]) {
			if (well[p.x + x][p.y + y] != Color.BLACK) {
				return true;
			}
		}
		return false;
	}
	
	// Rotate the piece clockwise or counterclockwise
	public void rotate(int i) {
		int newRotation = (rotation + i) % 4;
		if (newRotation < 0) {
			newRotation = 3;
		}
		if (!collidesAt(pieceOrigin.x, pieceOrigin.y, newRotation)) {
			rotation = newRotation;
		}
		repaint();
	}
	
	// Move the piece left or right
	public void move(int i) {
		if (!collidesAt(pieceOrigin.x + i, pieceOrigin.y, rotation)) {
			pieceOrigin.x += i;	
		}
		repaint();
	}
	
	// Drops the piece one line or fixes it to the well if it can't drop
	public void dropDown() {
		if (!collidesAt(pieceOrigin.x, pieceOrigin.y + 1, rotation)) {
			pieceOrigin.y += 1;
		} else {
			fixToWell();
		}	
		
		repaint();
	}
	
	/*
	 * Drops the piece down until it collides. Only used if space is pressed.
	 * Player is awarded two bonus points for every row the piece drops
	 */
	public void dropToBottom() {
		//while it can drop down, drops down.
		while (!collidesAt(pieceOrigin.x, pieceOrigin.y + 1, rotation)) {
			score += 2;
			pieceOrigin.y += 1;
		}
		
		fixToWell();
		
		repaint();
	}
	
	// Make the dropping piece part of the well, so it is available for
	// collision detection.
	public void fixToWell() {
		for (Point p : Tetraminos[currentPiece][rotation]) {
			well[pieceOrigin.x + p.x][pieceOrigin.y + p.y] = tetraminoColors[currentPiece];
		}
		swapped = false;
		clearRows();
		newPiece();
	}
	
	public void deleteRow(int row) {
		for (int j = row-1; j > 0; j--) {
			for (int i = 1; i < gamewidth - 1; i++) {
				well[i][j+1] = well[i][j];
			}
		}
	}
	
	// Clear completed rows from the field and award score according to
	// the number of simultaneously cleared rows.
	public void clearRows() {
		boolean gap;
		int numClears = 0;
		
		for (int j = gameheight - 2; j > 0; j--) {
			gap = false;
			for (int i = 1; i < gamewidth - 1; i++) {
				if (well[i][j] == Color.BLACK) {
					gap = true;
					break;
				}
			}
			if (!gap) {
				deleteRow(j);
				j += 1;
				numClears += 1;
			}
		}
				
		switch (numClears) {
		case 1:
			score += 100;
			break;
		case 2:
			score += 300;
			break;
		case 3:
			score += 500;
			break;
		case 4:
			score += 800;
			break;
		}
		
		//CODE FOR DIFFICULTY UPDATING
		//decrements length of time the game sleeps between dropdowns every 1000 points earned	
		if (score >= 1000 * phase) {
			//prevents timeDec from going to zero and breaking game - if the current tick time is
			//less than or equal to twice the timeDec value, timeDec is divided by 2
			//Should scale infinitely
			if (sleepTime <= (2 * timeDec)) {
				timeDec /= 2;
			}
			
			sleepTime -= timeDec;
			phase++;
		}
		
	}

    //checks for the theoretical y position of the gray Tetramino
	public int checkTheoreticalPos() {
        ArrayList<Integer> tPos = new ArrayList<>();
        for (Point p : Tetraminos[currentPiece][rotation]) {
            int tValue = 0;
            for (int j = p.y + pieceOrigin.y + 1; j < gameheight - 1; j++) {
                if (well[p.x + pieceOrigin.x][j] == Color.BLACK) {
                    tValue++;
                } else {break;}
            }
            tPos.add(tValue);
        }
        return Collections.min(tPos) + pieceOrigin.y;
    }
	
	private void drawPiece(Graphics g) {		
		
        // paints the theoretical gray Tetramino
        g.setColor(Color.GRAY);

		for (Point p : Tetraminos[currentPiece][rotation]) {
            g.fillRect((p.x + pieceOrigin.x) * 26, (p.y + checkTheoreticalPos()) * 26 + 60, 25, 25);
		}
        
        // Draw the falling piece
        g.setColor(tetraminoColors[currentPiece]);
		
		for (Point p : Tetraminos[currentPiece][rotation]) {
			g.fillRect((p.x + pieceOrigin.x) * 26, (p.y + pieceOrigin.y) * 26 + 60, 25, 25);
		}
	}
	
	// Returns the standard letter representation for a tetramino
	public String getPieceName(int num) {
		String returnString = null;
		
		if (num == -1) {
			returnString = "None";
		} 
        else if (storedPiece == 0) {returnString = "I";} 
        else if (storedPiece == 1) {returnString = "L";} 
        else if (storedPiece == 2) {returnString = "J";} 
        else if (storedPiece == 3) {returnString = "O";} 
        else if (storedPiece == 4) {returnString = "S";}
        else if (storedPiece == 5) {returnString = "T";}  
        else if (storedPiece == 6) {returnString = "Z";}
		
		return returnString;
		
	}
	
	@Override 
	public void paintComponent(Graphics g) {
		String storedPieceName = null;
		
		// Paint the well
		//Note: Edited to leave blank space at top for scoring data
		g.fillRect(0, 0, 26 * gamewidth, 26 * gameheight + 60);

		for (int i = 0; i < gamewidth; i++) {
			for (int j = 0; j < gameheight; j++) {
				g.setColor(well[i][j]);
				g.fillRect(26 * i, 26 * j + 60, 25, 25);
			}
		}
		
		// Display the score
		g.setColor(Color.WHITE);
		g.drawString("Score: " + score, 20, 35);
		
		//Display the current phase/level
		g.setColor(Color.WHITE);
		g.drawString("Level:  " + phase, 120, 35);
		
		//Display the currently stored piece
		g.setColor(Color.WHITE);
		storedPieceName = getPieceName(storedPiece);
		g.drawString("Stored: " + storedPieceName, 220, 35);
		
		//Draw the currently falling piece
		drawPiece(g);
	}

	
	public static void main(String[] args) throws IOException {

        String[] storeScores = new String[10];
		
	//	lowestScore = Integer.parseInt(storeScores[9]);
		
		
		//JFrame Initialization
		f = new JFrame("Tetris");
		
		endGame = new JFrame("Game Over");
		
		endGame.setLayout(null);
		
		endGame.setSize(gamewidth*26+15, 300);

		endGame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		newGame = new JButton();
		newGame.setText("Play Again");
		
		exit = new JButton();
		exit.setText("Exit");
		
		endGameText = new JLabel();
		endGameText.setSize(500, 100);	//200 25
		endGameText.setLocation(90, 160); //90 160
	
		newGame.setSize(100, 100);
		newGame.setLocation(50, 50);
		newGame.setBackground(Color.GREEN);
		
		exit.setSize(100, 100);
		exit.setLocation((100 + 62), 50);
		exit.setBackground(Color.RED);
		
		
		endGame.add(newGame);
		endGame.add(exit);
		endGame.add(endGameText);
		endGame.getContentPane().setBackground(Color.BLACK);
		
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		f.setSize(gamewidth * 26 + 15, gameheight * 26 + 38 + 60);
			
		f.setVisible(true);
		
		final Tetris game = new Tetris();
		game.init();
		f.add(game);
		
		
		// Keyboard controls
		f.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
				
			}
			
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
					case KeyEvent.VK_W:
						game.rotate(+1);
						break;
					
					//Note: Drop down has been switched to the down arrow key.
					case KeyEvent.VK_S:
						game.dropDown();
						game.score += 1;
						break;

					case KeyEvent.VK_A:
						game.move(-1);
						break;

					case KeyEvent.VK_D:
						game.move(+1);
						break;
					
					/*
					* Space bar is used for a Hard Drop - Piece instantly moves to the bottom of
					* the screen. This awards 2 points for every row dropped.
					*/
					case KeyEvent.VK_SPACE:
						game.dropToBottom();
						break;
					
					/*
					* Stored Piece Code 
					* On pressing c, the currently dropping piece should be
					* stored and be replaced by the piece previously in storage. The game only
					* permits "swapping" once per drop, unless the player is storing a piece for
					* the first time.
					*/
					case KeyEvent.VK_C:
						/*
						 * If no piece has been stored yet, store the current piece and drop a new
						 * randomly selected piece
						 */
						if (storedPiece == -1) {
							storedPiece = game.currentPiece;
							game.repaint();
							game.newPiece();
						} else {
						/*
						 * If player has not already swapped this drop, swaps current 
						 * and stored pieces and sets game.swapped to true
						 */
							if (game.swapped == false) {
								tempPiece = game.currentPiece;
								game.newPiece(storedPiece);
								storedPiece = tempPiece;
								game.swapped = true;
								
								game.repaint();
							}
						
							//If player has already swapped this drop, do nothing
						}
					
					    break;
				} 
			}
			
			public void keyReleased(KeyEvent e) {
				
			}
		});
		
		
		//Modified dropdown iterator code, removed threads
		while (game.keepPlaying) {
			while (game.gameOver == false) {
				try {
					TimeUnit.MILLISECONDS.sleep(sleepTime);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				game.dropDown();
			}
			
			//GAME OVER CODE
			//check to see if the player scored higher than the lowest scoring entry
			if (game.score > lowestScore) {
				int currentScore = 0;
				int i = 9;
				
				while (game.added == false) {
					
				//	currentScore = Integer.parseInt(storeScores[i]);
					
					//System.out.println("current Score: "+ currentScore);
					
					//if player's score is the new highest score
					if (i == 1) {
						game.added = true;
						storeScores[i] = Integer.toString(game.score);
						storeScores[i - 1] = "Player";
						
					
					//if player's score belongs in the middle of the scores
					}
					
					if (game.score <= currentScore) {
						//System.out.println("Found where it belongs");
						game.added = true;
						storeScores[i + 2] = Integer.toString(game.score);
						storeScores[i + 1] = "Player";
						
						//System.out.println("Added: " + Arrays.toString(storeScores));
						
						
					//otherwise, shift the array down one
					}
					
					if (game.score > currentScore && game.added == false) {
						//System.out.println("Too small, shift down: ");
						storeScores[i] = storeScores[i - 2];
						storeScores[i - 1] = storeScores[i - 3];
						
						//System.out.println("Updated: " + Arrays.toString(storeScores));
						
					}
					
					
					i = i - 2;
					
				}
				
				
			//	lowestScore = Integer.parseInt(storeScores[9]);

			}
			
			endGameString = "<html><pre>";
			
			for (int i = 0; i < 10; i = i + 2) {
				endGameString = endGameString + storeScores[i];
				
				endGameString = endGameString + "&#9;";
						
				endGameString = endGameString + storeScores[i + 1] + "<BR>";
			}
			
			
			endGameString = endGameString + "</pre></html>";
			
			endGameText.setText(endGameString);
			
			endGame.setVisible(true);
			
			newGame.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					
					game.restart();
					
	            }
				
			});
			
			
			exit.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					game.keepPlaying = false;
	            }
				
			});
			
			
		}
		
		//Exit game code
		endGame.dispose();
		f.dispose();
		
	}
	
}
