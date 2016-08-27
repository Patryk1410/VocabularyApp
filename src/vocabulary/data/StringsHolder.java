package vocabulary.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class StringsHolder {

    public static String fLanguage = null;
    public static String sLanguage = null;
    public static String version = null;
    
    public static void init() {
        try(BufferedReader br = new BufferedReader(new FileReader("resources/app_data.txt"))) {
            fLanguage = br.readLine();
            sLanguage = br.readLine();
            version = br.readLine();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            fLanguage = fLanguage == null ? "error" : fLanguage;
            sLanguage = sLanguage == null ? "error" : sLanguage;
            version = version == null ? "error" : version;
        }
    }
}
