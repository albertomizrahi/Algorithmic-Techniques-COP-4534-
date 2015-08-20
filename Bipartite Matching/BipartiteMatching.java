/**
 * Finds a bipartite matching that minimizes the value K, where K is the 
 * ranking of the "worst" pair.
 * @author Alberto Mizrahi
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

class Graph
{
    // These vertices are used to be able to calculate the max flow in the graph
    private final Vertex SOURCE, SINK;
    // Used when finding the shortest path in the graph to represent infinity
    private final int INFINITY = Integer.MAX_VALUE / 3;
    
    // Store the vertices in the first partition of the graph user their name as their keys
    private Map<String,Vertex> partition1;
    // Store the vertices in the second partition of the graph user their name as their keys
    private Map<String,Vertex> partition2;
    // Adjacency list for all vertices in the graph
    private Map<Vertex,Set<Vertex>> adjList;
    // Stores each vertex's rankings of the other partition
    private Map<Vertex,List<Vertex>> rankings;
    // The number of vertices in each partition
    private int N;
    
    public Graph( String filename ) throws FileNotFoundException
    {
        // Initialize the maps
        partition1 = new TreeMap<>(); 
        partition2 = new TreeMap<>();
        adjList = new HashMap<>();
        rankings = new HashMap<>();
        
        Scanner scanner = new Scanner( new File( filename ) );
        
        Map<String,Vertex> partition = partition1;        
        Map<String,Vertex> otherPartition = partition2;

        N = -1;
        int lineNum = 1;
        int numPerPartition = 0;
        boolean finishedFirstPartition = false;
        // Read the file containing the rankings
        while ( scanner.hasNextLine() )
        {
            String line = scanner.nextLine().trim();
            
            // If the line is empty, the first partition was finished reading
            if ( line.isEmpty() && !finishedFirstPartition )
            {
                partition = partition2;
                otherPartition = partition1;
                
                if ( numPerPartition != N )
                    throw new IllegalArgumentException( "File '" + filename + "': There are "
                            + N + " items in partition 1 but each one provided "
                            + numPerPartition + " rankings.");
                
                finishedFirstPartition = true;
                numPerPartition = 0;
                
                continue;    
            }
            
            // Prevents extra blanks lines after partition 2 fromg being processed
            if ( line.isEmpty() )
                continue;
            
            // Separate the vertex from its rankings
            String [ ] tokens = line.split( ":" );
            // Separate each of the vertices ranked
            String [ ] choices = tokens[ 1 ].split( "," );
            
            if ( N == -1)
                N  = choices.length;
            else
            {
                if ( choices.length != N )
                    throw new IllegalArgumentException( "File '" + filename + "': In line " + lineNum 
                                        + ", this item should specify " + N + " rankings.");
            }
           
            
            String vertexName = tokens[ 0 ];
            // Store the vertex in the correct partition
            Vertex v = partition.get( vertexName );
            if ( v == null )
            {
                v = new Vertex( vertexName );
                partition.put( vertexName, v );
            }
            
            // Store the vertex in the adjacency list with no adjacents by default
            adjList.put( v, new HashSet<Graph.Vertex>() );

            // Store vertex v's rankings in order
            List<Vertex> ranksList = new ArrayList<>();            
            for ( String choice : choices )
            {                
                // Get the choice vertex from the partition which v is not in
                Vertex w = otherPartition.get( choice );
                if ( w == null )
                {
                    w = new Vertex( choice );
                    otherPartition.put( choice, w );
                }
                
                ranksList.add( w );                
            }
            
            // Put vertex v's ranking in the map
            rankings.put( v, ranksList );
            
            ++lineNum;
            ++numPerPartition;
        }
        
        if ( numPerPartition != N )
                    throw new IllegalArgumentException( "File '" + filename + "': Each item "
                            + " in partition 1 provided " + N + " rankings, but"
                            + " there are only " + numPerPartition + " items in partition 2.");
        
        // Initialize the SOURCE and SINK vertices
        SOURCE = new Vertex( "source" );
        SINK = new Vertex( "sink" );
        
        // Make the SOURCE vertex adjacent to all the vertices in the first partition
        Set<Vertex> adjSource = new HashSet<>();        
        for ( Map.Entry<String,Vertex> entry : partition1.entrySet() )
            adjSource.add( entry.getValue() );
        adjList.put( SOURCE, adjSource );
        
        // The SINK vertex starts with having no adjacent vertices
        adjList.put( SINK, new HashSet<Graph.Vertex>() );

        // Make all the vertices in the second partition adjacent to SINK
        for ( Map.Entry<String,Vertex> entry : partition2.entrySet() )
            adjList.get( entry.getValue() ).add( SINK );
    }
    
    /**
     * Creates a graph having the same vertices as the one passed in the
     * parameter.
     * @param other 
     */
    public Graph( Graph other )
    {
        partition1 = new HashMap<>();
        partition2 = new HashMap<>();
        adjList = new HashMap<>();
        
        SOURCE = new Vertex( "source" );
        SINK = new Vertex( "sink" );
        
        Set<Graph.Vertex> adjSource = new HashSet<Graph.Vertex>();
        // Copy each vertex in the first partition of the other graph into this one
        for ( Map.Entry<String,Vertex> entry : other.partition1.entrySet() )
        {
            Vertex v = new Vertex( entry.getKey() );
            partition1.put( entry.getKey(), v );
            adjSource.add( v );
            adjList.put( v, new HashSet<Graph.Vertex>() );
        }
        
        // Copy each vertex in the second partition of the other graph into this one
        for ( Map.Entry<String,Vertex> entry : other.partition2.entrySet() )
        {
            Vertex v = new Vertex( entry.getKey() );
            partition2.put( entry.getKey(), v );
            Set<Graph.Vertex> thisSet = new HashSet<Graph.Vertex>();
            // Make each vertex in the 2nd partition adjacent to SINk
            thisSet.add( SINK );
            adjList.put( v, thisSet );
        }
       
        // Add the SOURCE and SINK to the adjacency list
        adjList.put( SOURCE, adjSource );
        adjList.put( SINK, new HashSet<Graph.Vertex>() );
        
        for ( Map.Entry<Vertex,Set<Vertex>> entry : other.adjList.entrySet() )
            for ( Vertex x : entry.getValue() )
                adjList.get( entry.getKey() ).add( getVertex( x.name ) );
    }
    
    /**
     * @param name Name of the vertex
     * @return The vertex in this graph with that name
     */
    private Vertex getVertex( String name )
    {
        Vertex v = null;
        if ( name.equals( "source" ) )
            v = SOURCE;
        else if ( name.equals( "sink" ) )
            v = SINK;
        else
        {
            v = partition1.get( name );
            if ( v == null ) 
                v = partition2.get( name );
        }
        
        return v;
    }
    
    /**
     * Adds an edge to the graph
     * @param v1Name Name of the first vertex
     * @param v2Name Name of the second vertex
     */
    public void addEdge( String v1Name, String v2Name )
    { adjList.get( getVertex( v1Name ) ).add( getVertex( v2Name ) ); }
    
    /**
     * Removes an edge to the graph
     * @param v1Name name of the first vertex
     * @param v2Name name of the second vertex
     */
    public void removeEdge( String v1Name, String v2Name )
    { adjList.get( getVertex( v1Name ) ).remove( getVertex( v2Name ) ); }
    
    /**
     * Find the lowest rank K such that a bipartite matching was found and print
     * all the matchings. Starting at K = 1, an edge is added to the residual graph 
     * between vertices in the two partitions if V1 ranked V2 as its K choice, 
     * while V2 ranked V1 as its <= K choice,  where V1 is a vertex in the first partition
     * and V2 is a vertex in the second one. Then carry a max network flow calculation
     * and if the net flow in the flow graph is equal to N, the lowest 
     * rank K for a bipartite match was found.
     */
    public void findMatching()
    {
        int K = 0;
        int netFlow = 0;
        // The residual graph is created
        Graph residual = new Graph( this );
        // Stores the current ranks seen so far for each vertex
        Map<Vertex,List<Vertex>> currentRanks = new HashMap<>();
        
        while ( netFlow != N )
        {
            K++;
            netFlow = calcNetworkFlow( residual, K, netFlow, currentRanks );
        }
        
        printMatching( K );
        
    }
    
    /**
     * Print the matching for all vertices in the graph
     * @param K 
     */
    private void printMatching( int K )
    {
        System.out.println( "Everybody was matched with their top " + K + " preferences.");
        
        // Print the first partition matchings
        for ( Map.Entry<String,Vertex> entry : partition1.entrySet() )
        {
            Vertex v1 = entry.getValue();
            Vertex v2 = v1.getMatchedVertex();
            // Get vertex v1's ranking of v2
            int ranking = rankings.get( v1 ).indexOf( v2 ) + 1;
            System.out.println( v1 + ": matched to " + v2 + " (rank " + ranking + ")" );
        }
        
        // Print the second partition matchings
        for ( Map.Entry<String,Vertex> entry : partition2.entrySet() )
        {
            Vertex v1 = entry.getValue();
            Vertex v2 = v1.getMatchedVertex();
            // Get vertex v2's ranking of v1
            int ranking = rankings.get( v1 ).indexOf( v2 ) + 1;
            System.out.println( v1 + ": matched to " + v2 + " (rank " + ranking + ")" );
        }
    }
    
    /**
     * Given K, for any two vertices V1 and V2 in opposite partitions, this
     * methods adds an edge to the residual graph from V1 and V2 if:
     *  1) V1 ranked V2 as his Kth choice and
     *  2) V2 ranked V1 as its <= Kth choice
     * @param residual The graph were the adges will be added
     * @param K 
     * @param currentRanks The current ranks that have been processed for each vertex already
     */
    private void addBipartiteEdges( Graph residual, int K, Map<Vertex,List<Vertex>> currentRanks )
    {
        // Get the Kth choice of each vertex and it to the current rankins seen so far
        for ( Map.Entry<Vertex,List<Vertex>> entry : rankings.entrySet() )
        {
            Vertex currentVertex = entry.getKey();
            Vertex nextChoice = entry.getValue().get( K - 1 );
            
            // If K == 1, the first choice of each vertex is being processed which means the map is empty
            if ( K == 1 )
                currentRanks.put( currentVertex, new ArrayList<Graph.Vertex>() );
            
            currentRanks.get( currentVertex ).add( nextChoice );
        }
        
        
        for ( Map.Entry<Vertex,List<Vertex>> entry : currentRanks.entrySet() )
        {
            Vertex currentVertex = entry.getKey();
            Vertex nextChoice = entry.getValue().get( K - 1 );
            
            // If the current vertex's choice also ranked the current vertex <= K, add the edge
            if ( currentRanks.get( nextChoice ).contains( currentVertex ) )
                if ( residual.partition1.containsKey( currentVertex.name ) )
                    residual.adjList.get( currentVertex ).add( residual.partition2.get( nextChoice.name ) );
                else                    
                    residual.adjList.get( nextChoice ).add( residual.partition2.get( currentVertex.name ) );
        }
    }
    
    /**
     * Calculates the max flow in the flow graph. First, the appropriate edges are 
     * inserted in the residual graph Gr. Then, a shortest path in Gr is repeatedly found
     * and transferred into the flow graph Gf until there are not more such paths or
     * the total net flow in Gf is equal to N.
     * @param residual residual Graph Gr
     * @param K Rank K being considered
     * @param netFlow Total net flow in Gf found so far
     * @param currentRanks The ran
     * @return 
     */
    private int calcNetworkFlow( Graph residual, int K, int netFlow, Map<Vertex,List<Vertex>> currentRanks )
    {   
        // Update Gr so it has the appropriate edges for each respective K
        addBipartiteEdges( residual, K, currentRanks );
        
        // Find a path in the Gr
        residual.findPath( residual.SOURCE );
        // Get the shortest path in Gr
        List<Vertex> path = residual.getPathToSink();
                
        while ( !path.isEmpty() && netFlow < N )
        {
            // Transfer the path from Gr to Gf
            for ( int i = 0; i < path.size() - 1; ++i )
            {
                Vertex v = path.get( i );
                Vertex w = path.get( i + 1 );
                
                /* 
                 * If the path v->w is being transferred to Gf but Gf has the path
                 * w->v, then that w->v is simply removed from Gf.
                 * Otherwise, v->w is added to Gf
                */
                
                if ( adjList.get( w ).contains( v ) )
                    removeEdge( w.name, v.name );
                else
                {
                    addEdge( v.name, w.name );
                    
                    /*
                     * If neither v or w are the SOURCE and the SINK, then v and 
                     * w are matched by the added the edge, so that is kept tracked
                    */
                    if ( !v.name.equals( "source" ) && !v.name.equals( "sink" ) && 
                            !w.name.equals( "source" ) && !w.name.equals( "sink" ) )
                    {
                        getVertex( v.name ).matchedTo( getVertex( w.name ) );
                        getVertex( w.name ).matchedTo( getVertex( v.name ) );
                    }
                }

                // Remove the edge from Gr
                residual.removeEdge( v.name, w.name );
                // Add the reversed edge to Gr
                residual.adjList.get( w ).add( v );
            }
            
            // Find the next shortest path in Gr
            residual.findPath( residual.SOURCE);
            path = residual.getPathToSink();

            // Every path transfered from Gr to Gf increases the net flow in Gf by 1
            ++netFlow;
        }

        return netFlow;
    }
    
    /**
     * @return A list containing all the vertices in the graph.
     */
    private List<Vertex> getAllVertices()
    {
        List<Vertex> vertices = new ArrayList<>();
        
        vertices.add( SOURCE );
        vertices.add( SINK );
        
        for ( Map.Entry<String,Vertex> entry : partition1.entrySet() )
            vertices.add( entry.getValue() );
        
        for ( Map.Entry<String,Vertex> entry : partition2.entrySet() )
            vertices.add( entry.getValue() );
        
        return vertices;
    }
    
    /**
     * Uses BFS to find the shortest path from the start vertex to all other
     * vertices in the graph.
     * @param start 
     */
    private void findPath( Vertex start )
    {
        // Reset all the path-finding related variables of the vertices
        for ( Vertex v : getAllVertices() )
        {
            v.setDistance( INFINITY );
            v.setProcessed( false );
            v.setPrevious( null );
        }
        
        start.setDistance( 0 );
        
        Queue<Vertex> queue = new LinkedList<>();
        queue.add( start );
        
        while ( !queue.isEmpty() )
        {
            Vertex v = queue.remove();
            v.setProcessed( true );
            
            Set<Vertex> adjacents = v.getAdjacents();
            
            // If v doesn't have any adjacents, no further processing is needed
            if ( adjacents == null )
                continue;
            
            // For every adjacent vertex, increase its distance to dv + 1 if it hasn't been processed
            for ( Vertex w : v.getAdjacents() )
                if ( !w.hasBeenProcessed() )
                {
                    w.setDistance( v.getDistance() + 1 );
                    w.setPrevious( v );
                    queue.add( w );
                }
        }
    }
    
    /**
     * @return List of vertices representing the shortest path from the start vertex
     * (as indicated by the findPath() parameter) to the SINK.
     */
    private ArrayList<Vertex> getPathToSink()
    {
        ArrayList<Vertex> path = new ArrayList<>();
        Stack<Vertex> stack = new Stack<>();
        
        // Push all the vertices from the SINK to the start vertex into a stack
        Vertex v = SINK;
        while ( v != null )
        {
            stack.add( v );
            v = v.getPrevious();
        }
        
        /*
         * If the size of the stack is 1, that means the only vertex in it is
         * the SINK so there is no path.
        */
        if ( stack.size() == 1 )
            stack.pop();
        
        // Pop all the elements in the stack to get the vertices from the start to SINK
        while ( !stack.isEmpty() )
            path.add( stack.pop() );
        
        return path;
    }
    
    /**
     * Models a vertex in a graph
     */
    private class Vertex
    {
        String name;
        int distance;
        boolean processed;
        Vertex prev;
        Vertex matchedTo;
        
        public Vertex( String name )
        { 
            this.name = name; 
        }
        
        public Set<Vertex> getAdjacents()
        { return adjList.get( this ); }
        
        public int getDistance()
        { return distance; }
        
        public void setDistance( int newDistance )
        { distance = newDistance; }
        
        public boolean hasBeenProcessed()
        { return processed; }
        
        public void setProcessed( boolean flag )
        { processed = flag; }
        
        public Vertex getPrevious()
        { return prev; }
        
        public void setPrevious( Vertex newPrev )
        { prev = newPrev; }
        
        public void matchedTo( Vertex v )
        { matchedTo = v; }
        
        public Vertex getMatchedVertex()
        { return matchedTo; }

        @Override
        public boolean equals( Object obj )
        {
            if ( obj == null )
                return false;
            if ( ! ( obj instanceof Vertex ) )
                return false;
            
            Vertex v = ( Vertex ) obj;
            return this.name.equals( v.name );
        }

        @Override
        public int hashCode()
        { return name.hashCode(); }
        
        @Override
        public String toString()
        { return name/*"{" + name + "," + distance + "}"*/; }

    }
}

public class BipartiteMatching
{
    private static void processFile( String filename )
    {
        try
        {
            Graph graph = new Graph( filename );
            
            System.out.println( "File: " + filename );
            long start = System.currentTimeMillis();
            graph.findMatching();
            long end = System.currentTimeMillis();
            
            System.out.println( "Elapsed time: " + ( end - start ) + " ms." );
            System.out.println( );
        } 
        catch ( FileNotFoundException ex )
        {
            System.err.println( "The file " + filename + " could not be found." );
            System.err.println( );
        }
        catch ( IllegalArgumentException ex )
        {
            System.err.println( ex.getMessage() );
            System.err.println( "File '" + filename + "' will not be processed." );
            System.err.println( );
        }
        
    }
    
    public static void main( String[] args )
    {
        System.out.println( "The extra credit was done for this assignment." );
        System.out.println( );
        for ( String filename : args )
        {
            processFile( filename );
        }
    }
    
}
