import java.util.HashMap;

public class PlateDatabase {
    private final HashMap<String, String> database = new HashMap<String, String>();

    public PlateDatabase() {
    }

    public int getNrPlates() {
        return this.database.size();
    }

    public boolean hasKey(String key) {
        return this.database.containsKey(key);
    }

    public boolean insertKeyValue(String key, String value) {
        if (hasKey(key)) {
            System.out.println("DBG:Key already registered");
            return false;
        }

        this.database.put(key, value);
        return true;
    }

    public String queryPlateOwner(String license) {
        return this.database.get(license);
    }
}