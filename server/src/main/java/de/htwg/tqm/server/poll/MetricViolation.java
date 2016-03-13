package de.htwg.tqm.server.poll;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jetbrains.annotations.NotNull;

final class MetricViolation {

    private final String causer;
    private final ObjectNode notificationContent;
    private final String violationName;

    public MetricViolation(@NotNull String violationName, @NotNull String causer, @NotNull ObjectNode notificationContent) {
        this.causer = causer;
        this.violationName = violationName;
        this.notificationContent = notificationContent;
    }
    public @NotNull String getCauser() {
        return causer;
    }

    public @NotNull  String getViolationName() {
        return violationName;
    }

    public @NotNull ObjectNode getNotificationContent() {
        return notificationContent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MetricViolation that = (MetricViolation) o;

        if (!causer.equals(that.causer)) return false;
        if (!notificationContent.equals(that.notificationContent)) return false;
        return violationName.equals(that.violationName);

    }

    @Override
    public int hashCode() {
        int result = causer.hashCode();
        result = 31 * result + notificationContent.hashCode();
        result = 31 * result + violationName.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "MetricViolation{" +
                "causer='" + causer + '\'' +
                ", notificationContent=" + notificationContent +
                ", violationName='" + violationName + '\'' +
                '}';
    }
}
