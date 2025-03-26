import java.util.ArrayList;

public class PokeHash {

    // Constants
    public static final int TABLE_SIZE = 10000;
    public static final double MAX_LOAD_FACTOR = 0.8;

    // Instance variables
    public ArrayList<PokemanzInfo> table;
    public int count;

    // Constructor
    public PokeHash() {
        table = new ArrayList<>(TABLE_SIZE);
        for (int i = 0; i < TABLE_SIZE; i++) {
            table.add(null);
        }
        count = 0;
    }

    // Returns the hash value of a key abs valued then modded by the table size as per requested
    public static int hash(PokemanzInfo pokemon) {
        return Math.abs(pokemon.name.hashCode()) % TABLE_SIZE;
    }
}