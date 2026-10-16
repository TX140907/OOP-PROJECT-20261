package game.config;

import java.io.StringReader;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PropertiesReaderTest {
    @Test void inlineCommentsDoNotChangeNumbersBooleansOrVietnameseNames() throws Exception {
        var p=PropertiesReader.read(new StringReader("speed=420  # Tốc độ ngang\nenabled=false # Bật hoặc tắt\nname=Ngày 5 - Trực thăng # Tên màn\n"));
        assertEquals("420",p.getProperty("speed"));assertEquals("false",p.getProperty("enabled"));
        assertEquals("Ngày 5 - Trực thăng",p.getProperty("name"));
    }
    @Test void colorsAndLiteralHashesStayIntact() throws Exception {
        var p=PropertiesReader.read(new StringReader("color = #467ca2  # Màu nền\npath=/image/a#b.png # Ảnh\nname=Ngày \\# một # Tên\n"));
        assertEquals("#467ca2",p.getProperty("color"));assertEquals("/image/a#b.png",p.getProperty("path"));
        assertEquals("Ngày # một",p.getProperty("name"));
    }
    @Test void bomHeadersAndStandardPropertyEscapesRemainSupported() throws Exception {
        var p=PropertiesReader.read(new StringReader(Character.toString(0xFEFF)+"# Nhóm 1\n! Ghi chú\nname: Việt Nam\nmessage=one\\ntwo\n"));
        assertEquals(2,p.size());assertEquals("Việt Nam",p.getProperty("name"));assertEquals("one\ntwo",p.getProperty("message"));
    }
}
