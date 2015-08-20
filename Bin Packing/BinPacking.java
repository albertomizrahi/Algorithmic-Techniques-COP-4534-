/**
 * Assignment #4:
 * Solves the offline and online version of the bin packing problem by
 * utilizing the heuristics: Next Fit, Worst Fit, Best Fit, First Fit
 * @author Alberto Mizrahi
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.TreeSet;


class TournamentTree
{
    /* 
     * Stores the minimum weight between any two bin siblings in the tree. The 
     * order of the weights is the same as in a heap
    */
    private int [] winners;
    // All the bins where the items are ultimately deposited. They are the leads of the tree
    private Bin [] bins;
    // The maximum capacity of weight a bin can hold
    private int binCapacity;
    
    public TournamentTree( int N, int binCapacity )
    {
        // Increment the total number of times until is a power of 2
        while ( ( N & -N ) != N )
            N++;
        
        winners = new int[ N ];
        
        bins = new Bin[ N ];
        for ( int i = 0; i < bins.length; ++i )
            bins[ i ] = new Bin();
        
        this.binCapacity = binCapacity;
    }
    
    /**
     * Add an item to the first bin that fits in O(logN)
     * @param item 
     */
    public void add( int item )
    {
        // Point at the root of the winners array
        int i = 1;
        // Calculate the maximum size for a bin to be able to store this item
        int search = binCapacity - item;
        
        // While the end of the winners array has not been reached
        while( 2 * i < winners.length )
        {
            i = 2 * i;
            /*
             * If the left child of the current winner bin has a size < than 'search'
             * that means that none of the bins in the left subtree have enough space
             * to store the item. This means the first bin where it will fit must be in
             * the right subtree.
             * If this is not the case, the left subtree is given preference.
            */
            if ( winners[ i ] > search )
                ++i;
        }
        
        // Find the index of the first bin of the pair of bins where the item can be stored
        int binPos = 2 * i - winners.length;
        
        /*
         * We check whether the left bin has enough space. It it does not
         * it means the item must be stored in the right bin
         */
        if ( bins[ binPos ].size() > search )
            ++binPos;
        
        
        bins[ binPos ].addItem( item );
        
        // If the bin position is not divisible by 2, then the right bin was used
        if ( binPos % 2 != 0 )
            --binPos;
        
        // Update the parent of the the pair of bins with the smallest weight between them
        if ( bins[ binPos ].size() <= bins[ binPos + 1 ].size() )
            winners[ i ] = bins[ binPos ].size();
        else
            winners[ i ] = bins[ binPos + 1 ].size();
        
        // Go up through the tree updating the winners
        i /= 2;
        while( i != 0 )
        {
            if ( winners[ 2 * i ] <= winners[ 2 * i + 1 ] )
                winners[ i ] = winners[ 2 * i ];
            else
                winners[ i ] = winners[ 2 * i + 1 ];
            
            i /= 2;
        }
    }
    
    /**
     * @return Return a list of all the bins that have items
     */
    public List<Bin> getBins()
    {
        List<Bin> list = new ArrayList<>();
        
        for ( int i = 0; i < bins.length && bins[ i ].size() != 0; ++i )
            list.add( bins[ i ] );
        
        return list;
    }
}

/*
 * Models a bin object that can hold items of different sizes and has a weight capacity
 */
class Bin implements Comparable<Bin>
{
    public static final int DEFAULT_CAPACITY = 1_000_000_000;
    
    private int id;
    private int capacity;
    private int size;
    private List<Integer> items;
    
    public Bin()
    {
        capacity = DEFAULT_CAPACITY;
        size = 0;
        items = new ArrayList<>();
    }
    
    public Bin( int id )
    {
        this();
        this.id = id;
        
    }
    
    public void addItem( int item )
    { 
        items.add( item );
        size += item;
    }
    
    /**
     * @param item
     * @return True if there is enough space to put the item in this bin
     */
    public boolean hasSpace( int item )
    { return size + item <= capacity; }
    
    public int size()
    { return size; }
    
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        
        for ( int item : items )
            sb.append( item ).append( ", " );
        
        if ( sb.length() > 0 )
            sb.setLength( sb.length() - 2 );
        
        return sb.toString();
    }

    /**
     * Compare two bins by the available space between them.
     * If they have the same available space, break the tie by the bins' ID
     * (This is really only needed for Best Fit)
     * @param o
     * @return 
     */
    @Override
    public int compareTo( Bin o )
    {
        int thisAvaliableSize = capacity - size;
        int otherAvailableSize = o.capacity - o.size;
        
        int diffSize = otherAvailableSize - thisAvaliableSize;

        return ( diffSize == 0 ) ? o.id - id : diffSize;
    }
}

public class BinPacking
{
    
    public static void main( String [ ] args )
    {
        for ( String filename : args )
            try 
            {
                List<Integer> items = processFile( filename );

                System.out.println( "************ ONLINE VERSION *************" );
                System.out.println( "----- Next Fit ----" );
                nextFit( items );
                System.out.println( );
                
                System.out.println( "----- Worst Fit ----" );
                worstFit( items );
                System.out.println( );

                System.out.println( "----- Best Fit -----" );
                bestFit( items );                
                System.out.println( );

                System.out.println( "----- First Fit -----" );
                System.out.println( "Using Tournament Tree" );
                firstFit( items );
                System.out.println( );
                
                // Sort the items from biggest to smallest in order to the offline version
                Collections.sort( items, Collections.reverseOrder() );

                System.out.println( "************ OFFLINE VERSION *************" );
                System.out.println( "----- Next Fit ----" );
                nextFit( items );
                System.out.println( );

                System.out.println( "----- Worst Fit ----" );
                worstFit( items );
                System.out.println( );
                
                System.out.println( "----- Best Fit -----" );
                bestFit( items );
                System.out.println( );
                
                System.out.println( "----- First Fit -----" );
                System.out.println( "Using Tournament Tree" );
                firstFit( items );
                
                
                System.out.println( );
                System.out.println( );
            }
            catch( FileNotFoundException ex )
            {
                System.err.println( "The file " + filename + " was not found." );
            }
    }
    
    /**
     * Heuristic where if an item fits in the last bin used, put it there;
     * Otherwise, create a new bin and put it there.
     * @param items 
     */
    public static void nextFit( List<Integer> items )
    {
        long start = System.currentTimeMillis(),
             end;
        
        List<Bin> bins = new ArrayList<>();
        Bin lastBin = new Bin();
        bins.add( lastBin );
        
        for ( int item : items )
        {
            // If the last bin does not have space, a new bin is created
            if ( ! lastBin.hasSpace( item ) )
            {
                lastBin = new Bin();
                bins.add( lastBin );
            }
                        
            lastBin.addItem( item );
        }
        
        end = System.currentTimeMillis();
        
        printBins( bins, ( end - start ) );   
    }
    
    /**
     * Heuristic where the item is placed in the bin that has the most
     * space. If no such bin exists, a new bin is created and it put there.
     * @param items 
     */
    public static void worstFit( List<Integer> items )
    {
        long start = System.currentTimeMillis(),
             end;
        
        List<Bin> bins = new ArrayList<>();
        bins.add( new Bin() );
        
        PriorityQueue<Bin> pq = new PriorityQueue<Bin>();
        pq.add( bins.get( 0 ) );
        
        for ( int item : items )
        {
            Bin bin = pq.peek();
            /* 
             * Determine if the bin with the most space has enough space for this item.
             * If not, create a new bin and put it there
            */
            if ( ! bin.hasSpace( item ) )
            {
                bin = new Bin();
                bins.add( bin );
            }
            else
                bin = pq.remove();
            
            bin.addItem( item );
            pq.add( bin );
        }
        
        end = System.currentTimeMillis();
        
        printBins( bins, ( end - start ) );
    }
    
    /**
     * Heuristic where the item is put in the fullest bin that still has 
     * enough space to store the item.
     * Since NavigableSet does not accept duplicates, each bin is given an ID
     * and this is used to differentiate bins with the same available space.
     * @param items 
     */
    public static void bestFit( List<Integer> items )
    {
        long start = System.currentTimeMillis(),
             end;
        
        List<Bin> bins = new ArrayList<>();
        bins.add( new Bin( 0 ) );
        
        NavigableSet<Bin> set = new TreeSet<>();
        set.add( bins.get( 0 ) );
        
        for ( int item : items )
        {
            // Create a Bin object used for the search
            Bin toSearch = new Bin();
            // Calculate the maximum weight that a bin must have to be able to store this item
            toSearch.addItem( Bin.DEFAULT_CAPACITY - item );
            
            /*
             * Find the bin with the most weight that still has enough space to
             * to store the item.
            */
            Bin bin = set.floor( toSearch );
            
            // If there is no such bin
            if ( bin == null )
            {
                // Create a new Bin with a new ID
                bin = new Bin( bins.size() );
                bins.add( bin );
            }
            // If there is such a bin, remove it so the item can be added to it
            else
                set.remove( bin );
            
            bin.addItem( item );
            set.add( bin );
        }
        
        end = System.currentTimeMillis();
     
        printBins( bins, ( end - start ) );
    }
    
    /**
     * Heuristic where the item is placed in the first bin that fits; if such
     * a bin does not exist a new one is created.
     * A tournament tree is used for this heuristic in order for the entire operation
     * to be O(NlogN)
     * @param items 
     */
    public static void firstFit( List<Integer> items )
    {
        long start = System.currentTimeMillis(),
             end;
        
        TournamentTree tree = new TournamentTree( items.size(), Bin.DEFAULT_CAPACITY );
        
        for( int item : items )
            tree.add( item );
                
        end = System.currentTimeMillis();
        
        printBins( tree.getBins(), ( end - start ) );
    }
    
    /**
     * Print the total # of bins used and the first 10 bins.
     * @param bins
     * @param elapsedTime 
     */
    private static void printBins( List<Bin> bins, long elapsedTime )
    {
        System.out.println( "# of Bins used: " + bins.size() );
        System.out.println( "First 10 bins:" );
        
        int binsToShow = ( bins.size() >= 10 ) ? 10 : bins.size();
        for ( int i = 0; i < binsToShow; ++i )
        {
            System.out.println( ( i + 1 ) + ": " + bins.get( i ) );
        }
        
        System.out.println( "Elapsed Time: " + elapsedTime + "ms." );
    }
    
    /**
     * Process each file and store the weights in a list.
     * @param filename
     * @return
     * @throws FileNotFoundException 
     */
    private static List<Integer> processFile( String filename ) throws FileNotFoundException
    {
        System.out.println( "File: " + filename );
        Scanner scanner = new Scanner( new File( filename ) );
        
        List<Integer> items = new ArrayList<>();
        // Store the sum of all the weights
        long totalSize = 1L;
        while ( scanner.hasNextLine() )
        {
            String line = scanner.nextLine().trim();
            int item = Integer.parseInt( line );
            items.add( item );
            totalSize += item;
        }
        
        // Print the ideal number of bins
        double idealNumBins = Math.ceil( ( (double) totalSize ) / Bin.DEFAULT_CAPACITY );
        System.out.println( "Ideal # of Bins: " + idealNumBins );
        
        return items;
    }
    
    
}
