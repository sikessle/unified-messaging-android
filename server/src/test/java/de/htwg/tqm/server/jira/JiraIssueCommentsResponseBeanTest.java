package de.htwg.tqm.server.jira;

import de.htwg.tqm.server.beans.AbstractBeanTest;
import org.jetbrains.annotations.NotNull;

public class JiraIssueCommentsResponseBeanTest extends AbstractBeanTest {

    @Override
    protected Object createSut() {
        return createJiraIssueCommentsResponseBean();
    }

    public static @NotNull JiraIssueCommentsResponseBean createJiraIssueCommentsResponseBean() {
        JiraIssueCommentsResponseBean.GenericFields fields = new JiraIssueCommentsResponseBean.GenericFields();
        fields.comment = new JiraIssueCommentsResponseBean.GenericFields();
        JiraIssueCommentsResponseBean.GenericFields singleComment = new JiraIssueCommentsResponseBean.GenericFields();
        singleComment.author = new JiraIssueCommentsResponseBean.GenericFields();
        singleComment.author.name = "sikessle";
        singleComment.created = "2015-12-09T15:22:23.679+0100";
        singleComment.body = "is closed?";
        fields.comment.comments = new JiraIssueCommentsResponseBean.GenericFields[] {
                singleComment
        };

        return new JiraIssueCommentsResponseBean(fields);
    }

    @Override
    protected String getJsonToDeserialize() throws Exception {
        String path = getClass().getResource("/single-issue-response.json").getPath();
        return readFile(path);
    }

}