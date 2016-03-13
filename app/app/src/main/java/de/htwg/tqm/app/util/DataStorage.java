package de.htwg.tqm.app.util;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import de.htwg.tqm.app.model.JiraUser;
import de.htwg.tqm.app.model.Dialog;
import de.htwg.tqm.app.model.JiraIssue;
import de.htwg.tqm.app.model.JiraProject;

/**
 * Data cache for all app data (JIRA issues, developers, dialogs and projects).
 */
public class DataStorage {

    private HashMap<String, JiraIssue> issues;
    private HashMap<String, JiraUser> developers;
    private HashMap<Long, Dialog> dialogs;
    private HashMap<String, JiraProject> projects;

    private DataStorage() {
        this.issues = new HashMap<>();
        this.developers = new HashMap<>();
        this.dialogs = new HashMap<>();
        this.projects = new HashMap<>();
    }

    private static class DataStorageHolder {
        private static final DataStorage INSTANCE = new DataStorage();
    }

    public static DataStorage getInstance() {
        return DataStorageHolder.INSTANCE;
    }

    public void addIssue(final JiraIssue issue) {
        this.issues.put(issue.getKey(), issue);
    }

    public void addIssues(final Set<JiraIssue> issues) {
        for (JiraIssue issue : issues) {
            this.addIssue(issue);
        }
    }

    public void addDeveloper(final JiraUser jiraUser) {
        this.developers.put(jiraUser.getName(), jiraUser);
    }

    public void addDevelopers(final Set<JiraUser> jiraUsers) {
        for (JiraUser jiraUser : jiraUsers) {
            this.addDeveloper(jiraUser);
        }
    }

    public void addDialog(final Dialog dialog) {
        this.dialogs.put(dialog.getDialogID(), dialog);
    }

    public void addDialogs(final List<Dialog> dialogs) {
        for (Dialog dialog : dialogs) {
            this.addDialog(dialog);
        }
    }

    public void addProjects(final List<JiraProject> projects) {
        for (JiraProject project : projects) {
            this.projects.put(project.getKey(), project);
        }
    }

    public ArrayList<JiraIssue> getIssues() {
        return new ArrayList<>(this.issues.values());
    }

    public JiraIssue getIssue(final String key) {
        return this.issues.get(key);
    }

    public ArrayList<JiraUser> getUsers() {
        return new ArrayList<>(this.developers.values());
    }

    public JiraUser getDeveloper(final String key) {
        return this.developers.get(key);
    }

    public ArrayList<Dialog> getDialogs() {
        return new ArrayList<>(this.dialogs.values());
    }

    public Dialog getDialog(final long dialogKey) {
        return this.dialogs.get(dialogKey);
    }

    public ArrayList<JiraProject> getProjects() {
        return new ArrayList<>(this.projects.values());
    }

    public ArrayList<Float> getDeveloperHistory(final String developerKey) {
        ArrayList<Float> values = new ArrayList<>();
        values.add(1.0f);
        values.add(2.0f);
        values.add(5.0f);
        values.add(2.0f);
        values.add(1.0f);

        return values;
    }

    public ArrayList<Float> getIssueHistory(final String issueKey) {
        ArrayList<Float> values = new ArrayList<>();
        values.add(0.8f);
        values.add(2.3f);
        values.add(3.3f);
        values.add(6.2f);
        values.add(0.1f);

        return values;
    }

    public void resetIssues() {
        this.issues.clear();
    }

    public void resetDevelopers() {
        this.developers.clear();
    }

    public void resetDialogs() {
        this.dialogs.clear();
    }

    public void resetProjects() {
        this.projects.clear();
    }

    private void storeData() {

        ObjectContainer db = Db4oEmbedded.openFile(Db4oEmbedded
                .newConfiguration(), "DataStorage_DB");
        try {
            db.store(this.issues);
            db.store(this.developers);
            db.store(this.dialogs);
            db.store(this.projects);
            db.commit();
        } finally {
            db.close();
        }
    }

    private void retrieveData() {
        ObjectContainer db = Db4oEmbedded.openFile(Db4oEmbedded
                .newConfiguration(), "DataStorage_DB");
        try {

        } finally {
            db.close();
        }
    }
}
