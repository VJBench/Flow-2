/*
 * Copyright 2000-2020 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.plugin.common;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.vaadin.flow.component.WebComponentExporter;
import com.vaadin.flow.component.WebComponentExporterFactory;
import com.vaadin.flow.migration.ClassPathIntrospector;
import com.vaadin.flow.server.webcomponent.WebComponentModulesWriter;

/**
 * Generates embeddable web component files in bower production mode, hiding the
 * complexity caused by using a different class loader.
 *
 * Uses {@link com.vaadin.flow.server.webcomponent.WebComponentModulesWriter} to
 * generate web component modules files from
 * {@link WebComponentExporter}/{@link WebComponentExporterFactory}
 * implementations found by
 * {@link com.vaadin.flow.migration.ClassPathIntrospector}.
 *
 * @author Vaadin Ltd.
 * @since 2.0
 */
public class WebComponentModulesGenerator extends ClassPathIntrospector {
    private Class<?> writerClass;

    /**
     * Creates a new instances and stores the {@code introspector} to be used
     * for locating
     * {@link com.vaadin.flow.server.webcomponent.WebComponentModulesWriter}
     * class and
     * {@link WebComponentExporter}/{@link WebComponentExporterFactory}
     * implementations.
     *
     * @param introspector
     *            {@link com.vaadin.flow.migration.ClassPathIntrospector}
     *            implementation to use as a base.
     */
    public WebComponentModulesGenerator(ClassPathIntrospector introspector) {
        super(introspector);
    }

    /**
     * Collects
     * {@link com.vaadin.flow.server.webcomponent.WebComponentModulesWriter}
     * class and classes that extend {@link WebComponentExporter} or
     * {@link WebComponentExporterFactory} using {@code
     * inspector}. Generates web component modules and places the into the
     * {@code outputDirectory}.
     *
     * @param outputDirectory
     *            target directory for the web component module files
     * @return generated files
     * @throws java.lang.IllegalStateException
     *             if {@code inspector} cannot locate required classes
     */
    public Set<File> generateWebComponentModules(File outputDirectory) {
        Set<Class<?>> exporterRelatedClasses = new HashSet<>();
        getSubtypes(WebComponentExporter.class)
                .forEach(exporterRelatedClasses::add);
        getSubtypes(WebComponentExporterFactory.class)
                .forEach(exporterRelatedClasses::add);

        return WebComponentModulesWriter.DirectoryWriter
                .generateWebComponentsToDirectory(getWriterClass(),
                        exporterRelatedClasses, outputDirectory, true, null);
    }

    private Class<?> getWriterClass() {
        if (writerClass == null) {
            writerClass = loadClassInProjectClassLoader(
                    WebComponentModulesWriter.class.getName());
        }
        return writerClass;
    }
}
