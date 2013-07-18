/*
 * known bugs: cant handle leavers
 *
 */

import java.util.Arrays;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;


class Draft {
	private int playersNum;
	private int roundsNum;
	private Player[] players;
	private Game[] games;
	private Player bye;
	private final static String INPUT_FILE_NAME = "in.txt";
	
	public static void main(String[] args) throws Exception {
		Draft draft = new Draft();

		boolean fullStats = false;
		if (args.length > 0 && args[args.length - 1].equals("-d")) {
			fullStats = true;
		}
		String in;
		if (args.length > 0 && !args[0].equals("-d")) {
			in = args[0];
		} else { 
			in = INPUT_FILE_NAME;
		}
		
		draft.init();
		draft.readInput(in);
		draft.process();
		draft.print(fullStats);
	}
	
	public void init() {
		bye = new Player("Bye");		
	}
	
	public void readInput(String in) throws Exception {
		Scanner sc = new Scanner(new File(in), "UTF-8");
		
		playersNum = sc.nextInt();
		players = new Player[playersNum];
		
		roundsNum = sc.nextInt();
		int gamesPerRound = (playersNum + 1) / 2;
		
		games = new Game[roundsNum * gamesPerRound];
		for (int i = 0; i < playersNum && sc.hasNext(); i++) {
			players[i] = new Player(sc.next());
		}
				
		String s1, s2, s3;
		for (int i = 0; i < roundsNum; i++) {
			for (int j = 0; j < gamesPerRound; j++) {
				s1 = sc.next();
				s2 = sc.next();
				if (s2.equals("Bye") || s2.equals("Бай")) {
					games[i * gamesPerRound + j] = new Game(i + 1, getPlayerByName(s1), bye, "2:0");
				} else {
					s3 = sc.next();
					if (!s2.matches("[01]:[012]|[012]:[01]")) {
						throw new Exception("invalid game result: " + s2);
					}
					Player p1 = getPlayerByName(s1);
					Player p2 = getPlayerByName(s3);
					for (int k = j - 1; k >= 0; k--) {
						Game g = games[i * gamesPerRound + k];
						if (g.player1 == p1 || g.player1 == p2 ||
						    g.player2 == p1 || g.player2 == p2) {
							throw new Exception("second game per round: " + s1 + " " + s2 + " " + s3);
						}
					}
					games[i * gamesPerRound + j] = new Game(i + 1, p1, p2, s2);
				}
			}
		}
		sc.close();
		
		/*for (int i = 0; i < games.length; i++) {
			System.out.println(games[i].round + "\t" + games[i].player1.name + "\t" + games[i].player2.name + "\t" + games[i].result);
		}*/
	}
	
	public void process() {
		int win, loss, draw;
		// перевроверить. пока что гарантированно корректно обрабатываются только 2:0 2:1 0:2 1:2 1:1 0:0
		for (int i = 0; i < games.length; i++) {
			//System.out.println("game " + i);
			win = games[i].result.charAt(0) - 48;
			loss = games[i].result.charAt(2) - 48;
			if (games[i].result.length() == 5) { // 1:1:1??
				draw = games[i].result.charAt(4) - 48;
			} else {
				draw = 0;
			}
			if (games[i].player1 != bye && games[i].player2 != bye) {
				games[i].player1.opponentsList.add(games[i].player2);
				games[i].player2.opponentsList.add(games[i].player1);
			}
			if (games[i].player1 != bye) {
				//System.out.println("player1 != bye");
				games[i].player1.matchesPlayed++;
				games[i].player1.gamePoints += win * 3 + draw * 1;
				games[i].player1.gamesPlayed += win + loss + draw;
			}
			if (games[i].player2 != bye) {
				//System.out.println("player2 != bye");
				games[i].player2.matchesPlayed++;
				games[i].player2.gamePoints += loss * 3 + draw * 1;
				games[i].player2.gamesPlayed += win + loss + draw;
			}
			
			if (win > loss) {
				games[i].player1.matchPoints += 3;
			} else if (loss > win) {
				games[i].player2.matchPoints += 3;
			} else {
				games[i].player1.matchPoints += 1;
				games[i].player2.matchPoints += 1;
			}
		}
		for (int i = 0; i < players.length; i++) {
			players[i].calculateStats();
		}
		for (int i = 0; i < players.length; i++) {
			players[i].calculateOppStats();
		}
		Arrays.sort(players);
	}
	
	public void print(boolean fullStats) {
		System.out.println("Draft results:");
		if (fullStats) {
			for (int i = 0; i < players.length; i++) {
				players[i].printFullStats();
			}
		} else {
			for (int i = 0; i < players.length; i++) {
				System.out.println(players[i]);
			}
		}
	}
		
	private Player getPlayerByName(String name) throws Exception {
		for (int i = 0; i < playersNum; i++) {
			if (players[i].name.equals(name)) {
				return players[i];
			}
		}
		if (name.equals("Bye")) {
			return bye;
		}
		throw (new Exception("can't find player: " + name));
	}
	
	class Player implements Comparable<Player> {
		public String name;
		public double mwp = 0; // match win percentage
		public double omwp = 0; // opponents' match win percentage
		public double gwp = 0; // game win percentage
		public double ogwp = 0; // opponents' game win percentage
		public int matchesPlayed = 0;
		public int matchPoints = 0;
		public int gamesPlayed = 0;
		public int gamePoints = 0;
		public ArrayList<Player> opponentsList = new ArrayList<Player>();
		
		public Player(String name) {
			this.name = name;
		}
		
		public void calculateStats() {
			mwp = (double) matchPoints / (3 * matchesPlayed);
			gwp = (double) gamePoints / (3 * gamesPlayed);
		}
		
		public void calculateOppStats() {
			double omwperc = 0;
			double ogwperc = 0;
			for (Player opp : opponentsList) {
				omwperc += opp.mwp > 0.333 ? opp.mwp : 0.333;
				ogwperc += opp.gwp > 0.333 ? opp.gwp : 0.333;
			}
			omwp = omwperc / opponentsList.size();
			ogwp = ogwperc / opponentsList.size();
		}
		
		public void printFullStats() {
			System.out.println("-------------------------------------------");
			System.out.println(String.format("%s\t%d\t%d\t%d\t%d", name, matchPoints, matchesPlayed, gamePoints, gamesPlayed));
			System.out.println(String.format("\t%.3f\t%.3f\t%.3f\t%.3f", mwp, omwp, gwp, ogwp));
			for (Player opp : opponentsList) {
				System.out.print("\t" + opp.name);
			}
			System.out.println("\n-------------------------------------------");
		}
		
		@Override
		public int compareTo(Player p) {
			if (this.matchPoints > p.matchPoints ||
			    this.matchPoints == p.matchPoints && this.omwp > p.omwp ||
			    this.matchPoints == p.matchPoints && this.omwp == p.omwp && this.gwp > p.gwp ||
			    this.matchPoints == p.matchPoints && this.omwp == p.omwp && this.gwp == p.gwp && this.ogwp > p.ogwp) {
				return -1;
			} else {
				return 1;
			}
		}			
		
		@Override
		public String toString() {
			return String.format("%s\t%d\t%.3f\t%.3f\t%.3f", name, matchPoints, omwp, gwp, ogwp);
		}
	}
		
	class Game {
		public int round;
		public Player player1;
		public Player player2;
		public String result;
		
		public Game(int round, Player player1, Player player2, String result) {
			this.round = round;
			this.player1 = player1;
			this.player2 = player2;
			this.result = result;
		}
		
		
	}
}