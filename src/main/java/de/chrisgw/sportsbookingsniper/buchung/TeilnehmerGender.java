package de.chrisgw.sportsbookingsniper.buchung;

public enum TeilnehmerGender {

    KEINE_ANGABE("X", "keine Angabe"), //
    MALE("M", "männlich"), //
    FEMALE("W", "weiblich"), //
    DIVERS("D", "divers"), //
    ;

    private final String shortName;
    private final String name;


    TeilnehmerGender(String shortName, String name) {
        this.shortName = shortName;
        this.name = name;
    }


    public static TeilnehmerGender fromShortname(String shortName) {
        for (TeilnehmerGender gender : TeilnehmerGender.values()) {
            if (gender.getShortName().equalsIgnoreCase(shortName)) {
                return gender;
            }
        }
        throw new IllegalArgumentException(shortName + " is not a valid shortname for a Gender");
    }

    public static TeilnehmerGender fromName(String shortName) {
        for (TeilnehmerGender gender : TeilnehmerGender.values()) {
            if (gender.getName().equalsIgnoreCase(shortName)) {
                return gender;
            }
        }
        throw new IllegalArgumentException(shortName + " is not a valid shortname for a Gender");
    }


    public String getShortName() {
        return shortName;
    }

    public String getName() {
        return name;
    }


    @Override
    public String toString() {
        return this.name;
    }

}
