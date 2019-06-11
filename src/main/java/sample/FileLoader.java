package sample;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

public class FileLoader {

    public File load(String link, String filename) {
        File xmlFile = downloadFromWeb(link, filename);
        if (xmlFile == null) {
            xmlFile = new File(filename);
            if (xmlFile.exists()) {
                return xmlFile;
            } else {
                return null;
            }
        } else {
            return xmlFile;
        }
    }

    public File downloadFromWeb(String link, String filename) {
        try {
            File file = new File(filename);
            URL url = new URL(link);
            ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            FileChannel fileChannel = fileOutputStream.getChannel();
            fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
