import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class PokemanzInfo {

    int attack;
    String classification;
    int defense;
    int health;
    String name_Jap;
    String name;
    int pokedex_Num;
    int attack_Sp;
    int defense_Sp;
    int speed;
    String type_1;
    String type_2;

    public PokemanzInfo(int attack, String classification, int defense, int health, String nameJap, String name, int pokedexNum, int attackSp, int defenseSp, int speed, String type1, String type2) {
        this.attack = attack;
        this.classification = classification;
        this.defense = defense;
        this.health = health;
        this.name_Jap = nameJap;
        this.name = name;
        this.pokedex_Num = pokedexNum;
        this.attack_Sp = attackSp;
        this.defense_Sp = defenseSp;
        this.speed = speed;
        this.type_1 = type1;
        this.type_2 = type2;
    }

    public void printEntry() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.name).append("\n");
        sb.append(String.format("[%4d] %s | %s%n", this.pokedex_Num, this.name, this.name_Jap));
        sb.append(this.name).append(" is a ").append(this.classification).append(" of type ").append(this.type_1);
        if (this.type_2 != null) sb.append(" and ").append(this.type_2);
        sb.append("\n\nHere are the stats of ").append(this.name).append("\n");
        sb.append(String.format("%-12s %-12s %-12s %-12s %-12s %-12s%n",
                "Attack", "Defense", "Health", "AttackSp", "DefenseSp", "Speed"));
        sb.append(String.format("%-12d %-12d %-12d %-12d %-12d %-12d%n",
                this.attack, this.defense, this.health, this.attack_Sp, this.defense_Sp, this.speed));
        sb.append("\n");
        System.out.println(sb.toString());
    }

    public void printEntryOld() {
        System.out.println(this.name);
        System.out.printf("[%4d] %s | %s%n", this.pokedex_Num, this.name, this.name_Jap);

        System.out.print(this.name + " is a " + this.classification + " of type " + this.type_1);
        if (this.type_2 != null) System.out.print(" and " + this.type_2);
        System.out.println();

        System.out.println("\nHere are the stats of " + this.name);
        this.printIntValues();
        System.out.println("\n");
    }

    public void printIntValues() {
        System.out.printf("%-12s %-12s %-12s %-12s %-12s %-12s%n",
                "Attack", "Defense", "Health", "AttackSp", "DefenseSp", "Speed");
        System.out.printf("%-12d %-12d %-12d %-12d %-12d %-12d%n",
                this.attack, this.defense, this.health, this.attack_Sp, this.defense_Sp, this.speed);
    }

    public static ArrayList<PokemanzInfo> getData() {
        // Temp list to parse file
        ArrayList<PokemanzInfo> pokemanzList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(Main.FILE_DATA_NAME))) {
            String line;
            line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");

                // Making a new instance pf PokemanzInfo with the values from the csv file
                PokemanzInfo pokemanz = new PokemanzInfo(
                        Integer.parseInt(values[0]), // attack
                        values[1],                   // classification
                        Integer.parseInt(values[2]), // defense
                        Integer.parseInt(values[3]), // health
                        values[4],                   // name_Jap
                        values[5].toLowerCase(),     // name
                        Integer.parseInt(values[6]), // pokedex_Num
                        Integer.parseInt(values[7]), // attack_Sp
                        Integer.parseInt(values[8]), // defense_Sp
                        Integer.parseInt(values[9]), // speed
                        values[10],                  // type_1
                        values.length > 11 ? values[11] : null // type_2
                );
                pokemanzList.add(pokemanz);
            }
        } catch (IOException e) {
            System.out.println("Whoah nelly, somethin' aint right!");
            e.getCause();
        }

        // returning
        return pokemanzList;
    }
}