package me.datashibe.MarketplaceServer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class CarImporter {

	public static void main(String[] args) {
		String name;
		String manifacturer;
		String year;
		String country;
		String engine;
		String id;
		String prize;
		String torque;
		String hp;
		String peakRPM;
		String topSpeed;
	
		Scanner scanner = new Scanner(System.in);

		System.out.println("ID: ");
		id = scanner.nextLine();
		
		System.out.println("Name: ");
		name = scanner.nextLine();
		
		System.out.println("Manifacturer: ");
		manifacturer = scanner.nextLine();
		
		System.out.println("Year: ");
		year = scanner.nextLine();

		System.out.println("Country: ");
		country = scanner.nextLine();

		System.out.println("Engine: ");
		engine = scanner.nextLine();

		System.out.println("Prize: ");
		prize = scanner.nextLine();

		System.out.println("torque: ");
		torque = scanner.nextLine();

		System.out.println("hp: ");
		hp = scanner.nextLine();

		System.out.println("Peak RPM: ");
		peakRPM = scanner.nextLine();

		System.out.println("Top-Speed: ");
		topSpeed = scanner.nextLine();
		
		File file = new File(System.getProperty("user.dir").split("Marketplace Server")[0] + "Marketplace Server/src/recources/cardata/" + id + ".txt");
		try {
			file.createNewFile();

			PrintWriter printWriter = new PrintWriter(new FileWriter(file));
			
			printWriter.println("id: " + id);
			printWriter.println("name: " + name);
			printWriter.println("manifacturer: " + manifacturer);
			printWriter.println("country: " + country);
			printWriter.println("year: " + year);
			printWriter.println("prize: " + prize);
			printWriter.println("engine: " + engine);
			printWriter.println("torque: " + torque);
			printWriter.println("hp: " + hp);
			printWriter.println("peak-rpm: " + peakRPM);
			printWriter.println("top-speed: " + topSpeed);
			
			printWriter.flush();
			printWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
