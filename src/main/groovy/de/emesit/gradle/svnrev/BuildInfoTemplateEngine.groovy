package de.emesit.gradle.svnrev;

import groovy.text.SimpleTemplateEngine

import java.text.SimpleDateFormat

import org.gradle.api.Project

/**
 * Class to format the gathered BuildInfo.
 * 
 * @author Ruediger Schobbert, emesit GmbH & Co. KG
 */
class BuildInfoTemplateEngine {
    SimpleTemplateEngine engine = new SimpleTemplateEngine()
    Map binding
    String lastConversion

    void initBinding(String projectNamePrefix, Project project, BuildInfo buildInfo, String filenameTemplate, String dateTimeFormat, Locale locale, String revisionPrefix, String modifiedSuffix) {
        binding = [
            projectNamePrefix: projectNamePrefix,
            projectGroup     : project.group,
            projectName      : project.name,
            projectVersion   : project.version,
            buildDateTime    : buildInfo.buildDateTime,
            buildDateTimeString : buildInfo.buildDateTime!=null ? new SimpleDateFormat(dateTimeFormat, locale).format(buildInfo.buildDateTime):'',
            revisionNumber   : buildInfo.revisionNumber,
            locallyModified  : buildInfo.locallyModified,
            commitDateTime   : buildInfo.commitDateTime,
            dateTimeFormat   : dateTimeFormat,
            commitDateTimeString : buildInfo.commitDateTime!=null ? new SimpleDateFormat(dateTimeFormat, locale).format(buildInfo.commitDateTime):'',
            locale           : locale,
            revisionPrefix   : revisionPrefix,
            modifiedSuffix   : modifiedSuffix,
            filenameTemplate : filenameTemplate
        ]
        String filename = engine.createTemplate(filenameTemplate).make(binding).toString()
        binding['filename'] = filename
    }

    String getFilename() {
        return binding['filename']
    }

    String convert(String template) {
        lastConversion = engine.createTemplate(template).make(binding).toString()
        return lastConversion
    }
}
