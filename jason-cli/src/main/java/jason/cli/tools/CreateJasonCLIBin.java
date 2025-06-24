package jason.cli.tools;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class CreateJasonCLIBin {
    public static void main(String[] args) {
        var jasonBinFile = new File(args[0]); //"build/bin/jason"; // Name of the file to write to
        String jarFileName = args[1];

        try (var writer = new FileOutputStream(jasonBinFile);
             var jarInput = new FileInputStream(new File(jarFileName))) {
            writer.write("""
                    #!/bin/sh
                    exec java -jar $0 $@
                    echo "exit 0"
                    """.getBytes(StandardCharsets.UTF_8));

            // copy jason jar at the end of the file
            byte[] buffer = new byte[1024];
            int length;
            while ((length = jarInput.read(buffer)) > 0) {
                writer.write(buffer, 0, length);
            }
            jasonBinFile.setExecutable(true);
            System.out.println("Successfully wrote to the file: " + jasonBinFile);
        } catch (IOException e) {
            System.err.println("An error occurred while writing to the file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
