package dataStructures;

import utils.DatabaseUtil;
import utils.Utils;
import interfaces.IVehicle;

public class Vehicle implements IVehicle {

	private int vehicle_id;
	private ShortestPath shortestPath; //This is the path that the vehicle must make.
	private Voyage voyage; //This is where all the positioning of the vehicle will be saved.
	private int index; //this index is related to the 'shortest path' trip.
	private GPSSignal checkpoint;
		
	
	public Vehicle(ShortestPath shortestPath){
		this.index = 1;
		this.shortestPath = shortestPath;
		this.voyage = new Voyage(shortestPath.getInstance(0));
		this.setCheckpoint(shortestPath.getInstance(this.index));
	}	

	public ShortestPath getShortestPath() {
		return shortestPath;
	}

	public Voyage getVoyage() {
		return voyage;
	}

	public GPSSignal origin() {
		return this.shortestPath.getInstance(0);
	}
	
	public GPSSignal destination() {
		return this.shortestPath.getInstance(shortestPath.size() - 1);
	}
	
	public void setVehicle_id(int vehicle_id) {
		this.vehicle_id = vehicle_id;
	}

	public int getVehicle_id() {
		return this.vehicle_id;
	}
	
	public String getActualPositionUTM() {
		GPSSignal last = this.voyage.getLast();
		Utils.LatLon2UTM(last);
		return "'POINT("+ last.getLongitude() +" "+ last.getLatitude() +")'::geometry";
	}	

	public GPSSignal getActualPosition(){
		return this.voyage.getLast();
	}
	
	public String toString() {
		return "["+this.vehicle_id+"|"+shortestPath.toString()+"|"+voyage.toString()+"]";
	}

	public String getVoyageFormat() {
		return this.voyage.getFormat();
	}

	public String getShortestPathFormat() {
		return this.shortestPath.getFormat();
	}
	
	public void move(DatabaseUtil database, double timeLeft, double time) {
		GPSSignal position = this.getActualPosition();
		int speedLimit = this.shortestPath.getSpeedLimitAt( this.index );
		assert(speedLimit > 0);
		
		double allowedDistance = timeLeft * speedLimit;
		assert(allowedDistance >= 0);
		
		double distance = Utils.UTMdistance(position, this.checkpoint); //it's in meters;
		assert(distance >= 0);

		GPSSignal newPosition = null;
		if(time == 0){ //First position already added.
			return;
		}
		else if(allowedDistance == 0){
			newPosition = position;
		}
		else if(allowedDistance < distance ){
			newPosition = move(database, distance, allowedDistance, position, this.index);
			
		}
		else { //allowedDistance > distance			
			double tmpTimeLeft = timeLeft - this.timespent(distance, speedLimit);
			assert(tmpTimeLeft <= timeLeft);
			
			int tmpIndex = this.index + 1;
			int tmpSpeedLimit = this.shortestPath.getSpeedLimitAt( tmpIndex);
			double tmpAllowedDistance = tmpTimeLeft * tmpSpeedLimit;
						
									//	database 	distance 			timeLeft	index		position
			newPosition = moveRecursive(database, allowedDistance - distance, tmpTimeLeft, tmpIndex, this.checkpoint);
		}	
		
		assert(newPosition != null): "new position is null";
		assert(newPosition.getFormat() == "UTM"): "new position is not in UTM format";
				
		this.setActualPosition(newPosition, time);
		database.setVehicle(this.vehicle_id, newPosition); //this is for overlap restriction
	}
	
	private GPSSignal moveRecursive(DatabaseUtil database, double distance, double timeLeft, int index, GPSSignal position) {
		
		int speedLimit = this.shortestPath.getSpeedLimitAt( index );
		double allowedDistance = timeLeft * speedLimit;
		
		if(timeLeft == 0){
			return position;
		}
		if(allowedDistance <= distance || allowedDistance == 0){
			return move(database, distance, allowedDistance, position, index);
		}
		else {			
			this.setCheckpoint( this.getShortestPath().getInstance(index) );
			
			double tmpTimeLeft = timeLeft - this.timespent(distance, speedLimit);
			assert(tmpTimeLeft <= timeLeft);
			
			int tmpIndex = index + 1;
			int tmpSpeedLimit = this.shortestPath.getSpeedLimitAt( tmpIndex);
			double tmpAllowedDistance = tmpTimeLeft * tmpSpeedLimit;
			
			return moveRecursive(database, allowedDistance - distance, tmpTimeLeft, tmpIndex, this.checkpoint);
		}		
	}
	
	private GPSSignal move(DatabaseUtil database, double distance, double allowedDistance, GPSSignal position, int index) {
		this.setCheckpoint( this.getShortestPath().getInstance(index) );
		this.index = index;
		float percentage = allowedDistance > distance ?  (float) (distance/allowedDistance) : (float) (allowedDistance/distance);
		GPSSignal newPosition = database.lineInterpolatePoint(this.checkpoint, position, percentage);		
		return newPosition;
	}
	
	private double timespent(double distance, Integer speedLimit) {
		assert(speedLimit > 0);
		return (1/speedLimit.doubleValue())*distance;
	}	

	private void setActualPosition(GPSSignal location, double time){
		this.voyage.addInstance(location, time);
	}

	public void setCheckpoint(GPSSignal checkpoint) {
		this.checkpoint = checkpoint;
	}

	public GPSSignal getCheckpoint() {
		return checkpoint;
	}
}
