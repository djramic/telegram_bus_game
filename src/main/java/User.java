import java.util.ArrayList;

public class User {
    int id;
    String name;
    ArrayList<String> numbers;
    int numbersCount;
    int drinksCount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumbersCount() {
        return numbersCount;
    }

    public void setNumbersCount(int numbersCount) {
        this.numbersCount = numbersCount;
    }

    public int getDrinksCount() {
        return drinksCount;
    }

    public void setDrinksCount(int drinksCount) {
        this.drinksCount = drinksCount;
    }

    public User(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public ArrayList<String> getNumbers() {
        return numbers;
    }

    public void setNumbers(ArrayList<String> numbers) {
        this.numbers = new ArrayList<>(numbers);
    }
}
