// Copyright (c) 2003 Compaq Corporation.  All rights reserved.
// Last modified on Mon 30 Apr 2007 at  9:21:13 PST by lamport
//      modified on Wed Aug 23 13:22:02 PDT 2000 by yuanyu
//      modified on Wed Jun 16 14:36:34 EDT 1999 by tuttle

package tlc2.pprint;

import tlc2.output.EC;
import tlc2.output.MP;
import util.ToolIO;


public class PrettyPrint {

    public static String mypp(final String value, final int width) {
        try {
            final Node tree = Parse.parse(value, 0);
            if (tree.last() < value.length() - 1) {
                return value;
            }
            final String format = Format.format(tree, width, 0, "");
            return format;
        } catch (final Exception e) {
            // Assert.printStack(e);
            return value;
        }
    }

    public static String pp(final String value, final int width) {
        return pp(value, width, "");
    }

    public static String pp(final String value, final int width, final String padding) {
        try {
            final Node tree = Parse.parse(value, 0);
            final String format = Format.format(tree, width, 0, padding);
            return format;
        } catch (final ParseException e) {
            MP.printTLCBug(EC.TLC_PP_PARSING_VALUE, new String[]{value, e.getMessage()});
            return value;
        } catch (final FormatException e) {
            MP.printTLCBug(EC.TLC_PP_FORMATING_VALUE, new String[]{value, e.getMessage()});
            return value;
        }
    }

    /**
     * Not called from the code. Seems to be for testing only
     */
    public static void main(final String[] argv) {
        final String value = argv[0];
        final int width = Integer.parseInt(argv[1]);
        final String padding;

        if (argv.length > 2) {
            padding = argv[2];
        } else {
            padding = "";
        }

        for (int i = 0; i < width; i++) {
            ToolIO.out.print("*");
        }
        ToolIO.out.println();
        ToolIO.out.println(pp(value, width, padding));
        for (int i = 0; i < width; i++) {
            ToolIO.out.print("*");
        }
        ToolIO.out.println();

    }

}
