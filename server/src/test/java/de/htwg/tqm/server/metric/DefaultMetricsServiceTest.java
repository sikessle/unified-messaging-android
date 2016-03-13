package de.htwg.tqm.server.metric;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


public class DefaultMetricsServiceTest {

    @Test
    public void testCategoryOfIssue() throws Exception {
        double thresholdOK = 2.0;
        double thresholdWARN = 4.0;

        DefaultMetricsService sut = new DefaultMetricsService(thresholdOK, thresholdWARN, 0, 0);

        assertThat(sut.categoryOfIssue(thresholdOK - 1.0), equalTo(MetricsService.Category.OK));
        assertThat(sut.categoryOfIssue(thresholdOK), equalTo(MetricsService.Category.OK));
        assertThat(sut.categoryOfIssue(thresholdWARN), equalTo(MetricsService.Category.WARN));
        assertThat(sut.categoryOfIssue(thresholdWARN + 1), equalTo(MetricsService.Category.CRITICAL));
    }

    @Test
    public void testCategoryOfDeveloper() throws Exception {
        int thresholdOK = 1;
        int thresholdWARN = 2;

        DefaultMetricsService sut = new DefaultMetricsService(0.0, 0.0, 1, 2);

        assertThat(sut.categoryOfUser(thresholdOK - 1), equalTo(MetricsService.Category.OK));
        assertThat(sut.categoryOfUser(thresholdOK), equalTo(MetricsService.Category.OK));
        assertThat(sut.categoryOfUser(thresholdWARN), equalTo(MetricsService.Category.WARN));
        assertThat(sut.categoryOfUser(thresholdWARN + 1), equalTo(MetricsService.Category.CRITICAL));
    }
}