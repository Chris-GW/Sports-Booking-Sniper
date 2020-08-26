package de.chrisgw.sportbooking.model;

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.math.BigInteger;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;


@Data
public class SportBuchungsBestaetigung {

    private SportBuchungsJob buchungsJob;

    private String buchungsNummer;
    private String buchungsBestaetigungUrl;
    private byte[] buchungsBestaetigung;


    public BigInteger getPreis() {
        TeilnehmerAngaben teilnehmerAngaben = buchungsJob.getTeilnehmerAngaben();
        TeilnehmerKategorie teilnehmerKategorie = teilnehmerAngaben.getTeilnehmerKategorie();
        SportAngebot sportAngebot = buchungsJob.getSportAngebot();
        return sportAngebot.preisFor(teilnehmerKategorie);
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE).append(buchungsJob.getJobId())
                .append("timestamp", buchungsJob.lastSportBuchungsVersuch().getTimestamp())
                .append("sportTermin", buchungsJob.getSportTermin())
                .append("buchungsNummer", buchungsNummer)
                .toString();
    }

}
