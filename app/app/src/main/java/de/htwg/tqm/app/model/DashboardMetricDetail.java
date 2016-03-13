package de.htwg.tqm.app.model;

/**
 * Dashboard metric detail containing information about metric.
 * (Number of cases that are OK (metric conform), WARN and CRITICAL (not metric conform)
 */
public class DashboardMetricDetail implements DashboardDetail {

    // Need to be floats/don't have to be doubles because chart library can only process floats
    private final float ok;
    private final float warn;
    private final float critical;

    public DashboardMetricDetail(final float ok, final float warn, final float critical) {
        this.ok = ok;
        this.warn = warn;
        this.critical = critical;
    }

    public float getOk() {
        return this.ok;
    }

    public float getWarn() {
        return this.warn;
    }

    public float getCritical() {
        return this.critical;
    }
}
