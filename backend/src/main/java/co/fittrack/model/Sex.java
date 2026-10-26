package co.fittrack.model;

/** Biological sex — only used as the constant term in the Mifflin–St Jeor BMR equation. */
public enum Sex {
    MALE(5),
    FEMALE(-161);

    private final int bmrConstant;

    Sex(int bmrConstant) {
        this.bmrConstant = bmrConstant;
    }

    public int bmrConstant() {
        return bmrConstant;
    }
}
