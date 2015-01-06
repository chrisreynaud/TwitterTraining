import java.io.IOException;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;

/**
 * Utility class: this class is in charge to sort a TreeMap object
 * and to manage client request
 * 
 * @author creynaud
 *
 */
class ValueComparator implements Comparator<String> {

	Hashtable<String, Integer> base;
    public ValueComparator(Hashtable<String, Integer> base) {
        this.base = base;
    }
    
	/**
	 * The compare method is executed on TreeMap object to sort element
	 * by value, big one in first in the list
	 *            
	 */
    public int compare(String a, String b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        }
    }
}
