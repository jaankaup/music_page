package T3;

import javax.ws.rs.core.StreamingOutput;
import java.io.RandomAccessFile;
import java.io.OutputStream;
import java.io.IOException;
import javax.ws.rs.WebApplicationException;

/*https://stackoverflow.com/questions/14410344/jersey-rest-support-resume-media-streaming.*/
/* TODO: implement my own version lates. */
public class MediaStreamer implements StreamingOutput {

    private int length;
    private RandomAccessFile raf;
    final byte[] buf = new byte[4096];

    public MediaStreamer(int length, RandomAccessFile raf) {
        this.length = length;
        this.raf = raf;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException, WebApplicationException {
        try {
            while( length != 0) {
                int read = raf.read(buf, 0, buf.length > length ? length : buf.length);
                outputStream.write(buf, 0, read);
                length -= read;
            }
        } finally {
            raf.close();
        }
    }

    public int getLenth() {
        return length;
    }
}
