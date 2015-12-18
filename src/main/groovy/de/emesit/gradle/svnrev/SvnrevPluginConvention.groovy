package de.emesit.gradle.svnrev

class SvnrevPluginConvention {
    String projectNamePrefix = ''
    String filenameTemplate  = '${projectNamePrefix}${projectName}.build.properties'
    String dateTimeFormat    = 'yyyy-MM-dd HH:mm:ss z'
    Locale locale            = Locale.US
    String revisionPrefix    = 'svn'
    String modifiedSuffix    = 'm'

    String consoleTemplate = '''  ==> ${projectNamePrefix}${projectName}.version=${projectVersion}
                               |<% if (revisionNumber != null) { %>
                               | revision=${revisionPrefix}${revisionNumber}${locallyModified ? modifiedSuffix : ''}
                               | commit.datetime=${commitDateTimeString}
                               |<% } else { %> 
                               | "Not under version control!"
                               |<% } %>'''.stripMargin().normalize().replace('\n', ' ') + '\n'

    String fileTemplate = '''${projectNamePrefix}${projectName}.version=${projectVersion}
                            |${projectNamePrefix}${projectName}.build.datetime=${buildDateTimeString}
                            |${projectNamePrefix}${projectName}.revision=${revisionNumber!=null ? revisionPrefix+revisionNumber+(locallyModified ? modifiedSuffix : '') : ''}
                            |${projectNamePrefix}${projectName}.locallyModified=${locallyModified}
                            |${projectNamePrefix}${projectName}.commit.datetime=${commitDateTimeString}\n'''.stripMargin().normalize()
}
