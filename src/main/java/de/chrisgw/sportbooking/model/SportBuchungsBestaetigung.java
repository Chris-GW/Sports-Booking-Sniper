package de.chrisgw.sportbooking.model;

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.math.BigInteger;
import java.time.LocalDateTime;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;


@Data
public class SportBuchungsBestaetigung {

    private long jobId;
    private LocalDateTime timestamp = LocalDateTime.now();
    private SportTermin sportTermin;
    private PersonenAngaben personenAngaben;

    private String buchungsNummer;
    private String buchungsBestaetigungUrl;
    private byte[] buchungsBestaetigung;


    public BigInteger getPreis() {
        PersonKategorie personKategorie = personenAngaben.getPersonKategorie();
        SportAngebot sportAngebot = sportTermin.getSportAngebot();
        return sportAngebot.preisFor(personKategorie);
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE).append(jobId)
                .append("timestamp", timestamp)
                .append("sportTermin", sportTermin)
                .append("buchungsNummer", buchungsNummer)
                .toString();
    }

}
