/**
 * Program utilizes Dynamic Programming to:
 *  1) Find the largest submatrix of 1s in a 0s and 1s matrix
 *  2) Find the longest common subsequence between two strings
 *  3) Find the optimal way to make 1...X cents using the given coins
 * @author Alberto Mizrahi
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Find the largest submatrix of 1s in a matrix of 0s and 1s
 * @author alberto
 */
class LSS
{
    public static int [ ][ ] table;
    
    public static void findLSS( int [ ][ ] matrix )
    {
        table = new int[ matrix.length ][ matrix[ 0 ].length ];
        int maxSquare = 0, // Store the largest square found so far
            bottomI = 0, // Store the row of the bottom right corner of the largest square found
            bottomJ = 0; // Store the column of the bottom right corner of the largest square found
        
        for( int i = 0; i < matrix.length; ++i )
            for ( int j = 0; j < matrix[ 0 ].length; ++j )
            {
                // If entry is in the first row or column, or if the entry is 0, there is nothing to do 
                if ( i == 0 || j == 0 || matrix[ i ][ j ] == 0 )
                    table[ i ][ j ] = matrix[ i ][ j ];
                else
                {
                    /*
                     * Else, if the entry in the matrix is a 1, find the minimum value
                     * between the top, left and top left entries wrt to the current one.
                     * The value of the current entry will be that minimum + 1
                    */
                    int minLeftTop = Math.min( table[ i ][ j - 1 ], table[ i - 1 ][ j ] );
                    int min = Math.min( table[ i - 1 ][ j - 1 ], minLeftTop );
                    
                    table[ i ][ j ] = 1 + min;
                    
                    // If a larger submatrix found, store it position and size
                    if ( table[ i ][ j ] > maxSquare )
                    {
                        maxSquare = table[ i ][ j ];
                        bottomI = i;
                        bottomJ = j;
                    }
                }
                
            }
        
        System.out.println( "The largest square submatrix of 1's is of size: " + maxSquare );
        int topLeftI = bottomI - maxSquare + 1;
        int topLeftJ = bottomJ - maxSquare + 1;
        System.out.println( "The top left corner of this submatrix is at row " 
                            + topLeftI + " and column " + topLeftJ + " (both starting from 0)" );

    }
}

/*
 * Find the longest common subsequence between two strings
*/
class LCS
{
    public static int [ ][ ] table;
    
    public static String calcLCS( String s1, String s2 )
    {
        table = new int[ s1.length() + 1 ][ s2.length() + 1 ];
        
        for ( int i = 1; i < table.length; ++i )
            for ( int j = 1; j < table[ 0 ].length; ++j )
                /*
                 * if the respective characters of the two strings are the same,
                 * insert in the corresponding entry, value of top left entry + 1
                */
                if ( s1.charAt( i - 1 ) == s2.charAt( j - 1 ) )
                    table[ i ][ j ] = 1 + table[ i - 1 ][ j - 1 ];
                // Else, store in this entry the min. value of the top andleft entries
                else
                    table[ i ][ j ] = Math.max( table[ i ][ j - 1 ], table[ i - 1][ j ] );
            
        return readLCS( s1, s2 );
    }
    
    /**
     * Read one LCS from the table
     * @param s1
     * @param s2
     * @return LCS
     */
    private static String readLCS( String s1, String s2 )
    {
        StringBuilder sb = new StringBuilder();
        int i = table.length - 1;
        int j = table[ 0 ].length - 1;
        
        while ( i > 0 && j > 0 )
        {
            /*
             * If the two chars. are equal, write it to the string and go to the
             * top left entry
            */
            if ( s1.charAt( i - 1 ) == s2.charAt( j - 1 ) )
            {
                sb.append( s1.charAt( i - 1 ) );
                --i;--j;
            }
            
            // Else choose the biggest entry between the top and left entries
            else if ( table[ i - 1 ][ j ] > table[ i ][ j - 1 ] )
                --i;
            else
                --j;
        }
        
        // Since the LCS is read backwards from the table, the string must be reversed
        return sb.reverse().toString();
    }
}

/*
 * Calculate the optimal amount of change needed to make X cents with
 * the given coins/
 */
class OptimalChangeCalc
{
    private List<Integer> coins;
    // For each cent, store the list of optimal change needed to make it
    private List<List<Integer>> optimal;
    
    public OptimalChangeCalc( List<Integer> coins )
    { 
        this.coins = new ArrayList<Integer>( coins );
    }
    
    public void calcOptimalChange( int cents )
    {
        optimal = new ArrayList<>();
        for ( int i = 1; i <= cents + 1; ++i )
            optimal.add( new ArrayList<Integer>() );
        
        // The optimal change for a coin is the coin itself
        for ( int coin : coins )
            optimal.get( coin ).add( coin );
        
        for ( int c = 1; c <= cents; ++c )
        {
            if ( ! optimal.get( c ).isEmpty() )
                continue;
            
            List<Integer> optimalList = null;
            int lastCoin = 0;
            int maxListSize = Integer.MAX_VALUE;
            
            /*
             * Given X cents, find the optimal way of making it by finding the best of
             * making X - c where c is each coin
             */
            for ( int coin : coins )
            {
                if ( c - coin < 0 )
                    break;
                
                List<Integer> l1 = optimal.get( c - coin );
                int listSize = l1.size() + 1;
                
                if ( listSize < maxListSize )
                {
                    maxListSize = listSize;
                    optimalList = l1;
                    lastCoin = coin;
                }
            }
            
            List<Integer> optimalChange = new ArrayList<>( optimalList );
            optimalChange.add( lastCoin );
            optimal.set( c, optimalChange );
        }
    }
    
    public void printOptimalChange()
    {
        StringBuilder sb = new StringBuilder();
        
        for ( int i = 1; i < optimal.size(); ++i )
        {
            System.out.print( i + ": " );
            List<Integer> optimalChange = optimal.get( i );
            for ( int coin : optimalChange )
                sb.append( coin ).append( ", " );
            
            if ( sb.length() > 2 )
                sb.setLength( sb.length() - 2 );
            
            String coin_s = ( optimalChange.size() == 1 ) ? "coin" : "coins";
            System.out.println( sb.toString() + "\t(" + optimalChange.size() + " " + coin_s + " needed)" );
            sb.setLength( 0 );
        }
    }
}
public class DynamicProg
{
    public static void main( String [ ] args )
    {
        if ( args.length != 2 )
        {
            System.err.println( "Please pass the names of the matrix and string files as parameters." );
            System.exit( 1 );
        }
        
        long start,
             end;
        
        System.out.println( "*** PROBLEM #1: Largest Square Submatrix of 1's" );
        try
        {
            int [ ][ ] matrix = readMatrix( args[ 0 ] );
            
            start = System.currentTimeMillis();
            LSS.findLSS( matrix );
            end = System.currentTimeMillis();
            System.out.println( "(It took " + ( end - start ) + " ms.)" );
       }
        catch( FileNotFoundException ex )
        {
            System.err.println( "The file: '" + args[ 0 ] + "' was not found." );
        }
        
        System.out.println( );
        
        System.out.println( "*** PROBLEM #2: Longest Common Subsequence of two strings" );
        try
        {
            String [ ] strings = readLCSWords( args[ 1 ] );
            
            System.out.println( "The lonesg common subsequence between the two words in the file is:" );
            
            start = System.currentTimeMillis();
            String lcs = LCS.calcLCS( strings[ 0 ], strings[ 1 ] );
            end = System.currentTimeMillis();
            
            System.out.println( lcs );
            System.out.println( "The LCS is of size " + lcs.length() );
            System.out.println( "(It took " + ( end - start ) + " ms.)" );
        }
        catch( FileNotFoundException ex )
        {
            System.err.println( "The file: '" + args[ 1 ] + "' was not found." );
        }
        catch( IllegalArgumentException ex )
        {
            System.err.println( ex.getMessage() );
        }
        
        System.out.println( );
        
        System.out.println( "*** PROBLEM #3: Optimal Coin Change" );
        final List<Integer> coins = new ArrayList<>();
        coins.add( 1 );
        coins.add( 5 );
        coins.add( 10 );
        coins.add( 18 );
        coins.add( 25 );
        OptimalChangeCalc calc = new OptimalChangeCalc( coins );
        
        start = System.currentTimeMillis();
        calc.calcOptimalChange( 100 );
        end = System.currentTimeMillis();
        
        System.out.println( "Coins utilized: 1, 5, 10, 18, 25" );
        System.out.println( "The following is a table of cents along with the list "
                            + " of minimum coins needed to make that change using the coins above." );
        calc.printOptimalChange();
        System.out.println( "(It took " + ( end - start ) + " ms.)" );
    }
    
    private static int [ ][ ] readMatrix( String filename ) throws FileNotFoundException
    {
        Scanner scanner = new Scanner( new File( filename ) );
        
        List<int[]> matrixList = new ArrayList<>();
        
        while( scanner.hasNextLine() )
        {
            String line = scanner.nextLine().trim();
            String [ ] tokens = line.split( "" );
            
            int [ ] row = new int[ tokens.length - 1 ];
            int rowNum = 0;
            for ( int i = 0; i < row.length; ++i )
            {
                try
                {
                    row[ i ] = Integer.parseInt( tokens[ i + 1 ] );
                }
                catch( NumberFormatException ex )
                {
                    System.out.println( "The character at ( " + rowNum + ", " + i + " ) "
                                        + "is not a number. A 0 was assumed." );
                    row[ i ] = 0;
                }
                
                if ( row[ i ] != 0 && row[ i ] != 1 )
                {
                    System.out.println( "The character at ( " + rowNum + ", " + i + " ) "
                                        + "is not a 0 or 1. A 0 was assumed." );
                    row[ i ] = 0;
                }
                
            }
            
            matrixList.add( row );
            
            ++rowNum;
        }
        
        int [ ][ ] matrix = new int[ matrixList.size() ][ matrixList.get( 0 ).length ];
        
        for ( int i = 0; i < matrixList.size(); ++i)
            matrix[ i ] = matrixList.get( i );
        
        return matrix;
    }
    
    private static String [ ] readLCSWords( String filename ) throws FileNotFoundException
    {
        Scanner scanner = new Scanner( new File( filename ) );
        String [ ] strings = new String[ 2 ];
        
        int count = 0;
        while( scanner.hasNextLine() )
        {
            if ( count == 2 )
                break;
            
            strings[ count ] = scanner.nextLine().trim();
            ++count;
        }
        
        if ( count != 2 )
            throw new IllegalArgumentException( "The file '" + filename + "'must "
                                                + "only contain 2 strings." );
        
        return strings;
    }
}
