/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.buildsetup.plugins.internal

import groovy.text.SimpleTemplateEngine
import groovy.text.Template
import org.gradle.api.Project
import org.gradle.api.internal.DocumentationRegistry
import org.gradle.util.GradleVersion

abstract class TemplateBasedProjectSetupDescriptor implements ProjectSetupDescriptor {

    private final Project project
    private final DocumentationRegistry documentationRegistry

    TemplateBasedProjectSetupDescriptor(Project project, DocumentationRegistry documentationRegistry) {
        this.project = project
        this.documentationRegistry = documentationRegistry
    }

    abstract URL getBuildFileTemplate()

    abstract URL getSettingsTemplate()

    void generateProject() {
        generateGradleFiles()
        generateProjectSources()
    }

    void generateProjectSources() {
    }

    def generateGradleFiles() {
        generateFileFromTemplate(getBuildFileTemplate(), project.file("build.gradle"), getAdditionalBuildFileTemplateBindings())
        generateFileFromTemplate(getSettingsTemplate(), project.file("settings.gradle"), getAdditionalSettingsFileTemplateBindings())
    }

    protected Map getAdditionalBuildFileTemplateBindings() {
        return [ref_userguide_java_tutorial: documentationRegistry.getDocumentationFor("tutorial_java_projects")]
    }

    protected Map getAdditionalSettingsFileTemplateBindings() {
        return [ref_userguide_multiproject: documentationRegistry.getDocumentationFor("multi_project_builds"), rootProjectName: project.name]
    }

    protected generateFileFromTemplate(URL templateURL, File targetFile, Map additionalBindings) {
        SimpleTemplateEngine templateEngine = new SimpleTemplateEngine()
        def bindings = [genDate: new Date(), genUser: System.getProperty("user.name"), genGradleVersion: GradleVersion.current().toString()]
        bindings += additionalBindings
        Template template = templateEngine.createTemplate(templateURL.text)
        targetFile.withWriter {writer ->
            template.make(bindings).writeTo(writer)
        }
    }
}
