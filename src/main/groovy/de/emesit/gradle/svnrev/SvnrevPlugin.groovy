package de.emesit.gradle.svnrev

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectState
import org.gradle.api.Task
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.bundling.Jar
import org.tmatesoft.svn.core.SVNDepth
import org.tmatesoft.svn.core.SVNException
import org.tmatesoft.svn.core.wc.ISVNStatusHandler
import org.tmatesoft.svn.core.wc.SVNClientManager
import org.tmatesoft.svn.core.wc.SVNRevision
import org.tmatesoft.svn.core.wc.SVNStatus
import org.tmatesoft.svn.core.wc.SVNStatusClient
import org.tmatesoft.svn.core.wc.SVNStatusType
import org.tmatesoft.svn.core.wc.SVNWCUtil

class SvnrevPlugin implements Plugin<Project> {
    
    void apply(Project project) {
        SvnrevPluginConvention convention = new SvnrevPluginConvention()
        project.convention.plugins.svnrev = convention

        Task svnrevInfoTask = project.task('svnrev-info') {
            group = BasePlugin.BUILD_GROUP
            description = "collects and shows subversion infos of this project"
            doFirst { task ->
                BuildInfoTemplateEngine templateEngine = executeTemplateEngine(task, convention)
                logFileTemplate(task, "this is the svnrev-info task, but the svnrev task (if called) would write file '"+templateEngine.filename+"', with the following contents:", templateEngine.lastConversion)
            }
        }
        Task svnrevTask = project.task('svnrev') {
            group = BasePlugin.BUILD_GROUP
            description = "Creates a properties file with subversion infos of this project";
            doFirst { task ->
                File outputDir = project.convention.plugins?.java?.sourceSets?.main?.output?.classesDir ?: new File('.')

                if (!outputDir.exists()) {
                    outputDir.mkdirs()
                }
                if (!outputDir.directory) {
                    throw new GradleException("$outputDir.absolutePath is not a directory")
                }

                BuildInfoTemplateEngine templateEngine = executeTemplateEngine(task, convention)

                logFileTemplate(task, "writing file '"+templateEngine.filename+"', with the following contents:", templateEngine.lastConversion)

                new File(outputDir, templateEngine.filename).withWriter { w ->
                    w << templateEngine.lastConversion
                }
            }
        }

        project.configure(project) {
            gradle.afterProject { Project theProject, ProjectState projectState ->
                for (Task nextTask in theProject.tasks.withType(Jar.class)) {
                    // War extends Jar, Ear extends Jar
                    nextTask.dependsOn << svnrevTask
                }
            }
        }
    }

    private static BuildInfoTemplateEngine executeTemplateEngine(Task task, SvnrevPluginConvention convention) {
        BuildInfoTemplateEngine templateEngine = createBuildInfoBinding(task, convention)

        task.getLogger().debug "svnrev plugin: binding used for all templates: ${templateEngine.binding}"
        task.getLogger().debug "svnrev plugin: using buildInfoConsoleTemplate: ${convention.consoleTemplate}"

        logConsoleTemplate(task, templateEngine, convention)
        
        task.getLogger().debug "svnrev plugin: converting buildInfoFileTemplate: ${convention.fileTemplate}"
        templateEngine.convert(convention.fileTemplate)
        return templateEngine
    }

    private static BuildInfoTemplateEngine createBuildInfoBinding(Task task, SvnrevPluginConvention convention) {
        BuildInfo buildInfo = new BuildInfo(projectName:task.project.name, projectVersion:task.project.version)
        if (SVNWCUtil.isVersionedDirectory(new File('.'))) {
            fillBuildInfo(buildInfo, new File('.'), SVNClientManager.newInstance().statusClient)
        }

        BuildInfoTemplateEngine templateEngine = new BuildInfoTemplateEngine()
        templateEngine.initBinding(convention.projectNamePrefix, task.project, buildInfo, convention.filenameTemplate, convention.dateTimeFormat, convention.locale, convention.revisionPrefix, convention.modifiedSuffix)
        return templateEngine
    }

    private static void fillBuildInfo(final BuildInfo buildInfo, File workingCopyRoot, SVNStatusClient statusClient) {
        ISVNStatusHandler handler = new ISVNStatusHandler() {
                    @Override
                    public void handleStatus(SVNStatus status) throws SVNException {
                        if (status.revision != SVNRevision.UNDEFINED && status.revision.number > 0L) {
                            buildInfo.revisionNumber = status.revision.number
                        }
                        if (!buildInfo.locallyModified) {
                            buildInfo.locallyModified = isModified(status)
                        }
                        buildInfo.lastCommitDateTime = status.committedDate
                    }
                }
        try {
            boolean remote = false
            boolean reportAll = true
            boolean includeIgnored = false
            boolean collectParentExternals = false

            statusClient.doStatus(workingCopyRoot, SVNRevision.HEAD, SVNDepth.INFINITY, remote, reportAll, includeIgnored, collectParentExternals, handler, [])
        } catch (SVNException exc) {
            throw new SvnRevException(exc)
        }
    }

    static boolean isModified(SVNStatus status) {
        if (status.locked) {
            return true
        }
        if (status.contentsStatus == SVNStatusType.STATUS_MODIFIED) {
            return true
        }
        if (status.contentsStatus == SVNStatusType.STATUS_CONFLICTED) {
            return true
        }
        if (status.propertiesStatus == SVNStatusType.STATUS_MODIFIED) {
            return true
        }
        if (status.propertiesStatus == SVNStatusType.STATUS_CONFLICTED) {
            return true
        }
        return false
    }
    
    
    private static String toValidLogLevel(String logLevel, String defaultLogLevel) {
        return ['debug', 'info', 'lifecycle'].find { it == logLevel } ?: defaultLogLevel
    }
    private static String getLogLevelConsoleTemplate() {
        return toValidLogLevel(System.properties['svnrev.loglevel.consoleTemplate'], 'lifecycle')
    }
    private static String getLogLevelFileTemplate() {
        return toValidLogLevel(System.properties['svnrev.loglevel.fileTemplate'], 'info')
    }
    private static void logConsoleTemplate(Task task, BuildInfoTemplateEngine templateEngine, SvnrevPluginConvention convention) {
        task.getLogger()."${getLogLevelConsoleTemplate()}" templateEngine.convert(convention.consoleTemplate)
    }
    private static void logFileTemplate(Task task, String message, String conversion) {
        task.getLogger()."${getLogLevelFileTemplate()}" "  ==> $message"
        conversion.eachLine {
            task.getLogger()."${getLogLevelFileTemplate()}" "    "+it
        }
   }
}