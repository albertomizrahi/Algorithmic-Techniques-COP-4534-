/*
 * Find the shortest path in a maze taking into cosideration wall knocking (and its penalty)
 * @author Alberto Mizrahi
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Stack;

/**
 * Class that models an m x n grid of Squares with walls and is able to find
 * the shortest path from a square to end of the maze.
*/
class Maze
{
    /**
     * Interface that a models a cell in the maze's grid.
    */
    public interface Square
    {
        /**
         * @return The neighbors (if they exist) of the Square in all its 4 directions
        */
        List<Square> getAdjacents();
        int getX();
        int getY();
        void setDistance( int newDist );
        int getDistance();
        void setPrevious( Square newPrev );
        Square getPrevious();
        /**
         * Set the walls that this Square represented as a String with
         * the coordinates of the wall (i.e. "NSEW")
         * @param walls The walls that this Square has
         */
        void setWalls( String walls );
        String getWalls();
        /**
         * Set whether this Square has already been processed while finding
         * the shortest path.
         * @param flag True or false depending on above
         */
        void setAlreadyProcessed( boolean flag );
        boolean isAlreadyProcessed();
        /**
         * The method should take into consideration that there may not be
         * redundancy of the walls in the two Squares. In other words, this
         * Square may have a East wall but the the Square to the right may not have
         * a west wall.
         * @param other The Square with which a wall may be between
         * @return Boolean indicating if there is a wall between the two Squares
         */
        boolean hasWall( Square other );
        /**
         * @param other
         * @return Return a string with "E","W","N","S" indicating the direction from
         * this Square to the 'other' Square
         */
        String directionTo( Square other );
    }
    
    /**
     * Implements the actual Square interface. For general comments on the methods,
     * please refer to the interface Square.
     */
    private class MySquare implements Square
    {
        int x;
        int y;
        String walls;
        int distance;
        Square prev;
        boolean alreadyProcessed;
        
        public MySquare( int x, int y, String walls )
        {
            this.x = x;
            this.y = y;
            this.walls = walls;
        }
        
        @Override
        public List<Square> getAdjacents()
        {
            List<Square> adjacents = new ArrayList<>();
            
            // Get the Square to the left
            if ( y - 1 >= 0 )
                adjacents.add( grid[ x ][ y - 1 ] );
            // Get the Square to the right
            if ( y + 1 < numColumns )
                adjacents.add(  grid[ x ][ y + 1 ] );
            // Get the Square above
            if ( x - 1 >= 0 )
                adjacents.add( grid[ x - 1 ][ y ] );
            // Get the Square below
            if ( x + 1 < numRows )
                adjacents.add(  grid[ x + 1 ][ y ] );
            
            return adjacents;
        }

        @Override
        public void setDistance( int newDist )
        { distance = newDist; }

        @Override
        public int getDistance()
        { return distance; }

        @Override
        public void setPrevious( Square newPrev )
        { prev = newPrev; }

        @Override
        public Square getPrevious()
        { return prev; }

        @Override
        public void setWalls( String newWalls )
        { walls = newWalls; }

        @Override
        public String getWalls()
        { return walls; }

        @Override
        public void setAlreadyProcessed( boolean flag )
        { alreadyProcessed = flag; }

        @Override
        public boolean isAlreadyProcessed()
        { return alreadyProcessed; }

        @Override
        public boolean hasWall( Square other )
        {
            // Check if there is a wall between this Square and the 'other' Square
            boolean thisToOther = walls.contains( directionTo( other ) );
            // Check if there is a wall between the 'other' Square and this Square
            boolean otherToThis = other.getWalls().contains( other.directionTo( this) );
            return thisToOther || otherToThis;
        }
        
        @Override
        public String directionTo( Square other )
        {
            int deltaX = x - other.getX();
            int deltaY = y - other.getY();
            
            // The 'other' Square is to the north of this Square
            if ( deltaX == 1 )
                return "N";
            // The 'other' Square is to the south of this Square
            else if ( deltaX == -1 )
                return "S";
            
            // The 'other' Square is to the west of this Square
            if ( deltaY == 1 )
                return "W";
            // The 'other' Square is to the east of this Square
            else if ( deltaY == -1 )
                return "E";
            
            return "";
        }
        
        @Override
        public String toString()
        { return "[" + x + ", " + y + "]"; }

        @Override
        public int getX()
        { return x; }

        @Override
        public int getY()
        { return y; }
    }
    
    /*
     * Represent the value of infinity for the distances in the path. Divided by 3 
     * to prevent accidental overflow.
    */
    public final int INFINITY = Integer.MAX_VALUE / 3;
    
    // Top left and bottom right Squares in grid
    public final Square TOP_LEFT;
    public final Square BOTTOM_RIGHT;
    
    private int numRows;
    private int numColumns;
    private Square [ ][ ] grid;
    
    public Maze( String filename )
    {
        boolean error = false;
        
        try
        {
            processFile( filename );
        } 
        catch ( FileNotFoundException ex )
        {
            System.out.println( "The file '" + filename + "' was not found." 
                                + " The file will be skipped." );
            error = true;
        }
        catch( IllegalStateException ex )
        {
            System.out.println( "The maze from '" + filename + "' was not created."
                    + " The file will be skipped." );
            error = true;
        }
        
        
        TOP_LEFT = ( error ) ? null : grid[ 0 ][ 0 ];
        BOTTOM_RIGHT = ( error ) ? null : grid[ numRows - 1 ][ numColumns - 1 ];
        
    }
    
    /**
     * Read the file containing the maze date
     * @param filename
     * @throws FileNotFoundException 
     */
    private void processFile( String filename ) throws FileNotFoundException
    {
        Scanner scanner = new Scanner( new File( filename ) );
        
        int lineNum = 0;
        while ( scanner.hasNextLine() )
        {
            // Separate the line being read by whitespace
            String [ ] tokens = scanner.nextLine().trim().split( "\\s+" );
            
            // If lineNum==0, then the first line, containing, the # of rows and columns is being read
            if ( lineNum == 0 )
            {
                // If the line is missing the rows or columns, do not process the file
                if ( tokens.length < 2 )
                {
                    System.out.println( "In " + filename + ": The first row, which "
                            + "contains the number of rows and columns, is missing parameters." );
                    throw new IllegalStateException();
                }
                
                try 
                {
                    // Attempt to parse the integers of the rows and columns
                    numRows = Integer.parseInt( tokens[ 0 ] );
                    numColumns = Integer.parseInt( tokens[ 1 ] );
                }
                catch( NumberFormatException ex )
                {
                    System.out.println( "In " + filename + ": The first row, which contains the number of "
                            + "rows and columns, is incorrectly formatted." );
                    throw new IllegalStateException();
                }
                
                initGrid();
                
                lineNum++;
                continue;
            }
            
            // Else, the line being read just has info on a grid cell.
            
            // Check that at least the rows and columns are given
            if ( tokens.length < 2 )
            {
                System.out.println( "In " + filename + ": Line #" + lineNum +
                                    " is missing data. It will be skipped." );
                continue;
            }
            
            int xPos = parseInt( tokens[ 0 ] );
            int yPos = parseInt( tokens[ 1 ] );
            // The walls of the Square may or may not be present
            String walls = ( tokens.length == 3 ) ? tokens[ 2 ] : "";
                        
            /* Ensure that the grid cell position is between the sze of the grid
             * and the that the parsing went well.
            */
            if ( xPos > -1 && xPos < numRows && yPos > -1 && yPos < numColumns )
                grid[ xPos ][ yPos ].setWalls( walls );
            else
                System.out.println( "In " + filename + ": Line #" + lineNum +
                                    " is incorrectly formatted or the (x,y) position is "
                                    + "outside the grid. It will be skipped." );
     
            lineNum++;
        }        
    }
    
    /**
     * Initiate the grid of Squares once the number of rows and columns are known.
     */
    private void initGrid()
    {
        grid = new Square [ numRows ][ numColumns ];
        
        for ( int i = 0; i < numRows; ++i )
            for ( int j = 0; j < numColumns; ++j )
                grid[ i ][ j ] = new MySquare( i, j, "" );
    }
    
    /**
     * Attempt to parse an Integer from a String
     * @param s String containing Integer
     * @return The number parsed or -1 if the parsed failed. It is okay to use
     * -1 as the error number because this method is used to parse grid cell positions.
     */
    private int parseInt( String s )
    {
        int i = -1;
        try 
        {
            i = Integer.parseInt( s );
        }
        catch( NumberFormatException ex )
        {
        }
        
        return i;
    }
    
    public boolean isEmpty()
    { return grid == null; }
    
    /**
     * Class used to insert Square into the PriorityQueue when finding the shortest
     * path. This is used to prevent that the actual distance of the Square object 
     * is modified thereby messing up the heap order.
     */
    private class PQEntry implements Comparable<PQEntry>
    {
        Square square;
        int distance;
        
        public PQEntry( Square square, int distance )
        {
            this.square = square;
            this.distance = distance;
        }
        
        /**
         * Compare by the distance
         * @param other
         * @return Whether the distance of this Square is lower,equal or higher
         * wrt to the 'other' Square.
         */
        @Override
        public int compareTo( PQEntry other )
        { return distance - other.distance; }
    }
    
    /**
     * @return A list of all Squares in the grid
     */
    public List<Square> getAllSquares()
    {
        List<Square> list = new ArrayList<>();
        for ( int i = 0; i < numRows; ++i )
            for ( int j = 0; j < numColumns; ++j )
                list.add( grid[ i ][ j ] );
        return list;
    }
    
    /**
     * Find the shortest path from the start Square to the end of the 
     * maze taking into consideration wall-knocking and its penalty.
     * @param start
     * @param wallPenalty 
     */
    public void findShortestPath( Square start, int wallPenalty )
    {
        // Check first the the grid was populated
        if ( grid == null )
        {
            System.out.println( "Error: the maze is empty. No path can be find from it." );
            return;
        }
        
        // Reset all the values of the Squares.
        for ( Square square : getAllSquares() )
        {
            square.setDistance( INFINITY );
            square.setPrevious( null );
            square.setAlreadyProcessed( false );
        }
        
        start.setDistance( 0 );
        
        PriorityQueue<PQEntry> pq = new PriorityQueue<>();
        pq.add( new PQEntry( start, 0 ) );
        
        while( !pq.isEmpty() )
        {
            PQEntry entry = pq.remove();
            Square s = entry.square;
            
            // If the Square has already been processe, skip it.
            if ( s.isAlreadyProcessed() )
                continue;
            
            // For each neighbor, determine if the new distance through the s Square is smaller
            for ( Square adj : s.getAdjacents() )
            {
                // Default cost when going from one Square to another
                int cost = 1;
                
                // Determine if there is a wall between them, and if so add the penalty
                if ( s.hasWall( adj ) )
                    cost += wallPenalty;

                if ( s.getDistance() + cost < adj.getDistance() )
                {
                    adj.setDistance( s.getDistance() + cost );
                    adj.setPrevious( s );
                    pq.add( new PQEntry( adj, adj.getDistance() ) );
                }
            }
            
            s.setAlreadyProcessed( true );
        }
    }
    
    /**
     * @param end
     * @return The path from the 'end' Square to the start of the path
     */
    private List<Square> retrievePath( Square end )
    {
        // Use a stack so that the order of the path is reverted and shown properly
        Stack<Square> stack = new Stack<>();
        // Hold the path
        List<Square> path = new ArrayList<>();
        
        while ( end != null )
        {
            stack.push( end );
            end = end.getPrevious();
        }
        
        while ( !stack.isEmpty() )
            path.add( stack.pop() );
        
        
        return path;
    }
    
    /**
     * Print the path from the end of the maze to the start
     */
    public void printPath()
    {
        
        List<Square> path = retrievePath( BOTTOM_RIGHT );
        
        StringBuilder sb = new StringBuilder();
        
        // Count the total number of walls knocked down in the path
        int wallsKnockedDown = 0;
        for ( Square s : path )
        {
            if ( s.getPrevious() == null )
                continue;
            
            // Check if there is a wall between the parent Square and this Square
            if ( s.getPrevious().hasWall( s ) )
                ++wallsKnockedDown;
            
            String direction = s.getPrevious().directionTo( s );
            sb.append( direction );
        }
        
        int cost = path.get( path.size() - 1 ).getDistance();
        String wallWord = ( wallsKnockedDown == 1 ) ? "wall" : "walls";
        
        // The length of the path is substracted one b/c the first Square in the path is the start
        System.out.println( "Total cost of the path is " + cost + " with " 
                        + wallsKnockedDown + " " + wallWord + " knocked down." );
        System.out.print( sb.toString() );
    }
}

public class ShortestPath 
{

    // Parameters: -p 100000 5 10 15 20 50 -f maze27x9.txt maze40x40.txt maze60x100.txt maze75x75.txt maze80x120.txt  maze85x120.txt maze86x118.txt maze87x119.txt maze173x237.txt
    // Test: -p 100000 5 -f maze-test.txt t.txt maze-test2.txt
    public static void main( String [ ] args )
    {
        int i = 0;
        // Skip all arguments (there should not be any) until the -p is reached
        while ( i < args.length && !args[ i ].equals( "-p" ) )
            ++i;
        
        // Check if it reached the end of the args array which means -p is missing
        if ( i == args.length )
        {
            System.err.println( "The -p argument is missing." );
            return;
        }
                
        ++i;
        
        
        List<Integer> penalties = new ArrayList<>();
        // Attempt to parse all the penalty values
        while( i < args.length && !args[ i ].equals( "-f" ) )
        {
            try {
                penalties.add( Integer.parseInt( args[ i ] ) );
            }
            catch ( NumberFormatException ex )
            {
                System.err.println( "The argument '" + args[ i ] + "' is incorrectly formatted." );
            }
            
            ++i;
        }
        
        // Check if the loop reached the end of the args array which means -f is missing
        if ( i == args.length )
        {
            System.err.println( "The -f argument is missing." );
            return;
        }
        
        ++i;
        
        Maze maze;
        // For each file find the shortest path with each wall-knocking penalty
        while ( i < args.length )
        {
            String filename = args[ i ];
            maze = new Maze( filename );
            for ( Integer p : penalties )
            {
                // If the maze is empty (due to some error in the file data), skip it
                if ( maze.isEmpty() )
                    continue;
                
                System.out.println( "File '" + filename + "' with penalty = " + p + ": ");
                
                long start = System.currentTimeMillis();
                maze.findShortestPath( maze.TOP_LEFT, p );
                long end = System.currentTimeMillis();
                
                maze.printPath();
                
                System.out.println( );
                System.out.println( "(It took " + ( end - start ) + " ms)" );
            }
            System.out.println( );
            i++;
       }
        
        
    }
    
}
