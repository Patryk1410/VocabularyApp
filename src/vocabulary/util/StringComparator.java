package vocabulary.util;

import java.util.Comparator;

public class StringComparator implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {
        if (o1.equals(o2)) {
            return 0;
        } else if (containsNumber(o1) && containsNumber(o2)) {
            String name1, number1, name2, number2;
            int i;
            for (i = o1.length()-1; isNumber(o1.charAt(i)); --i) { }
            name1 = o1.substring(0, ++i);
            number1 = o1.substring(i);
            for (i = o2.length()-1; isNumber(o2.charAt(i)); --i) { }
            name2 = o2.substring(0, ++i);
            number2 = o2.substring(i);
            if (name1.equals(name2)) {
                return Integer.parseInt(number1) - Integer.parseInt(number2);
            } else {
                return o1.compareTo(o2);
            }
        } else {
            return o1.compareTo(o2);
        }
    }

    public boolean containsNumber(String s) {
        char last = s.charAt(s.length()-1);
        return isNumber(last);
    }
    
    public boolean isNumber(char c) {
        return c >= '0' && c <= '9';
    }
}
