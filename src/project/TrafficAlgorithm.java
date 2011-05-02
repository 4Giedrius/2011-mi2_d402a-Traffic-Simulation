package project;

import java.util.ArrayList;

import utils.GenerationUtil;

import dataStructures.Vehicle;
import dataStructures.Zone;

public class TrafficAlgorithm {
	
	public static void start(Integer numberOfCars, 
							 Double frequency, 
							 ArrayList<Zone> fromZones, 
							 ArrayList<Zone> toZones) {
		
		
		/*
		System.out.println("number of cars: "+numberOfCars+"\nfrequency: "+frequency);
		System.out.println("from:");
		for(Zone z : fromZones)
			System.out.println(z.toString());
		System.out.println("to:");
		for(Zone z : toZones)
			System.out.println(z.toString());
		*/
		GenerationUtil gen = new GenerationUtil();
		for(int i = 0; i < numberOfCars; i++){
			Vehicle v = gen.generateVehicle(fromZones.get(0), toZones.get(0));
			System.out.println(v.toString());
		}	
		
	}	
	
}