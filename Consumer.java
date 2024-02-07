import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class Consumer {
    private static InputStream in = new InputStream() {
        @Override
        public int read() throws IOException {
            return System.in.read();
        }

        @Override
        public void close() {
        }
    };

    private static long readLong() {
        try (Scanner s = new Scanner(in)) {
            return s.nextLong();
        }
    }

    public static void main(String... args) {
        for (long i = 1000000000; ; ++i) {
            long j = readLong();
            System.out.println(i + " " + j);
            if (i != j) {
                break;
            }
        }
    }
}
