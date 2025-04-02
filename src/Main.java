import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.lang.Thread;

import static java.lang.System.currentTimeMillis;

public class Main extends Thread implements Pokedex {
    public static PokeHash pokedex = new PokeHash();
    //public static final String FILE_DATA_NAME = "src/pokemon_pokedex_alt.csv";
    //public static final String FILE_DATA_NAME = "src/new_pokemon_pokedex.csv";
    public static final String FILE_DATA_NAME = "src/new_pokemon_pokedex_1_million.csv";
    public static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private boolean isReady = false;

    public void run() {
        Thread setUpThread = new Thread(this::setUp);
        Thread uiRunThread = new Thread(this::uiRun);

        setUpThread.start();
        uiRunThread.start();

        try {
            setUpThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set-up sequence that is run once at the start of Main
     */
    public void setUp() {
        isReady = false;
        System.out.println("Welcome to the Pokedex!");
        help();

        isReady = true;

        // Start as a thread
        Thread getDataThread = new Thread(() -> {
            long x = currentTimeMillis();
            PokemanzInfo.getData(this);
            System.out.println("Data loaded in " + (currentTimeMillis() - x) + "ms.");
        });

        getDataThread.start();

    }

    /**
     * Main logics block
     * @return true if pokedex is running, false if pokedex stops
     */
    public void uiRun() {
        while (true) {
            while (!isReady) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            try {
                System.out.print("\nEnter command: ");
                String command[] = reader.readLine().split(" ");
                command[0] = command[0].trim().toLowerCase();
                int amount = 1;

                if (command.length > 1) {
                    command[1] = command[1].trim().toLowerCase();
                    try {
                        if (Integer.parseInt(command[1]) >= 1) {
                            amount = Integer.parseInt(command[1]);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid amount, defaulting to 1.");
                        amount = 1;
                    }

                }

                switch (command[0]) {
                    case "add":
                        System.out.println("Enter the Pokemon name:");
                        String name = reader.readLine();
                        System.out.println("Enter the Pokemon details:");
                        String entry = reader.readLine();
                        addThread(name, entry);
                        break;
                    case "delete":
                        System.out.println("Enter the Pokemon name to delete:");
                        name = reader.readLine();
                        deleteThread(name);
                        break;
                    case "find":
                        findThread(amount);
                        break;
                    case "printht":
                        printHT();
                        break;
                    case "who":
                        who();
                        break;
                    case "help":
                    case "?":
                        help();
                        break;
                    case "exit":
                    case "q":
                    case "x":
                        exit();
                        return;
                    case "clear":
                        clearConsoleThread(amount);
                        break;
                    default:
                        System.out.println("Invalid command. Type 'help' or '?' for a list of commands.");
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Search multiplexing
     *
     * @param amount The amount of searches to be done.
     */
    public void findThread(int amount) throws IOException {
        isReady = false;

        String[] names = new String[amount];

        for (int i = 0; i < amount; i++) {
            System.out.println("Enter the Pokemon name to find:");
            names[i] = reader.readLine();
        }

        for (String name : names) {
            Thread findThread = new Thread(() -> {
                find(name);
            });
            findThread.start();
        }
    }

    /**
     * Search.
     *
     * @param pokemon The name of the Pokemon
     * @return The full entry associated with this Pokemon.
     */
    public String find(String pokemon) {
        int hash = Math.abs(pokemon.hashCode()) % pokedex.TABLE_SIZE;
        int i = 0;
        while (pokedex.table.get((hash + i * i) % pokedex.TABLE_SIZE) != null) {
            isReady = false;
            if (pokedex.table.get((hash + i * i) % pokedex.TABLE_SIZE).name.equals(pokemon)) {
                pokedex.table.get((hash + i * i) % pokedex.TABLE_SIZE).printEntry();
                isReady = true;
                return "Pokemon found!";
            }
            i++;
        }
        isReady = true;
        return null;
    }

    public void addThread(String name, String entry) {
        Thread addThread = new Thread(() -> {
            if (add(name.toLowerCase(), entry)) {
                System.out.println("Pokemon added.");
            } else {
                System.out.println("Failed to add Pokemon, see reason above.");
            }
        });
        addThread.start();
        try {
            addThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add a new entry.
     * @param pokemon The name will be the table key.
     * @param entry The value that will be inserted.
     * @return true if insertion was successful
     */
    public boolean add(String pokemon, String entry) {

        if (getLoadFactor() >= PokeHash.MAX_LOAD_FACTOR) {
            System.out.println("Too many pokemon, wait for the next game!");
            return false; // Too many pokemon, wait for the next game!
        }

        String[] values = entry.split(",");

        if (values.length < 10 && values.length > 12) {
            System.out.println("Invalid details.");
            return false;
        }

        int k = 0;
        // Parsing extry string
        try {
            PokemanzInfo pokemanz = new PokemanzInfo(
                    Integer.parseInt(values[k++].trim()), // attack
                    values[k++].trim(),                   // classification
                    Integer.parseInt(values[k++].trim()), // defense
                    Integer.parseInt(values[k++].trim()), // health
                    values[k++].trim(),                   // name_Jap
                    pokemon.trim(),                       // name
                    Integer.parseInt(values[k++].trim()), // pokedex_Num
                    Integer.parseInt(values[k++].trim()), // attack_Sp
                    Integer.parseInt(values[k++].trim()), // defense_Sp
                    Integer.parseInt(values[k++].trim()), // speed
                    values[k++].trim(),                   // type_1
                    values.length > k ? values[k].trim() : null // type_2
            );

            int hash = PokeHash.hash(pokemanz);
            int i = 0;
            while (pokedex.table.get((hash + (i * i)) % PokeHash.TABLE_SIZE) != null) {
                i++;
            }
            pokedex.table.set((hash + (i * i)) % PokeHash.TABLE_SIZE, pokemanz);
            pokedex.count++;
            return true;

        } catch (Exception e) {
            System.out.println("There was an error in your inputs, please make sure you are following the correct input format.\n" +
                    "Attack,Classification,Defense,Health,Japaneese name,Pokedex Number,SP Attack,SP Defence,Speed,Type 1,Type 2\n" +
                    "Int   ,String        ,Int    ,Int   ,String        ,Int           ,Int      ,Int       ,Int  ,String,String\n" +
                    "Thank you!");
            // e.printStackTrace();
            return false;
        }
    }

    /**
     * Add a new entry based off of the object PokemanzInfo that is made when importing data from the .csv
     * @param entry The data that will be inserted.
     * @return true if insertion was successful
     */
    public boolean add(PokemanzInfo entry) {
        if (getLoadFactor() >= PokeHash.MAX_LOAD_FACTOR) {
            return false; // Too many pokemon, wait for the next game!
        }

        int hash = PokeHash.hash(entry);
        int i = 0;
        while (pokedex.table.get((hash + (i * i)) % PokeHash.TABLE_SIZE) != null) {
            i++;
        }
        pokedex.table.set((hash + (i * i)) % PokeHash.TABLE_SIZE, entry);
        pokedex.count++;
        return true;
    }

    public void deleteThread(String name) {
        Thread deleteThread = new Thread(() -> {
            if (delete(name.toLowerCase())) {
                System.out.println("Pokemon deleted.");
            } else {
                System.out.println("Failed to delete Pokemon, see reason above.");
            }
        });
        deleteThread.start();
        try {
            deleteThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove an entry.
     * @param pokemon  The key for the entry to remove.
     * @return true if this entry was deleted.
     */
    public boolean delete(String pokemon) {
        int hash = Math.abs(pokemon.hashCode()) % pokedex.TABLE_SIZE;
        int i = 0;
        while (pokedex.table.get((hash + i * i) % pokedex.TABLE_SIZE) != null) {
            if (pokedex.table.get((hash + i * i) % pokedex.TABLE_SIZE).name.equals(pokemon)) {
                pokedex.table.set((hash + i * i) % pokedex.TABLE_SIZE, null);
                pokedex.count--;
                return true;
            }
            i++;
        }
        return false;
    }

    /**
     *  Print the entire table in the format below:
     * 	KEY : ENTRY
     */
    public void printHT() {
        for (int i = 0; i < pokedex.TABLE_SIZE; i++) {
            if (pokedex.table.get(i) != null) {
                pokedex.table.get(i).printEntry();
            }
        }
    }

    /**
     * How full is this table?  Count how many entries you have and divide by the size of the table.
     * @return count / size
     */
    public double getLoadFactor() {
        return (double) pokedex.count / PokeHash.TABLE_SIZE;
    }

    /**
     * How full can this table be?
     * @return maximum load factor
     */
    public double getMaxLoadFactor() {
        return PokeHash.MAX_LOAD_FACTOR;
    }

    /**
     * The number of items in this table.
     * @return n
     */
    public int count() {
        return pokedex.count;
    }

    /**
     * The author of this particular Pokedex.
     */
    public void who() {
        System.out.println("This Pokedex was created by Lucian Rectanus.");
    }

    /**
     * Print all the instructions along with any helpful information.
     */
    public void help() {
        System.out.println("Available commands:");
        System.out.println("• add – insert an entry into the table");
        System.out.println("\tDetails input format.\n" +
                "\t\tAttack, Classification, Defense, Health, Japaneese name, Pokedex Number, SP Attack, SP Defence, Speed, Type 1, Type 2\n" +
                "\t\tInt   , String        , Int    , Int   , String        , Int           , Int      , Int       , Int  , String, String\n" +
                "\tThank you!");
        System.out.println("• delete – remove an entry");
        System.out.println("• find – If the user enters a pokemon name, print out its entry");
        System.out.println("• printHT – Print the entire table");
        System.out.println("• who – Print your name");
        System.out.println("• help, or ? – Print these commands");
        System.out.println("• clear – Clear the console");
        System.out.println("• exit, q, or x – Quit your program");
    }

    /**
     * Print an exit message and close out the Pokedex program.
     */
    public void exit() {
        System.out.println("Exiting Pokedex... Goodbye!");
    }

    /**
     * Creates a thread of clearConsole x amount of times
     */

    public void clearConsoleThread(int amount) {
        for (int i = 0; i < amount; i++) {
            Thread clearConsoleThread = new Thread(this::clearConsole);
            clearConsoleThread.start();
        }
    }

    /**
     * Clear the console (Really just pushes everything up)
     */
    public void clearConsole() {
        isReady = false;

        for (int i = 0; i < 50; i++) {
            System.out.println();
        }

        isReady = true;
    }
}