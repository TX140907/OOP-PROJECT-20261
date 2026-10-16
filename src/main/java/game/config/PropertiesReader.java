package game.config;

import java.io.*;
import java.util.Properties;

/** Reads standard properties plus comments introduced by a spaced " # " after a value. */
public final class PropertiesReader {
    private PropertiesReader() {}
    public static Properties read(Reader reader) throws IOException {
        BufferedReader input=new BufferedReader(reader);
        StringBuilder content=new StringBuilder();
        boolean first=true;
        for (String line;(line=input.readLine())!=null;) {
            if (first && line.startsWith(Character.toString(0xFEFF))) line=line.substring(1);
            first=false;
            String trimmed=line.stripLeading();
            if (!trimmed.startsWith("#") && !trimmed.startsWith("!")) {
                for (int i=1;i<line.length();i++) {
                    if (line.charAt(i)=='#' && Character.isWhitespace(line.charAt(i-1))
                            && (i+1==line.length() || Character.isWhitespace(line.charAt(i+1)))) {
                        line=line.substring(0,i).stripTrailing();break;
                    }
                }
            }
            content.append(line).append('\n');
        }
        Properties properties=new Properties();
        properties.load(new StringReader(content.toString()));
        return properties;
    }
}
