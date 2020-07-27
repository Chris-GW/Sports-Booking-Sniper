package de.chrisgw.sportbooking.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import static de.chrisgw.sportbooking.model.Semester.SemesterType.SOMER_SEMESTER;
import static de.chrisgw.sportbooking.model.Semester.SemesterType.WINTER_SEMESTER;


@Data
public class Semester implements Comparable<Semester> {

    public enum SemesterType {
        SOMER_SEMESTER, WINTER_SEMESTER;
    }


    private final int year;
    private final SemesterType type;

    @JsonCreator
    public Semester(@JsonProperty("year") int year, @JsonProperty("type") SemesterType type) {
        this.year = year;
        this.type = type;
    }


    public static Semester newWinterSemester(int year) {
        return new Semester(year, WINTER_SEMESTER);
    }

    public static Semester newSommerSemester(int year) {
        return new Semester(year, SOMER_SEMESTER);
    }


    @Override
    public int compareTo(Semester other) {
        int compare = Integer.compare(this.year, other.year);
        if (compare == 0) {
            compare = this.type.compareTo(other.type);
        }
        return compare;
    }


    public String toString() {
        int shortYear = year / 100;
        switch (type) {
        case SOMER_SEMESTER:
            return "SS " + shortYear;
        case WINTER_SEMESTER:
            int wsEndYear = shortYear + 1;
            return "WS " + shortYear + "-" + wsEndYear;
        default:
            throw new IllegalStateException("Unknown SemesterType: " + type);
        }
    }

}
