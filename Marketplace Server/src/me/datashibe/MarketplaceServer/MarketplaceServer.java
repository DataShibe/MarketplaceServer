package me.datashibe.MarketplaceServer;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Scanner;

import javax.imageio.ImageIO;

public class MarketplaceServer {

	public static final int port = 5252;
	public static String recourcesDirectory;
	
	public static void main(String[] args) {
		
		try {
			recourcesDirectory = System.getProperty("user.dir").split("Marketplace Server")[0] + "Marketplace Server/src/recources/";
			ServerSocket serverSocket = new ServerSocket(port);
			
			System.out.println("Server started on port " + port + "\n\n");
			
			while(true) {
				
				Socket socket = serverSocket.accept();
				Client client = new Client(socket);

				System.out.println("\n\nClient " + socket.getInetAddress() + ":" + socket.getPort() + " connected");
				
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
				
				new Thread() {
					
					public void run() {
						
						try {
							
							String message;
							
							while((message = bufferedReader.readLine()) != null) {
								
								System.out.println("[" + socket.getInetAddress() + ":" + socket.getPort() + "]: " + message);
							
								String command = message.split(" ")[0];
								String[] params = message.split(command + " ")[1].split(" ");
								
								String username;
								String password;
								
								if(!command.equals("login") && !command.equals("register") && !client.loggedIn) {
									
									printWriter.println("not logged in");
									continue;
									
								}
								
								switch(command) {
								
								case "login":
									username = params[0];
									password = params[1];
									
									if(loginUser(username, password)) {
										printWriter.println("login confirm");
										
										client.loggedIn = true;
										client.username = username;
										
										break;
									}
									
									printWriter.println("incorrect login");
									
									break;
								
								case "register": 
									username = params[0];
									password = params[1];
									
									if(registerUser(username, password)) {
										printWriter.println("register confirm");
										
										client.loggedIn = true;
										client.username = username;
										
										break;
									}
									
									printWriter.println("user already exists");
									
									break;
									
								case "user-get":
									printWriter.println(userGet(client.username, params[0]));
									break;
								
								case "list":
									printWriter.println(list(params[0], client.username));
									break;
									
								case "car-get":
									sendPicture(socket, Integer.parseInt(params[0]));
									sendFile(printWriter, Integer.parseInt(params[0]));
									break;
									
								case "buy":
									File carFile = new File(recourcesDirectory + "/cardata/" + params[0] + ".txt");
									Scanner fileScanner = new Scanner(carFile);
									
									int counter = 0;
									while(counter < 5) {
										
										fileScanner.nextLine();
										counter++;
										
									}
									
									int money = Integer.parseInt(fileScanner.nextLine().split(": ")[1].replace("$", "").replace(",", ""));
									if(!hasUserEnoughMoney(client.username, money)) {
										
										printWriter.println("not enough money");
										break;
										
									}
									
									String amount = "" + (Integer.parseInt(userGet(client.username, "money").replace("$", "").replace(",", "")) - money);
									String newAmount = "";
									int zeroCounter = 0;
									for(int i = (amount.toCharArray().length - 1); i >= 0; i--) {
										
										if(zeroCounter == 3) {
										
											newAmount = "," + newAmount; 
											zeroCounter = 0;
										
										}
									
										zeroCounter++;
										newAmount = amount.toCharArray()[i] + newAmount;
									
									}
									
									modifyUser(client.username, "money", "$" + newAmount);
									
									if(userGet(client.username, "cars").equals("[]")) modifyUser(client.username, "cars", "[" + params[0] + "]");
									else modifyUser(client.username, "cars", userGet(client.username, "cars").replace("]", ", " + params[0] + "]"));
									
									printWriter.println("buyed successfully");
									break;
									
								case "sell":
									File file = new File(recourcesDirectory + "/cardata/" + params[0] + ".txt");
									Scanner scanner = new Scanner(file);
									
									int i = 0;
									while(i < 5) {
										
										scanner.nextLine();
										i++;
										
									}
									
									
									int carMoney = Integer.parseInt(scanner.nextLine().split(": ")[1].replace("$", "").replace(",", ""));
									
									String a = "" + (Integer.parseInt(userGet(client.username, "money").replace("$", "").replace(",", "")) + carMoney);
									System.out.println(a);
									String na = "";
									int c = 0;
									for(int j = (a.toCharArray().length - 1); j >= 0; j--) {
										
										if(c == 3) {
										
											na = "," + na; 
											c = 0;
										
										}
									
										c++;
										na = a.toCharArray()[j] + na;
									
									}
									
									modifyUser(client.username, "money", "$" + na);
									modifyUser(client.username, "cars", userGet(client.username, "cars").replace(params[0] + ",", "").replace(params[0], ""));
									
									printWriter.println("sold successfully");
									
									break;
									
								default:
									printWriter.println("unknown command");
									break;
								}
							}
						
						} catch (Exception e) { e.printStackTrace(); }
						
					}
					
				}.start();
				
			}
			
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	
	public static String list(String mod, String username) {
		switch(mod) {
		
		case "garage":
			return userGet(username, "cars");
			
		case "used-cars":
			
			File dir = new File(recourcesDirectory + "/cardata/");
			File[] cars = dir.listFiles();
			Scanner fileScanner;
			
			String result = "";
			
			for(File car : cars) {
				
				if(!car.getAbsolutePath().endsWith(".txt")) continue;
				System.out.println(car.getName());
				
				try {
					
					fileScanner = new Scanner(car);

					int counter = 0;
					
					while(counter < 4) {
						
						fileScanner.nextLine(); 
						counter++;
						
					}
					
					int year = Integer.parseInt(fileScanner.nextLine().split(": ")[1]);
					if(year < 2000) result += car.getName().replace(".txt", "") + ", "; 
					
				} catch(Exception e) { e.printStackTrace(); }
				
			}
			
			return "[" + result.substring(0, result.toCharArray().length - 2) + "]";
			
		case "new-cars":
			
			File directory = new File(recourcesDirectory + "/cardata/");
			File[] cs = directory.listFiles();
			Scanner scanner;
			
			String res = "";
			
			for(File car : cs) {
				
				if(!car.getAbsolutePath().endsWith(".txt")) continue;
				
				try {
					
					fileScanner = new Scanner(car);

					int counter = 0;
					
					while(counter < 4) {
						
						fileScanner.nextLine(); 
						counter++; 
					
					}
					
					int year = Integer.parseInt(fileScanner.nextLine().split(": ")[1]);
					if(year > 2000) res += car.getName().replace(".txt", "") + ", "; 
					
				} catch(Exception e) { e.printStackTrace(); }
				
			}
			if (res == "") return "[]";
			return "[" + res.substring(0, res.length() - 2) + "]";
			
		}
		
		return "";
	}
	
	public static String userGet(String username, String var) {
		
		try {
		
			Scanner fileScanner = new Scanner(new File(recourcesDirectory + "/userdata/" + username + ".txt"));
			String line = "";
			
			while(fileScanner.hasNextLine()) {
			
				line = fileScanner.nextLine();
				
				if(line.split(": ")[0].equals(var)) {
					return line.split(": ")[1];
				}
				
			}
			
			return null;
		
		} catch (FileNotFoundException e) {	e.printStackTrace(); }
		
		return null;
	}
	
	public static boolean loginUser(String username, String password) {
		
		File userDataDirectory = new File(recourcesDirectory + "/userdata/");
		File[] userData = userDataDirectory.listFiles();
		
		for(File user : userData) {
			
			if(user.getName().split(".txt")[0].equals(username)) {
				
				try {
					
					Scanner fileScanner = new Scanner(user);
					String content = "";
					
					while(fileScanner.hasNextLine()) content += fileScanner.nextLine() + "\n";
					
					fileScanner.close();
					
					if(content.split("\n")[1].split(": ")[1].equals(password)) { return true; }
					
				} catch (FileNotFoundException e) { e.printStackTrace(); }
			}
		}
		
		return false;
	}
	
	public static boolean registerUser(String username, String password) {
		
		File userDataDirectory = new File(recourcesDirectory + "/userdata/");
		File[] userData = userDataDirectory.listFiles();
		
		for(File user : userData) 	if(user.getName().split(".txt")[0].equals(username)) { return false; }
		
		File newUser = new File(userDataDirectory.getAbsolutePath() + "/" + username + ".txt");
		
		try {
			
			newUser.createNewFile();
			
			PrintWriter fileWriter = new PrintWriter(new FileWriter(newUser));
			fileWriter.println("name: " + username);
			fileWriter.println("psswrd: " + password);
			fileWriter.println("money: $14,000");
			fileWriter.println("cars: []");
			fileWriter.close();
			
			return true;
		
		} catch (IOException e) { e.printStackTrace(); }
		
		return false;
	}
	
	public static void sendPicture(Socket socket, int id) {
		
		File picture = new File(recourcesDirectory + "/cardata/" + id + ".jpg");
		
		try {
			
			BufferedImage image = ImageIO.read(picture);
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			OutputStream outputStream = socket.getOutputStream();
			
			ImageIO.write(image, "jpg", byteArrayOutputStream);
			
			byte[] size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
			outputStream.write(size);
			outputStream.write(byteArrayOutputStream.toByteArray());
			outputStream.flush();
		
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	public static void sendFile(PrintWriter printWriter, int id) {
		
		File file = new File(recourcesDirectory + "/cardata/" + id + ".txt");
		try {
		
			Scanner fileScanner = new Scanner(file);
			String fileContent = "";
			
			while(fileScanner.hasNextLine()) fileContent += fileScanner.nextLine() + "/n";
			
			printWriter.println(fileContent);
		
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	public static void modifyUser(String username, String var, String param) {
		
		try {
		
			Scanner fileScanner = new Scanner(new File(recourcesDirectory + "/userdata/" + username + ".txt"));
			String content = "";
			
			while(fileScanner.hasNextLine()) content += fileScanner.nextLine() + "\n";
			
			String nContent = "";
			FileWriter fileWriter = new FileWriter(recourcesDirectory + "/userdata/" + username + ".txt", false);

			for(int i = 0; i < content.split("\n").length; i++) {
			
				if(content.split("\n")[i].startsWith(var + ": ")) nContent += var + ": " + param + "\n";
				else nContent += content.split("\n")[i] + "\n";
			
			}
			
			fileWriter.write(nContent);
			fileWriter.flush();
			fileWriter.close();
			
		} catch (IOException e) { e.printStackTrace(); }
		
	}
	
	public static boolean hasUserEnoughMoney(String username, int amount) {
		return Integer.parseInt(userGet(username, "money").replace("$", "").replace(",", "")) >= amount;
	}			
}


class Client {

	Socket socket;
	boolean loggedIn;
	String username;
	
	public Client(Socket s) {
		socket = s;
		loggedIn = false;
	}
	
}

