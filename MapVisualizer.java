package project;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jgrapht.graph.*;

import elementclasses.OSM;
import elementclasses.OSMNode;
import elementclasses.Way;
import parser.OSMParser;
import project.VisualNode;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JApplet;
import org.jgraph.JGraph;
import org.jgraph.graph.CellView;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgrapht.ext.JGraphModelAdapter;
public class MapVisualizer extends JApplet {

	private static final long serialVersionUID = 1;
	//Constants
	private static final Color     DEFAULT_BG_COLOR = Color.decode( "#FAFBFF" );
	private static final int mapSize = 800;
	private static final Dimension DEFAULT_SIZE = new Dimension( mapSize, mapSize );
	

	// Member variables
	private JGraphModelAdapter<OSMNode,DefaultEdge> m_jgAdapter;
	double min_x,min_y,max_x,max_y;
	AbstractGraph<OSMNode,DefaultEdge> map;

	// JApplet initialization
	public void init()
	{
		try{
			OSM osm = OSMParser.parse("../data/map4.osm");
			this.map = new SimpleGraph<>(DefaultEdge.class);

			/* *******************************************************
			   //Uncomment this part if you want to see some statistics
			   //*************************************************************************
				Map<Strisng,OSMNode> id2Node = new HashMap<String,OSMNode>();
				for debug of nodes
				Set <String> distinct_tagsN= new HashSet<String>();
				Set <String> tag_keysN= new HashSet<String>();
				int countN =0;
				for (OSMNode n:osm.getNodes())
				{
					tag_keysN.addAll(n.tags.keySet());
					if (n.tags.containsKey("highway")){
						distinct_tagsN.add(n.tags.get("highway"));
						countN = countN +1 ;
					}
				}
				//for debug of ways
				Set <String> distinct_tagsW= new HashSet<String>();
				Set <String> tag_keysW= new HashSet<String>();
				int countW =0;
				for (Way n:osm.getWays())
				{
					tag_keysW.addAll(n.tags.keySet());
					if (n.tags.containsKey("highway")){
						distinct_tagsW.add(n.tags.get("highway"));
						countW = countW +1 ;
					}
				}

				System.out.println(distinct_tagsN);
				System.out.println(distinct_tagsW);
				System.out.println("#Nodes: " + osm.getNodes().size() +", #Highway Nodes: "+ countN);
			 */

			// Construct the map
			NodeSelection ns = new NodeSelection();
			for(Way cur_way:osm.getWays())
			{
				if (ns.validateWay(cur_way))
				{   
					List<OSMNode> cur_nodes = cur_way.nodes;
					for(int i=1;i<cur_nodes.size();i++)
					{
						//System.out.println(i);
						if(!map.containsVertex(cur_nodes.get(i-1)))
						{
							map.addVertex(cur_nodes.get(i-1));
						}   
						if(!map.containsVertex(cur_nodes.get(i)))
						{
							map.addVertex(cur_nodes.get(i));
						}
						map.addEdge(cur_nodes.get(i-1),cur_nodes.get(i));
					}
				}
			}
			computeXY();

			// Re-scaling the points
			for(OSMNode n:map.vertexSet()){
				rescaleXY(n);
			}

		}
		catch(Exception e)
		{
			System.out.println("There was a problem loading the osm data!");
		}

		// Convert JGrapht graph to JGraph for visualization
		m_jgAdapter = new JGraphModelAdapter<OSMNode,DefaultEdge>( map );
		JGraph jgraph = new JGraph( m_jgAdapter );


		// Removing the edge labels
		GraphLayoutCache cache = jgraph.getGraphLayoutCache();
		CellView[] cells = cache.getCellViews();
		for(CellView cell:cells)
		{
			if(cell instanceof EdgeView)
			{
				EdgeView ev = (EdgeView) cell;
				org.jgraph.graph.DefaultEdge eval = (org.jgraph.graph.DefaultEdge) ev.getCell();
				eval.setUserObject("");
			}
		}
		cache.reload();
		jgraph.repaint();

		adjustDisplaySettings( jgraph );
		getContentPane(  ).add( jgraph );
		resize( DEFAULT_SIZE );

		for (OSMNode vv:map.vertexSet())
		{
			positionVertexAt( vv, (int)vv.x, (int)vv.y );
		}
	}



	private void adjustDisplaySettings( JGraph jg ) {
		jg.setPreferredSize( DEFAULT_SIZE );

		Color  c        = DEFAULT_BG_COLOR;
		String colorStr = null;

		try {
			colorStr = getParameter( "bgcolor" );
		}
		catch( Exception e ) {}

		if( colorStr != null ) {
			c = Color.decode( colorStr );
		}

		jg.setBackground( c );
	}


	private void positionVertexAt( Object vertex, int x, int y ) {
		DefaultGraphCell cell = m_jgAdapter.getVertexCell( vertex );
		
		Map           attr = cell.getAttributes(  );

		GraphConstants.setBounds( attr, new Rectangle(x, y, 1, 1));

		Map cellAttr = new HashMap(  );
		cellAttr.put( cell, attr );
		m_jgAdapter.edit( cellAttr, null, null, null );
	}




	private void computeXY()
	{
		min_x = Double.MAX_VALUE;
		min_y = Double.MAX_VALUE;
		max_x = 0;
		max_y = 0;
		for(OSMNode n:map.vertexSet())
		{
			double cur_lon = Double.parseDouble(n.lon);
			double cur_lat = Double.parseDouble(n.lat);
			VisualNode visualNode = mercator_map(cur_lon, cur_lat);
			n.x =  visualNode.x;
			n.y = visualNode.y;
			if(visualNode.x<min_x)
				min_x = visualNode.x;
			if(visualNode.x>max_x)
				max_x = visualNode.x;
			if(visualNode.y<min_y)
				min_y = visualNode.y;
			if(visualNode.y>max_y)
				max_y = visualNode.y;
		}
	}

	private void rescaleXY(OSMNode n)
	{
		n.x = (n.x - this.min_x) / (this.max_x - this.min_x) * mapSize;
		n.y = (n.y - this.min_y) / (this.max_y - this.min_y) * mapSize;
	}
	protected VisualNode mercator_map(double longitude,double latitude)
	{
		//double longitude = Double.parseDouble(lon);
		//double latitude = Double.parseDouble(lat);

		// Computation based on a map size (later we will re-scale it)
		int mapSize    = 500;

		// get x value
		double x = (longitude+180)*(mapSize/360);

		// convert from degrees to radians
		double latRad = latitude*Math.PI/180;

		// get y value
		double mercN = Math.log(Math.tan((Math.PI/4)+(latRad/2)));
		double y  = (mapSize/2)-(mapSize*mercN/(2*Math.PI));
		return new VisualNode(x,y);
	}

}
