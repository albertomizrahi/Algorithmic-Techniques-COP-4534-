/**
 * Finds the topological order (if it exists) or the cycle in a graph
 * @author Alberto Mizrahi
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

/**
 * Class that the adjacent vertices and indegree of a vertex in the map
 */
class VertexInfo
{
    int indegree;
    ArrayList<String> adjacents;
    
    public VertexInfo( int indegree, ArrayList<String> adjacents )
    {
        this.indegree = indegree;
        this.adjacents = adjacents;
    }
    
    public String toString()
    {
        return  "{indegree: " + indegree + ", adjacents" + adjacents.toString() + "}";
    }
}

public class TopOrder 
{

    public static void main( String [ ] args )
    {
        for ( String filename : args )
        {
            try
            {
                processFile( filename );
            } 
            catch ( FileNotFoundException ex )
            {
                System.err.println( "The file " + filename + " was not found. "
                        + "The program will continue processing the rest of the files." );
            }
        }
    }
    
    /**
     * Process each file containing all the edges in the graph
     * @param filename Name of the file
     * @throws FileNotFoundException 
     */
    private static void processFile( String filename ) throws FileNotFoundException
    {
        // Map containing all the vertices and their info
        Map<String,VertexInfo> vertices = new HashMap<>();
        
        Scanner scanner = new Scanner( new File( filename ) );
        
        System.out.print( filename + ": " );
        
        int lineNum = 1;
        while ( scanner.hasNextLine() )
        {
            String line = scanner.nextLine();
            // Split the line by its whitespase using Regex
            String [ ] edge = line.trim().split( "\\s+" );
            
            /*
             * If the edge does not have 2 vertices, then the line is wrongly formatted.
             * In that case, the line is skipped and the processing continues.
            */
            if ( edge.length != 2 )
            {
                System.err.println( "Line #" + lineNum + ": '" + line + "' in file " + filename
                        + " is incorrectly formatted. " + "The program will continue to "
                        + "process the rest of the file." );
                continue;
            }
            
            VertexInfo infoV = vertices.get( edge[ 0 ] );
            if ( infoV == null )
            {
                infoV = new VertexInfo( 0, new ArrayList<String>() );
                vertices.put( edge[ 0 ], infoV );
            }
            // Add the 2nd vertex of the edge to the adjacents list of the 1st's
            infoV.adjacents.add( edge[ 1 ] );
            
            
            VertexInfo infoW = vertices.get( edge[ 1 ] );
            if ( infoW == null )
            {
                infoW = new VertexInfo( 0, new ArrayList<String>() );
                vertices.put( edge[ 1 ], infoW );
            }
            // Increase the indegree of the 2nd vertex
            infoW.indegree++;
            
            ++lineNum;
        }
        
        /*
         * Only the time to find the top order or cycle is counted, not the time
         * to read the file contents and store them.
        */
        long start = System.currentTimeMillis();
        
        // Find a top order (if it exists) or a cycle in the graph
        List<String> topOrder = findTopOrder( vertices );
        
        long end = System.currentTimeMillis();

        
        // If the first and last vertex of the path are the same then there is a cycle
        if ( topOrder.get( 0 ).equals( topOrder.get( topOrder.size() - 1 ) ) )
            System.out.print( "graph has a cycle: " + formatPath( topOrder ) );
        // Else print the top order
        else
            System.out.print( formatPath( topOrder ) );
        
        System.out.println( " (" + ( end - start ) + " ms. to finish)" );
    }
    
    /**
     * Find the topological order (if it exists) or a cycle in the graph
     * @param vertices Vertices of the graph with their info
     * @return List containing a top order or a cycle
     */
    private static List<String> findTopOrder( Map<String,VertexInfo> vertices )
    {
        // Store vertices with indegree 0
        LinkedList<String> queue = new LinkedList<>();
        // Store the top order
        ArrayList<String> topOrder = new ArrayList<>();
        
        // For every vertex, if its indegree is 0 add it to the queue
        for ( Map.Entry<String,VertexInfo> entry : vertices.entrySet() )
            if ( entry.getValue().indegree == 0 )
                queue.addLast( entry.getKey() );
        
        
        while ( !queue.isEmpty() )
        {
            String v = queue.removeFirst();
            topOrder.add( v );
            // For all the neighbors of v, decrease their indegree
            for ( String w : vertices.get( v ).adjacents )
            {
                VertexInfo infoW = vertices.get( w );
                infoW.indegree--;
                // If w's indegree becomes 0, add it to the queue
                if ( infoW.indegree == 0 )
                    queue.addLast( w );
            }
        }
        
        // Return the top order (if it exists) or the cycle otherwise
        return ( topOrder.size() == vertices.size() ) ? topOrder : detectCycle( vertices );
    }
    
    /**
     * @param vertices Map of the vertices with their info
     * @return Cycle in the graph
     */
    private static List<String> detectCycle( Map<String,VertexInfo> vertices )
    {
        // Store the vertices that DON'T have a indegree of 0
        List<String> badVertices = new ArrayList<>();
        
        for ( Map.Entry<String,VertexInfo> entry : vertices.entrySet() )
            if ( entry.getValue().indegree > 0 )
                badVertices.add( entry.getKey() );
        
        // Incomings table containing the vertices that are going to each bad vertex
        Map<String,List<String>> incomings = buildIncomingsTable( vertices, badVertices );
        // Store used vertices in the path
        LinkedHashSet<String> usedVertices = new LinkedHashSet<>();
        // Store the path containing the cycle
        Stack<String> path = new Stack<>();
        
        
        // Start finding the cycle from the first bad vertex
        String v = badVertices.get( 0 );
        
        path.add( v );
        usedVertices.add( v );
        
        // Get the vertices coming to v
        List<String> incomingsToVertex = incomings.get( v );
        
        // Stores the last vertex in the cycle
        String lastVertex = null;
        boolean foundCycle = false;
        while( ! foundCycle )
        {
            // Get all the vertices coming to the vertex
            for ( String incoming : incomingsToVertex )
                // If any of the incoming vertices is already in our list, then a cycle was found
                if ( usedVertices.contains( incoming ) )
                {
                    lastVertex = incoming;
                    foundCycle = true;
                    break;
                }
            
            if ( !foundCycle )
            {
                // Get the first vertex incoming to v
                String w = incomingsToVertex.get( 0 );
                // Add w to the used vertices list and path
                usedVertices.add( w );
                path.add( w );
                // Get the vertices incoming to w
                incomingsToVertex = incomings.get( w );
            }
        }

        // Store the actual cycle
        List<String> cycle = new ArrayList<>();
        cycle.add( lastVertex );
        
        /*
         * In this loop, the vertices in the path containing the cycle are popped
         * until the vertex popped is equal to the repeating vertex in the path
        */
        while ( !path.isEmpty() )
        {
            String vertex = path.pop();
            cycle.add( vertex );
            
            if ( vertex.equals( lastVertex ) )
                break;
        }
                
        return cycle;
    }
    
    /** 
     * Find all the vertices that are incoming to each vertex in the list.
     * e.g if v -> w is a edge, then v is incoming to w
     * @param vertices The entire list of vertices
     * @param badVertices The vertices whose incoming table will be fuilt for
     * @return 
     */
    private static Map<String,List<String>> buildIncomingsTable( Map<String,VertexInfo> vertices,
                                                                   List<String> badVertices )
    {
        Map<String,List<String>> incomings = new HashMap<>();
        
        for ( String v : badVertices )
        {
            // For every vertex coming into v
            for ( String w : vertices.get( v ).adjacents )
            {
                List<String> incomingsToW = incomings.get( w );
                if ( incomingsToW == null )
                {
                    incomingsToW = new ArrayList<>();
                    incomings.put( w, incomingsToW );
                }
                // Add v to w's icoming
                incomingsToW.add( v );
            }
        }
        
        return incomings;
    }
    
    /**
     * @param vertices Sequence of vertices
     * @return Return a formatted String of the path given by the sequence of the vertices
     */
    private static String formatPath( List<String> vertices )
    {
        StringBuilder sb = new StringBuilder( "" );
        
        for ( String v : vertices )
            sb.append( v ).append( " -> " );
        
        if ( sb.length() != 0 )
            sb.setLength( sb.length() - 4 );
        
        return new String( sb );
    }
}
