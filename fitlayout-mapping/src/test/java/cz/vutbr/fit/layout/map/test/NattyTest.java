/**
 * NattyTest.java
 *
 * Created on 30. 11. 2022, 19:20:08 by burgetr
 */
package cz.vutbr.fit.layout.map.test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
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
                Instant ins = date.toInstant();
                ins = ins.truncatedTo(ChronoUnit.MINUTES);
                if (group.isTimeInferred())
                    System.out.println("    Local " + LocalDate.ofInstant(ins, ZoneId.systemDefault()));
                else
                    System.out.println("    Local " + LocalDateTime.ofInstant(ins, ZoneId.systemDefault()));
            }
        }
    }
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        String s = "today 12:31";

        dumpParserResult(s);
    }

}
