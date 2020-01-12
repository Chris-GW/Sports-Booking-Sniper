package de.chrisgw.sportbooking.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


public class SportBuchungsBestaetigung {

    private long jobId;
    private SportTermin sportTermin;
    private PersonenAngaben personenAngaben;

    private String buchungsNummer;
    private String buchungsBestaetigungUrl;
    private byte[] buchungsBestaetigung;


    public SportBuchungsBestaetigung() {

    }


    public long getJobId() {
        return jobId;
    }

    public void setJobId(long jobId) {
        this.jobId = jobId;
    }


    public SportTermin getSportTermin() {
        return sportTermin;
    }

    public void setSportTermin(SportTermin sportTermin) {
        this.sportTermin = sportTermin;
    }


    public PersonenAngaben getPersonenAngaben() {
        return personenAngaben;
    }

    public void setPersonenAngaben(PersonenAngaben personenAngaben) {
        this.personenAngaben = personenAngaben;
    }


    public String getBuchungsNummer() {
        return buchungsNummer;
    }

    public void setBuchungsNummer(String buchungsNummer) {
        this.buchungsNummer = buchungsNummer;
    }


    public byte[] getBuchungsBestaetigung() {
        return buchungsBestaetigung;
    }

    public void setBuchungsBestaetigung(byte[] buchungsBestaetigung) {
        this.buchungsBestaetigung = buchungsBestaetigung;
    }


    public String getBuchungsBestaetigungUrl() {
        return buchungsBestaetigungUrl;
    }

    public void setBuchungsBestaetigungUrl(String buchungsBestaetigungUrl) {
        this.buchungsBestaetigungUrl = buchungsBestaetigungUrl;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (!(o instanceof SportBuchungsBestaetigung))
            return false;

        SportBuchungsBestaetigung that = (SportBuchungsBestaetigung) o;
        return new EqualsBuilder().append(jobId, that.jobId)
                .append(sportTermin, that.sportTermin)
                .append(buchungsNummer, that.buchungsNummer)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(jobId).append(sportTermin).append(buchungsNummer).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append(jobId)
                .append("sportTermin", sportTermin)
                .append("buchungsNummer", buchungsNummer)
                .append("buchungsBestaetigungUrl", buchungsBestaetigungUrl)
                .toString();
    }

}
