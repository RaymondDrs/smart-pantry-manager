package za.ac.richfield.smartpantry;

import java.util.Objects;

/**
 * A single row of the pantry_items table: an ingredient the user physically
 * has at home, with a quantity, a unit and (optionally) an expiry date.
 */
public class PantryItem {

    private long id;
    private String name;      // canonical name, e.g. "tomato"
    private double quantity;  // always stored in the item's own unit
    private String unit;      // one of the Unit enum's codes (see Unit.java)
    private Long expiryDate;  // epoch millis; null means "no expiry set"

    public PantryItem(long id, String name, double quantity, String unit, Long expiryDate) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    /** Creates a brand-new (not yet saved) pantry item. */
    public PantryItem(String name, double quantity, String unit, Long expiryDate) {
        this(-1, name, quantity, unit, expiryDate);
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public Long getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Long expiryDate) { this.expiryDate = expiryDate; }

    /** True when the item expires within the given number of days from now. */
    public boolean isExpiringSoon(int withinDays) {
        if (expiryDate == null) return false;
        long now = System.currentTimeMillis();
        long horizon = now + withinDays * 24L * 60L * 60L * 1000L;
        return expiryDate <= horizon && expiryDate >= now - 24L * 60L * 60L * 1000L;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PantryItem)) return false;
        PantryItem that = (PantryItem) o;
        return id == that.id
                && Double.compare(that.quantity, quantity) == 0
                && Objects.equals(name, that.name)
                && Objects.equals(unit, that.unit)
                && Objects.equals(expiryDate, that.expiryDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, quantity, unit, expiryDate);
    }

    @Override
    public String toString() {
        return "PantryItem{" + name + ", qty=" + quantity + " " + unit
                + (expiryDate == null ? "" : ", expires=" + expiryDate) + "}";
    }
}
