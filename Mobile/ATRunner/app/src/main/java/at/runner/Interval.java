package at.runner;

/**
 * An interval that captures what should happen within a specific period of time.
 */
public class Interval implements Comparable {

    /**
     * Gets or sets the starting hour for the interval.
     */
    public int StartHour;

    /**
     * Gets or sets the starting minute, which should be mod 5, for the interval.
     */
    public int StartMinute;

    /**
     * Gets or sets the weight of processing that should be done within the interval.
     */
    public IntervalWeight Weight;

    /**
     * Constructs a new interval.
     * @param startHour The starting hour for the interval.
     * @param startMinute The starting minute, which should be mod 5, for the interval.
     * @param weight The weight of processing that should be done within the interval.
     */
    public Interval(int startHour, int startMinute, IntervalWeight weight) {
        StartMinute = startMinute;
        StartHour = startHour;
        Weight = weight;
    }

    /**
     * Compares the current Interval to another Interval for sorting purposes.
     * @param item The Interval to compare this interval to.
     * @return -1 is returned if the current Interval is less than the specified Interval, 0 is returned if the current Interval is equal to the specified Interval, and 1 is returned for all other cases.
     */
    @Override
    public int compareTo(Object item) {
        Interval compareItem = (Interval)item;
        if (compareItem.StartHour > StartHour) {
            return -1;
        }

        if (compareItem.StartHour == StartHour && compareItem.StartMinute > StartMinute) {
            return -1;
        }

        if (compareItem.StartHour == StartHour && compareItem.StartMinute == StartMinute) {
            return 0;
        }

        return 1;
    }

    /**
     * Returns the string representation of this class.
     * @return the string representation of this class is returned;
     */
    @Override
    public String toString() {
        String weight = "";
        switch (Weight)
        {
            case Heavy:
                weight = "Heavy";
                break;
            case Light:
                weight = "Light";
                break;
            case Medium:
                weight = "Medium";
                break;
        }
        return String.format("[StartHour=%d, StartMinute=%d, Weight=%s]", StartHour, StartMinute, weight);
    }
}