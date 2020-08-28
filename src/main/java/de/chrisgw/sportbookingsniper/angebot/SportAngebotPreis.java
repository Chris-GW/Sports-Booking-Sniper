package de.chrisgw.sportbookingsniper.angebot;

import de.chrisgw.sportbookingsniper.buchung.TeilnehmerKategorie;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigInteger;
import java.util.function.BinaryOperator;


@Data
@AllArgsConstructor
public class SportAngebotPreis {

    private final BigInteger preisStudierende;
    private final BigInteger preisMitarbeiter;
    private final BigInteger preisExterne;
    private final BigInteger preisAlumni;


    public SportAngebotPreis() {
        this(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO);
    }

    public SportAngebotPreis(int preis) {
        this(preis, preis, preis, preis);
    }

    public SportAngebotPreis(long preisStudierende, long preisMitarbeiter, long preisExterne, long prei1sAlumni) {
        this(BigInteger.valueOf(preisStudierende), BigInteger.valueOf(preisMitarbeiter),
                BigInteger.valueOf(preisExterne), BigInteger.valueOf(prei1sAlumni));
    }


    public boolean isPaymentRequierd() {
        return preisStudierende.compareTo(BigInteger.ZERO) > 0 || preisMitarbeiter.compareTo(BigInteger.ZERO) > 0
                || preisExterne.compareTo(BigInteger.ZERO) > 0 || preisAlumni.compareTo(BigInteger.ZERO) > 0;
    }

    public boolean isPaymentRequierd(TeilnehmerKategorie teilnehmerKategorie) {
        return preisFor(teilnehmerKategorie).compareTo(BigInteger.ZERO) > 0;
    }


    public BigInteger preisFor(TeilnehmerKategorie teilnehmerKategorie) {
        switch (teilnehmerKategorie) {
        case STUDENT_FH:
        case STUDENT_RWTH:
        case STUDENT_NRW:
        case STUDENT_ANDERE_HOCHSCHULE:
            return preisStudierende;

        case AZUBI_RWTH_UKA:
        case SCHUELER:
            return preisStudierende;

        case RWTH_ALUMI:
            return preisAlumni;

        case MITARBEITER_RWTH:
        case MITARBEITER_FH:
        case MITARBEITER_KLINIKUM:
            return preisMitarbeiter;

        case EXTERN:
            return preisExterne;

        default:
            throw new IllegalArgumentException("Could not find Preis for " + teilnehmerKategorie);
        }
    }


    public SportAngebotPreis add(SportAngebotPreis otherPreis) {
        return combine(otherPreis, BigInteger::add);
    }

    public SportAngebotPreis subtract(SportAngebotPreis otherPreis) {
        return combine(otherPreis, BigInteger::subtract);
    }


    public SportAngebotPreis multiply(SportAngebotPreis otherPreis) {
        return combine(otherPreis, BigInteger::multiply);
    }

    public SportAngebotPreis divide(SportAngebotPreis otherPreis) {
        return combine(otherPreis, BigInteger::divide);
    }


    public SportAngebotPreis combine(SportAngebotPreis otherPreis, BinaryOperator<BigInteger> operator) {
        BigInteger preisStudierende = operator.apply(this.getPreisStudierende(), otherPreis.getPreisStudierende());
        BigInteger preisMitarbeiter = operator.apply(this.getPreisMitarbeiter(), otherPreis.getPreisMitarbeiter());
        BigInteger preisExterne = operator.apply(this.getPreisExterne(), otherPreis.getPreisExterne());
        BigInteger preisAlumni = operator.apply(this.getPreisAlumni(), otherPreis.getPreisAlumni());
        return new SportAngebotPreis(preisStudierende, preisMitarbeiter, preisExterne, preisAlumni);
    }

}
