package model;

/**
 * Created by steve on 05/11/2015.
 */
public class counterWeightPair {
    private int counter;
    private double weight;

    public counterWeightPair (int counter, double weight) {
        this.counter = counter;
        this.weight = weight;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}
