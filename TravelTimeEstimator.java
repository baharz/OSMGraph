package project;

import java.util.List;
import java.util.Set;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import elementclasses.OSM;
import elementclasses.OSMNode;
import elementclasses.Way;
import parser.OSMParser;

public class TravelTimeEstimator {
	// private member variables
	private Time startTime;
	private OSM osm;
	private DirectedWeightedMultigraph<OSMNode, MapWay>  dir_map;

	// Get functions
	public Time getStartTime()
	{
		return startTime;
	}
	public DirectedWeightedMultigraph<OSMNode, MapWay> getMap()
	{
		return this.dir_map;
	}

	// Set functions
	public void setStartTime(Time time)
	{
		this.startTime = time;
		this.changeTime(time);
	}


	//Constructors
	public TravelTimeEstimator(String mapPath){
		startTime = new Time(0,0);
		dir_map = new DirectedWeightedMultigraph<>(MapWay.class);
		try{
			this.osm = OSMParser.parse(mapPath);
			this.createGraph();
		}
		catch(Exception e){
			System.out.println("The map osm file could not be parsed!");
		}


	}

	// Member functions
	public List<GraphPath<OSMNode,MapWay>> getShortestPath(OSMNode s, OSMNode d, int k)
	{
		KShortestPaths<OSMNode, MapWay> kShortest = new KShortestPaths<OSMNode, MapWay>(dir_map, s, k);
		return kShortest.getPaths(d);
	}

	public List<GraphPath<OSMNode,MapWay>> getShortestPath(String sourceName, String destName,int k)
	{
		OSMNode s = this.findNodeByName(sourceName);
		OSMNode d = this.findNodeByName(destName);
		if(s == null)
			System.out.println("The source name: " + sourceName + " could not be found in the map!");
		else if(d == null)
			System.out.println("The destionation name: " + sourceName + " could not be found in the map!");
		else
			return getShortestPath(s, d, k);

		return null;

	}

	public List<GraphPath<OSMNode,MapWay>> getShortestPath(double source_lat,double source_lon, double dest_lat,double dest_lon, int k)
	{
		OSMNode s = this.findNodeByCoord(source_lat, source_lon);
		OSMNode d = this.findNodeByCoord(dest_lat, dest_lon);
		System.out.println("Closest node to source has (lat,lon) =  (" + s.getLatitude() + "," + s.getLongitude() +")" );
		System.out.println("Closest node to destination has (lat,lon) =  (" + d.getLatitude() + "," + d.getLongitude() +")" );
		return getShortestPath(s, d, k);
	}

	public List<GraphPath<OSMNode,MapWay>> getShortestPathByID(String source_id,String dest_id, int k)
	{
		OSMNode s = this.findNodeByID(source_id);
		OSMNode d = this.findNodeByID(dest_id);
		if(s==null)
			System.out.println("There is no source node with this ID");
		if(d==null)
			System.out.println("There is no destination node with this ID");
		else
			return getShortestPath(s, d, k);
		return null;
	}

	public void printPath(GraphPath<OSMNode,MapWay> path)
	{
		List<MapWay> ways = path.getEdgeList();
		//Printing based on way names
		String nameBasedPath = "";
		if(ways.get(0).getParentWay().getTags().containsKey("name"))
			nameBasedPath += ways.get(0).getParentWay().getTags().get("name");
		else
			nameBasedPath += "noname";


		String latlonBasedPath = "(" + ways.get(0).getSource().getLatitude() + "," + ways.get(0).getSource().getLongitude() + ")"; 
		String idBasedPath = ways.get(0).getSource().id;
		double dist = 0;
		String previous_name = nameBasedPath;
		for(MapWay way:ways)
		{
			idBasedPath += 	"->";
			latlonBasedPath += "->";

			if(way.getParentWay().getTags().containsKey("name")){
				String cur_name =  way.getParentWay().getTags().get("name");
				if(!cur_name.equals(previous_name)){
					nameBasedPath +="->"+cur_name ;
					previous_name = cur_name;
				}
			}
			else{
				if(!previous_name.equals("noname")){
					nameBasedPath +="->"+"noname" ;
					previous_name = "noname";
				}
			}

			idBasedPath += way.getDestination().getId();
			latlonBasedPath +=  "(" + way.getDestination().getLatitude() + "," + way.getDestination().getLongitude() + ")"; 
			dist += way.getDistance();
		}

		System.out.printf("The estimated travel time is: %3.2f minutes\n", path.getWeight() * 60);
		System.out.printf("The distance is: %3.2f miles\n", dist);
		System.out.println("********** The path based on way names ************");
		System.out.println(nameBasedPath);
		System.out.println("********** The path based on (Lat,Long) of the nodes ************");
		System.out.println(latlonBasedPath);
		System.out.println("********** The path based on node IDs ************");
		System.out.println(idBasedPath);

	}

	// Helper functions
	private OSMNode findNodeByCoord(double lat, double lon)
	{
		OSMNode found = null;
		double best_diff = Double.MAX_VALUE;
		for(OSMNode node:dir_map.vertexSet())
		{
			double cur_lat = Double.parseDouble(node.getLatitude());
			double cur_lon = Double.parseDouble(node.getLongitude());
			double cur_diff = Math.pow(cur_lat-lat,2.0)+ Math.pow(cur_lon-lon, 2.0);
			if(cur_diff < best_diff)
			{
				found = node;
				best_diff = cur_diff;
			}
		}
		return found;
	}
	private OSMNode findNodeByID(String idStr)
	{
		OSMNode found = null;
		for(OSMNode node:dir_map.vertexSet())
		{
			if(node.getId().equals(idStr))
			{
				found = node;
				break;
			}
		}
		return found;
	}
	private OSMNode findNodeByName(String name)
	{
		String name2found = name.toLowerCase();
		OSMNode found = null;
		for(MapWay cur_way:dir_map.edgeSet())
		{
			if(cur_way.getParentWay().getTags().containsKey("name"))
			{
				String cur_name = cur_way.getParentWay().getTags().get("name");
				cur_name = cur_name.toLowerCase();
				if(cur_name.equals(name2found))
				{
					found = cur_way.getParentWay().nodes.get(0);
					break;
				}
			}
		}
		// If the name is not found in the ways look for the node in the nodes
		if(found==null)
		{
			Set<OSMNode> nodes = this.osm.getNodes();
			for(OSMNode node: nodes)
			{
				if(node.getTags().containsKey("name"))
				{
					String cur_name = node.getTags().get("name").toLowerCase();
					if(name2found.equals(cur_name))
					{
						// Find the closest node to this node in our graph
						double lat = Double.parseDouble(node.lat);
						double lon = Double.parseDouble(node.lon);

						found = this.findNodeByCoord(lat, lon);
						//double found_lat = Double.parseDouble(found.lat);
						//double found_lon = Double.parseDouble(found.lon);
						//System.out.printf("The location found with (lat,lon) = (%f,%f)",found_lat,found_lon);
						break;
					}
				}
			}
		}
		return found;
	}


	private void createGraph()
	{
		// Create a node selection object to filter good ways
		NodeSelection ns = new NodeSelection();
		for(Way cur_way:osm.getWays())
		{
			if (ns.validateWay(cur_way))
			{   
				List<OSMNode> cur_nodes = cur_way.nodes;
				for(int i=1;i<cur_nodes.size();i++)
				{
					//System.out.println(i);
					if(!dir_map.containsVertex(cur_nodes.get(i-1)))
					{
						dir_map.addVertex(cur_nodes.get(i-1));
					}


					if(!dir_map.containsVertex(cur_nodes.get(i)))
					{
						dir_map.addVertex(cur_nodes.get(i));
					}

					if (cur_way.isOneway()){
						MapWay newWay = new MapWay(cur_nodes.get(i-1),cur_nodes.get(i),cur_way);
						dir_map.addEdge(cur_nodes.get(i-1),cur_nodes.get(i),newWay);
						double curWeight = newWay.getTravelTime(startTime);
						dir_map.setEdgeWeight(newWay,curWeight);
					}
					else{ 
						MapWay newWay1 = new MapWay(cur_nodes.get(i-1),cur_nodes.get(i),cur_way);
						dir_map.addEdge(cur_nodes.get(i-1),cur_nodes.get(i),newWay1);
						double curWeight = newWay1.getTravelTime(startTime);
						dir_map.setEdgeWeight(newWay1, curWeight);
						MapWay newWay2 = new MapWay(cur_nodes.get(i),cur_nodes.get(i-1),cur_way);
						dir_map.addEdge(cur_nodes.get(i),cur_nodes.get(i-1),newWay2);
						curWeight = newWay2.getTravelTime(startTime);
						dir_map.setEdgeWeight(newWay2,curWeight);
					}


				}
			}
		}

	}
	private void changeTime(Time newTime)
	{
		for(MapWay curWay: dir_map.edgeSet())
		{
			double newWeight = curWay.getTravelTime(newTime);
			dir_map.setEdgeWeight(curWay, newWeight);
		}

	}


}
