package de.htwg.tqm.server.resource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
final class DialogCreateRequestBean {
    private final String subject;
    private final long violationID;
    private final String initiator;
    private final String affected;

    @JsonCreator
    public DialogCreateRequestBean(@NotNull @JsonProperty("subject") String subject,
                                   @JsonProperty("violationID") long violationID,
                                   @NotNull @JsonProperty("initiator") String initiator,
                                   @NotNull @JsonProperty("affected") String affected) {
        this.subject = subject;
        this.violationID = violationID;
        this.initiator = initiator;
        this.affected = affected;
    }

    public @NotNull String getSubject() {
        return subject;
    }

    public long getViolationID() {
        return violationID;
    }

    public @NotNull String getInitiator() {
        return initiator;
    }


    public @NotNull String getAffected() {
        return affected;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DialogCreateRequestBean that = (DialogCreateRequestBean) o;

        if (violationID != that.violationID) return false;
        if (!subject.equals(that.subject)) return false;
        if (!initiator.equals(that.initiator)) return false;
        return affected.equals(that.affected);

    }

    @Override
    public int hashCode() {
        int result = subject.hashCode();
        result = 31 * result + (int) (violationID ^ (violationID >>> 32));
        result = 31 * result + initiator.hashCode();
        result = 31 * result + affected.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DialogCreateRequestBean{" +
                "subject='" + subject + '\'' +
                ", violationID=" + violationID +
                ", initiator='" + initiator + '\'' +
                ", affected='" + affected + '\'' +
                '}';
    }
}
