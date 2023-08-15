package wiki.dawn.newfishsystem;

public class ItemInfo {
    private String itemId;
    private double probability;

    public ItemInfo(String itemId, double probability) {
        this.itemId = itemId;
        this.probability = probability;
    }

    public String getItemId() {
        return itemId;
    }

    public double getProbability() {
        return probability;
    }
}
