/**
 * Finds the longest string that occurs K times in a file.
 * @author Alberto Mizrahi
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class LongestRepeatingString
{

    public static Map<String,Integer> kTimes;
    public static String data;
    public static int lineLength;
    public static int K = 10;
    
    public static void main( String[] args )
    {
        for ( String filename : args )
        {
            try
            {
                String contents = readFile( filename );
                
                System.out.println( "****PROCESSING: " + filename );

                int [ ] sufArr = new int[ contents.length( ) ];
                int [ ] LCP = new int[ contents.length( ) ];
                // Create the suffix and LCP arrays for the file string
                SuffixArray.createSuffixArray( contents, sufArr, LCP );

                long start, end;
                start = System.currentTimeMillis();
                // Compute all the substrings that occur 2...K times in the file
                computeKTimes( contents, sufArr, LCP );
                end = System.currentTimeMillis();

                // Return the longest strings that occur 2...K times
                String [ ] longest = longestKTimes();

                printKTimes( longest );
                
                System.out.println( "It took " + ( end - start ) + " ms." );
                
                System.out.println();
            } 
            catch ( FileNotFoundException ex )
            {
                System.out.println( "The file '" + filename + "' was not found." );
                System.out.println();
            }
        }
        
    }
    
    /**
     * Read the entire contents of a file and return it a String
     * @param filename
     * @return
     * @throws FileNotFoundException 
     */
    private static String readFile( String filename ) throws FileNotFoundException
    {
        Scanner scanner = new Scanner( new File( filename ) );
        
        StringBuilder sb = new StringBuilder();
        
        while ( scanner.hasNextLine() )
        {
            String line = scanner.nextLine();
            
            if ( lineLength == 0 )
                lineLength = line.length();
            
            sb.append( line );
        }
        
        data = sb.toString();
        
        return sb.toString();
    }
    
    /**
     * Find and store all the strings that occur 2..K times
     * @param str String containing the entire file
     * @param sufArr Suffix array
     * @param LCP LCP array
     */
    private static void computeKTimes( String str, int [ ] sufArr, int [ ] LCP )
    {
        kTimes = new HashMap<>();
        /*
         * This two sets are used to keep track of the substring seen while processing
         * the current suffix as well as the ones seen in the previous suffix.
         * This is used to prevent overcounting a substring.
        */
        Set<String> used = new HashSet<>();
        Set<String> previous = new HashSet<>();
        
        for ( int i = 0; i < sufArr.length; ++i )
        {
            // If the LCP between this and the previous suffix is 0, there is nothing to process
            if ( LCP[ i ] == 0 )
                continue;
            
            // Start of the suffix in the file string
            int start = sufArr[ i ];
            // End of the LCP in the file string
            int end = sufArr[ i ] + LCP[ i ];
                        
            /*
             * For every substring of the LCP, increase the count of that substring
            */
            StringBuilder sb = new StringBuilder();
            for ( int j = start; j < end; ++j )
            {
                sb.append( str.charAt( j ) );
                String substr = sb.toString();
                
                // Get the number of times this string has been seen
                Integer times = kTimes.get( substr );
                
                if ( times == null )
                    times = 0;
                
                // If it has been seen more than K times, there is no need to further process it
                if ( times > K )
                    continue;
                
                // Add the substring to currently seen substrings
                used.add( substr );
                
                /*
                 * If the current substring has not been seen in the previous LCP,
                 * it must be counted twice because it appears in this suffix and
                 * in the previous. Otherwise, count it only once.
                 * 
                */
                if ( !previous.contains( substr ) )
                    times += 2;
                else
                    ++times;
                
                kTimes.put( substr, times );
            }
            
            /*
             * Set the current substring seen as the previous one in preparation
             * for the next loop.
            */
            previous = new HashSet<>( used );
            used.clear();
        }
    }
    
    /**
     * Find and return the longest substring that occurred 2..K times in the
     * file.
     * @param kMax The max number of K occurrences that are wanted
     * @return 
     */
    private static String [ ] longestKTimes()
    {
        String [ ] longest = new String[ K + 1 ];
        
        for ( Map.Entry<String,Integer> entry : kTimes.entrySet() )
        {
            String str = entry.getKey();
            int times = entry.getValue();
            
            // If this substring ocurred more than K times, it doess not matter
            if ( times > K )
                continue;
            
            if ( longest[ times ] == null || str.length() > longest[ times ].length() )
                longest[ times ] = str;
        }
           
        return longest;
    }
    
    /**
     * Print the substrings that occurred 2..K times in the file as well
     * as in what lines in the file each one occurred.
     * If the substring has more 50 characters, print the first and last 20 characters.
     * @param longest 
     */
    private static void printKTimes( String [ ] longest )
    {
        for ( int i = 1; i < longest.length; ++i )
            if ( longest[ i ] != null )
            {
                String str = longest[ i ];
                int strLength = str.length();
                
                // Trim the string if it has more than 50 characters
                String trim = str;
                if ( strLength > 50 )
                    trim = str.substring( 0, 20 ) + "..." + str.substring( strLength - 20 );
                
                System.out.println( "Longest sequence that occurs  k = " + i + " times "
                                    + "has basic length " + strLength + " and is "
                                    + trim );
                
                /*
                 * To find the lines where the string occurred, get the index (or indices) 
                 * of where it ocurr in the file string (which contains the entire file),
                 * divide that by the length of the lines and add 1.
                 */
                int occurrences = 1;
                int index = -1;
                while ( true )
                {
                    index = data.indexOf( str, index + 1);
                    if ( index == -1 )
                        break;
                    
                    int lineNum = index / lineLength + 1;
                                        
                    System.out.println( "Occurrence #" + occurrences + " at line " + lineNum );
                    ++occurrences;
                }
                
                System.out.println();
                
            }
    }   
}
