package project;

import java.util.List;
import java.util.Scanner;

import org.jgrapht.GraphPath;

import elementclasses.OSMNode;

public class Main {

	private final static String help= "help: list of commands\nload: loading the OSM map\ntraveltime: estimating travel time between two points\nexit: exiting the system";
			
	public static void main (String []args){
		TravelTimeEstimator estimator = null;
		Scanner scanner = new Scanner(System.in);
		while (true){
			System.out.println("Please enter your command or type help for more information");
		    String command = scanner.next();
		    command = command.toLowerCase();
		    if (command.equals("help"))
		    	System.out.println(help);
		    else if (command.equals("load"))
		    {
		    	System.out.println("Please enter your OSM file path");
		    	String OSMPath = scanner.next();
		    	estimator = new TravelTimeEstimator(OSMPath);
		    }
		    else if (command.equals("traveltime")){
		    	if (estimator == null)
		    		System.out.println("Please first provide a map file with load command!");
		    	else{
		    		System.out.println("Please enter the time of departure (hh:mm)");
		    		String timeStr = scanner.next();
		    		String t[] = timeStr.split(":");
		    		int hour, minute;
		    		try{ 
		    			hour = Integer.parseInt(t[0]);	
		    			minute = Integer.parseInt(t[1]);
		    			Time departureTime = new Time (hour,minute);
		    			estimator.setStartTime(departureTime);
		    			System.out.println("Please enter the number of paths");
		    			String kStr = scanner.next();
		    			int k = Integer.parseInt(kStr);

		    			String menu = "1: Estimation based on node's names\n2: Estimation based on node's ids\n3: Estimation based on lat and long";
		    			System.out.println("Please select one of the options");
		    			System.out.println(menu);
		    			String optionStr = scanner.next();
		    			List<GraphPath<OSMNode, MapWay>> paths;
		    			if (optionStr.equals("1")){
		    				System.out.println("Please enter the source name:");
		    				String source = scanner.nextLine();
		    				source = scanner.nextLine();
		    				System.out.println("Please enter the destination name:");
		    				String dest = scanner.nextLine();
		    				paths = estimator.getShortestPath(source, dest, k);
		    				showPaths(paths,estimator);
		    			}
		    			else if (optionStr.equals("2")){
		    				System.out.println("Please enter the source id:");
		    				String source = scanner.next();
		    		
		    				System.out.println("Please enter the destination id:");
		    				String dest = scanner.next();
		    				
		    				paths = estimator.getShortestPathByID(source, dest, k);
		    				showPaths(paths,estimator);
		    			}
		    			else if (optionStr.equals("3")){

		    				System.out.println("Please enter the source lat and lon (lat,lon)");
		    				String latlonStr = scanner.next();
		    				String latlons[] = latlonStr.split(",");
		    				double srcLat = Double.parseDouble(latlons[0]);
		    				double srcLon = Double.parseDouble(latlons[1]);

		    				System.out.println("Please enter the destination lat and lon (lat,lon)");
		    				latlonStr = scanner.next();
		    				latlons = latlonStr.split(",");
		    				double destLat = Double.parseDouble(latlons[0]);
		    				double destLon = Double.parseDouble(latlons[1]);
		    				
		    				paths = estimator.getShortestPath(srcLat, srcLon, destLat, destLon, k);
		    				showPaths(paths,estimator);

		    			}
		    			else 
		    				System.out.println("Wrong option number");


		    		}
		    		catch (Exception e){
		    			System.out.println("Wrong format");
		    		}
		    	}
		    }
		    else if (command.equals("exit")){
		    	scanner.close();
		    	break;
		    }
		    else 
		    	System.out.println("wrong command please enter help for more information\n");
		}
		
	}
	
	public static void showPaths(List<GraphPath<OSMNode, MapWay>> paths, TravelTimeEstimator estimator)
	{
		int current = 1;
		for(GraphPath<OSMNode,MapWay> path:paths)
		{
			System.out.println("***************** Path #" + current + " *************************");
			estimator.printPath(path);
			current++;
		}
	}
}
