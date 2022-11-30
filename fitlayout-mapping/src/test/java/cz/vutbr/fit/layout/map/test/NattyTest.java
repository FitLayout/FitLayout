/**
 * NattyTest.java
 *
 * Created on 30. 11. 2022, 19:20:08 by burgetr
 */
package cz.vutbr.fit.layout.map.test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.natty.DateGroup;
import org.natty.Parser;

/**
 * 
 * @author burgetr
 */
public class NattyTest
{

    public static void dumpParserResult(String dateString)
    {
        Parser parser = new Parser();
        List<DateGroup> groups = parser.parse(dateString);
        for (DateGroup group : groups)
        {
            System.out.println("Group, date inferred: " + group.isDateInferred() 
                + ", time inferred: " + group.isTimeInferred() 
                + ", recurring: " + group.isRecurring());
            List<Date> dates = group.getDates();
            for (Date date : dates)
            {
                System.out.println("    " + date.toString() + " ~ " + date.toInstant());
                if (group.isTimeInferred())
                    System.out.println("    Local " + LocalDate.ofInstant(date.toInstant(), ZoneId.systemDefault()));
                else
                    System.out.println("    Local " + LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
            }
        }
    }
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        String s = "today 12:30";

        dumpParserResult(s);
    }

}
