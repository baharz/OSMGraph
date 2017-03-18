package project;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import elementclasses.OSMNode;
import elementclasses.Way;

public class NodeSelection {
	List<String> interestedNodes;
	List<String> interestedWays;
     public NodeSelection(){
         interestedNodes = new ArrayList<String> ();
    	 interestedNodes.add("traffic_signals");
    	 interestedNodes.add("stop");
    	 interestedNodes.add("crossing");interestedNodes.add("road");
    	 interestedNodes.add("buss_stop");interestedNodes.add("motorway_junction");
    	 interestedNodes.add("turning_circle");interestedNodes.add("motorway_junction");

         interestedWays = new ArrayList<String>();
         interestedWays.add("motorway_link");
         interestedWays.add("secondary_link");
         interestedWays.add("primary_link");
         interestedWays.add("tertiary_link");
         interestedWays.add("trunk_link");
         interestedWays.add("trunk");
         interestedWays.add("motorway");
         interestedWays.add("primary");
         interestedWays.add("secondary");
         interestedWays.add("tertiary");
         interestedWays.add("unclassified");
         interestedWays.add("residential");
         interestedWays.add("service");
         interestedWays.add("road");
     }
     public boolean validateNode(OSMNode n){
    	 if ( n.tags.containsKey("highway") && interestedNodes.contains(n.tags.get("highway")))
    		 return true;
    	 else return false;	 
     }
     public boolean validateWay(Way w){
    	 if ( w.tags.containsKey("highway") && interestedWays.contains(w.tags.get("highway")))
    		 return true;
    	 else return false;	 
     }

}
