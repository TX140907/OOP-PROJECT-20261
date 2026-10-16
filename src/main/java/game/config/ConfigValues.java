package game.config;

import java.awt.Color;
import java.util.Properties;
import java.util.HashMap;
import java.util.Map;

/** Immutable, validated access to UTF-8 properties; invalid values never silently fall back. */
public final class ConfigValues {
    private final Properties values = new Properties();
    private final Map<String,Double> numbers;
    public ConfigValues(Properties source) {
        values.putAll(source);
        Map<String,Double> parsed=new HashMap<>();
        for (String key:source.stringPropertyNames()) {
            try { double value=Double.parseDouble(source.getProperty(key).trim());
                if (Double.isFinite(value)) parsed.put(key,value);
            } catch (NumberFormatException ignored) { /* Non-numeric settings are accessed through text/bool/color. */ }
        }
        numbers=Map.copyOf(parsed);
    }
    public String text(String key) {
        String value = values.getProperty(key);
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Missing key: " + key);
        return value.trim();
    }
    public String text(String key, String fallback) { return values.containsKey(key) ? text(key) : fallback; }
    public double number(String key) {
        text(key); // Missing-key errors are distinct from malformed values.
        Double value=numbers.get(key);
        if (value==null) throw new IllegalArgumentException("Invalid number: " + key);
        return value;
    }
    public double number(String key, double fallback) { return values.containsKey(key) ? number(key) : fallback; }
    public double range(String key, double min, double max) {
        double value = number(key);
        if (value < min || value > max) throw new IllegalArgumentException("Out of range: " + key);
        return value;
    }
    public int integer(String key) {
        double value = number(key);
        if (value != Math.rint(value) || value < Integer.MIN_VALUE || value > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Invalid integer: " + key);
        return (int) value;
    }
    public boolean bool(String key) {
        String value = text(key);
        if (!value.equals("true") && !value.equals("false")) throw new IllegalArgumentException("Invalid boolean: " + key);
        return Boolean.parseBoolean(value);
    }
    public Color color(String key) {
        try { return Color.decode(text(key)); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Invalid color: " + key, e); }
    }
    public Properties copy() { Properties copy = new Properties(); copy.putAll(values); return copy; }
}
