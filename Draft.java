/**
 * known bugs: cant handle leavers
 *			check mtg tie breaks
 *			
 */

import java.util.Arrays;
import java.io.File;
import java.util.*;


class Draft {
	private int playersNum;
	private int roundsNum;
	private Player[] stats;
	private Game[] games;
	private Player bye;
	final static String INPUT_FILE_NAME = "in.txt";
	
	public static void main(String[] args) throws Exception {
		Draft draft = new Draft();
		draft.init();
		draft.readInput(args);
		draft.process();
		draft.print(args);
	}
	
	public void init() {
		bye = new Player("Bye");
		/*stats = new Player[5];
		stats[0] = new Player("one", 5, 3, 0, 2);
		stats[1] = new Player("two", 4, 3, 0, 2);
		stats[2] = new Player("three", 8, 3, 0, 2);
		stats[3] = new Player("four", 5, 1, 0, 2);
		stats[4] = new Player("five", 5, 3, 7, 2);*/
		
	}
	
	private void log(String s) {
		System.out.println(s);
	}
	
	public void readInput(String[] args) throws Exception {
		String input;
		if (args.length == 0) {
			input = INPUT_FILE_NAME;
		} else {
			input = args[0];
		}
		Scanner sc = new Scanner(new File(input), "UTF-8");
		
		/*if (!sc.hasNext()) {
			return;
		}*/
		playersNum = sc.nextInt();
		stats = new Player[playersNum];
		/*if (!sc.hasNext()) {
			return;
		}*/
		roundsNum = sc.nextInt();
		int gamesPerRound = (playersNum + 1) / 2;
		games = new Game[roundsNum * gamesPerRound];
		for (int i = 0; i < playersNum && sc.hasNext(); i++) {
			stats[i] = new Player(sc.next());
		}
		/*if (i != playersNum) {
			return;
		}*/
		
		String s1, s2, s3;
		for (int i = 0; i < roundsNum; i++) {
			for (int j = 0; j < gamesPerRound; j++) {
				s1 = sc.next();
				s2 = sc.next();
				if (s2.equals("Bye") || s2.equals("Бай")) {
					games[i * gamesPerRound + j] = new Game(i + 1, getPlayerByName(s1), bye, "2:0");
				} else {
					s3 = sc.next();
					games[i * gamesPerRound + j] = new Game(i + 1, getPlayerByName(s1), getPlayerByName(s3), s2);
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
		// тут надо перечитать правила и пересмотреть код. пока что гарантированно корректно обрабатываются только 2:0 2:1 0:2 1:2 1:1 0:0
		for (int i = 0; i < games.length; i++) {
			//log("game" + i);
			win = games[i].result.charAt(0) - 48;
			loss = games[i].result.charAt(2) - 48;
			if (games[i].result.length() == 5) {
				draw = games[i].result.charAt(4) - 48;
			} else {
				draw = 0;
			}
			if (games[i].player1 != bye && games[i].player2 != bye) {
				games[i].player1.opponentsList.add(games[i].player2);
				games[i].player2.opponentsList.add(games[i].player1);
			}
			if (games[i].player2 != bye) {
				//log("player2 != bye");
				games[i].player2.matchesPlayed++;
				games[i].player2.gamePoints += loss * 3 + draw * 1;
				games[i].player2.gamesPlayed += win + loss + draw;
			}
			if (games[i].player1 != bye) {
				//log("player1 != bye");
				games[i].player1.matchesPlayed++;
				games[i].player1.gamePoints += win * 3 + draw * 1;
				games[i].player1.gamesPlayed += win + loss + draw;
			}
			// bye cant win or draw so no check required
			if (win > loss) {
				games[i].player1.matchPoints += 3;
			} else if (loss > win) {
				games[i].player2.matchPoints += 3;
			} else {
				games[i].player1.matchPoints += 1;
				games[i].player2.matchPoints += 1;
			}
		}
		for (int i = 0; i < stats.length; i++) {
			stats[i].calculateStats();
		}
		for (int i = 0; i < stats.length; i++) {
			stats[i].calculateOppStats();
		}
		Arrays.sort(stats);
	}
	
	public void print(String[] args) {
		System.out.println("Draft results:");
		if (args.length > 0 && args[0].equals("-d")) {
			for (int i = 0; i < stats.length; i++) {
				stats[i].printFullStats();
			}
		} else {
			for (int i = 0; i < stats.length; i++) {
				System.out.println(stats[i]);
			}
		}
	}
		
	private Player getPlayerByName(String name) {
		for (int i = 0; i < playersNum; i++) {
			if (stats[i].name.equals(name)) {
				return stats[i];
			}
		}
		if (name.equals("Bye")) {
			return bye;
		}
		return null;
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
		
		public Player(String name, double mwp, double omwp, double gwp, double  ogwp) {
			this.name = name;
			this.mwp = mwp;
			this.omwp = omwp;
			this.gwp = gwp;
			this.ogwp = ogwp;
		}
		
		public Player(String name) {
			this.name = name;
			/*this.mwp = 0;
			this.omwp = 0;
			this.gwp = 0;
			this.ogwp = 0;
			this.matchesPlayed = 0;
			this.matchPoints = 0;
			this.gamesPlayed = 0;
			this.gamePoints = 0;
			this.opponentsList = null;*/
		}
		
		public void calculateStats() {
			mwp = (double) matchPoints / (3 * matchesPlayed);
			if (mwp < 0.333) {
				mwp = 0.333;
			}
			gwp = (double) gamePoints / (3 * gamesPlayed);
			if (gwp < 0.333) {
				gwp = 0.333;
			}
		}
		
		public void calculateOppStats() {
			double omwperc = 0;
			double ogwperc = 0;
			for (Player opp : opponentsList) {
				omwperc += opp.mwp;
				ogwperc += opp.gwp;
			}
			omwp = omwperc / opponentsList.size();
			ogwp = ogwperc / opponentsList.size();
		}
		
		public void printFullStats() {
			System.out.println("----------------------------------------------------------");
			System.out.println(String.format("%s\t%d\t%d\t%d\t%d", name, matchPoints, matchesPlayed, gamePoints, gamesPlayed));
			System.out.println(String.format("\t%.3f\t%.3f\t%.3f\t%.3f", mwp, omwp, gwp, ogwp));
			for (Player opp : opponentsList) {
				System.out.print("\t" + opp.name);
			}
			System.out.println("\n----------------------------------------------------------");
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