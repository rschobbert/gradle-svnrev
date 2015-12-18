package de.emesit.gradle.svnrev;


/**
 * Class to hold the gathered information.
 * 
 * @author Ruediger Schobbert, emesit GmbH & Co. KG
 */
class BuildInfo {
    String  projectName
    String  projectVersion
    Date    buildDateTime = new Date()
    Long    revisionNumber
    boolean locallyModified
    Date    commitDateTime

    void setLastCommitDateTime(Date date) {
        if (commitDateTime == null || (date != null && commitDateTime.before(date))) {
            commitDateTime = date
        }
    }
}
