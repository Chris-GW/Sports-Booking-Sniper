package de.chrisgw.sportsbookingsniper.buchung;

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;


@Data
public class SportBuchungsBestaetigung {

    private SportBuchungsJob buchungsJob;

    private String buchungsNummer;
    private String buchungsBestaetigungUrl;
    private byte[] screenshotBytes;


    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE).append(buchungsJob.getJobId())
                .append("timestamp", buchungsJob.lastSportBuchungsVersuch().getTimestamp())
                .append("sportTermin", buchungsJob.getSportTermin())
                .append("buchungsNummer", buchungsNummer)
                .toString();
    }

}
