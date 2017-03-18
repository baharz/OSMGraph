package project;

import java.util.HashMap;
import java.util.Map;

import org.jgrapht.graph.DefaultWeightedEdge;

import elementclasses.OSMNode;
import elementclasses.Way;
import project.Time;

public class MapWay extends DefaultWeightedEdge {
	
	//Constants
	private static final int earthRadius = 3959;
	public static final Map<String,Integer> defaultSpeed;
    static
    {
        defaultSpeed = new HashMap<String, Integer>();
        defaultSpeed.put("motorway_link",30);
        defaultSpeed.put("secondary_link",20);
        defaultSpeed.put("primary_link",25);
        defaultSpeed.put("tertiary_link",15);
        defaultSpeed.put("trunk_link",35);
        defaultSpeed.put("trunk",50);
        defaultSpeed.put("motorway",60);
        defaultSpeed.put("primary",40);
        defaultSpeed.put("secondary",25);
        defaultSpeed.put("tertiary",25);
        defaultSpeed.put("unclassified",20);
        defaultSpeed.put("residential",10);
        defaultSpeed.put("service",10);
        defaultSpeed.put("road",15);
    }
    
    public static final double[] time2congestionFactor;
    static{
    	time2congestionFactor = new double[48];
    	for(int i=0; i<12;i++)
    		time2congestionFactor[i] = 3.0;
    	time2congestionFactor[12] = 3.5;
    	time2congestionFactor[13] = 4.0;
    	time2congestionFactor[14] = 4.5;
    	time2congestionFactor[15] = 5.0;
    	time2congestionFactor[16] = 4.5;
    	time2congestionFactor[17] = 4.0;
    	time2congestionFactor[18] = 3.5;
    	for(int i=19;i<30;i++)
    		time2congestionFactor[i] = 3.0;
    	time2congestionFactor[30] = 3.5;
    	time2congestionFactor[31] = 4.0;
    	time2congestionFactor[32] = 4.5;
    	time2congestionFactor[33] = 5.0;
    	time2congestionFactor[34] = 4.5;
    	time2congestionFactor[35] = 4.0;
    	time2congestionFactor[36] = 3.5;
    	for(int i=37;i<48;i++)
    		time2congestionFactor[i] = 3.0;
    }
	
	// Member variables
	private OSMNode source;
	private OSMNode dest;
	private int speedLimit;
	private Way parentWay;
	private double dist;
	// Get functions
	public OSMNode getSource()
	{
		return source;
	}
	public OSMNode getDestination()
	{
		return this.dest;
	}
	public int getSpeedLimit()
	{
		return this.speedLimit;
	}
	public Way getParentWay()
	{
		return this.parentWay;
	}
	public double getDistance()
	{
		return this.dist;
	}
	
	public double getCongestionFactor(Time startTime)
	{
		int minutes = startTime.convertToMinutes();
		int idx = (int)Math.floor((double)minutes/30.0);
		return time2congestionFactor[idx];
	}
	
	// Set functions
	public void setNodes(OSMNode source, OSMNode dest)
	{
		this.source = source;
		this.dest = dest;
		this.dist = computeDistance();
	}
	
	public void setParentWay(Way parentWay)
	{
		this.parentWay = parentWay;
		this.speedLimit = this.determineSpeed();
	}
	
	
	//Constructors
	public MapWay() {}
	
	public MapWay(OSMNode source, OSMNode dest, Way way){
		this.source= source;this.dest=dest;this.parentWay=way;	
		this.dist = computeDistance();
		this.speedLimit = this.determineSpeed();
		
	}
	
	public double getTravelTime(Time startTime)
	{
		double congestionFactor = this.getCongestionFactor(startTime);
		return (this.dist / this.speedLimit)*congestionFactor;
		
	}
	
	// Helper functions
	private double computeDistance(){
		double lon2 = Double.parseDouble(dest.lon);
		double lon1 = Double.parseDouble(source.lon);
		double lat2 = Double.parseDouble(dest.lat);
		double lat1 = Double.parseDouble(source.lat);
		double pi180 = Math.PI/180;
		double dLat = pi180*(lat2-lat1);  // deg2rad below
		double dLon = pi180*(lon2-lon1); 
		double a = 
		    Math.sin(dLat/2) * Math.sin(dLat/2) +
		    Math.cos(pi180*(lat1)) * Math.cos(pi180* lat2) * 
		    Math.sin(dLon/2) * Math.sin(dLon/2)
		    ; 
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
		return earthRadius * c; 
	}
	private int determineSpeed(){
		if (this.parentWay.getTags().containsKey("maxspeed")){
			String speedString = parentWay.getTags().get("maxspeed");
			String[] splits = speedString.split(" ");
			return Integer.parseInt(splits[0]);
		}
		else{
		return defaultSpeed.get(parentWay.getType());
		}
		
	}
	
	// functions that should be implemented for jgrapht edge
	private static final long serialVersionUID = 1L;

    public int hashCode()
    {
    	return this.parentWay.hashCode();
    }

    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof MapWay)) {
            return false;
        }
        return false;
    }
	
}
