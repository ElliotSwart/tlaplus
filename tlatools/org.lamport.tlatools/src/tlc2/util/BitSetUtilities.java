package tlc2.util;

import java.io.IOException;
import java.util.BitSet;
import java.util.Iterator;

public class BitSetUtilities {

    public static String generateString(final BitSet bitSet, final int start, final int length, final char one, final char zero) {
        final StringBuilder buf = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            if (bitSet.get(start + i)) {
                buf.append(one);
            } else {
                buf.append(zero);
            }
        }
        return "[" + buf.reverse() + "]";
    }

    /** Write the bit vector to a file. */
    public static void write(final BitSet bitSet, final BufferedRandomAccessFile raf) throws IOException {
        final int bytes = bitSet.size() / 8;
        raf.writeNat(bytes);
        raf.write(bitSet.toByteArray());
    }

    /** Read a bit vector from a file */
    public static BitSet fromFile(final BufferedRandomAccessFile raf) throws IOException {
        final int bytes = raf.readNat();
        final byte[] byteArray = new byte[bytes];

        raf.read(byteArray);

        return BitSet.valueOf(byteArray);
    }
}
