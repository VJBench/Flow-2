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
package com.vaadin.flow.migration;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import com.vaadin.flow.server.frontend.FrontendTools;
import com.vaadin.flow.server.frontend.FrontendUtils;
import com.vaadin.flow.server.frontend.scanner.ClassFinder;

public class MigrationTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    private MigrationConfiguration configuration = Mockito
            .mock(MigrationConfiguration.class);

    private File targetFolder;

    @After
    public void cleanup() {
        targetFolder = null;
    }

    @Test(expected = IllegalArgumentException.class)
    public void createMigration_noBaseDir_throw() {
        new Migration(configuration);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createMigration_baseDirIsSet_noClassFinder_throw() {
        Mockito.when(configuration.getClassFinder())
                .thenReturn(Mockito.mock(ClassFinder.class));
        Mockito.when(configuration.getJavaSourceDirectories())
                .thenReturn(new File[] { new File("bar") });
        Mockito.when(configuration.getCompiledClassDirectory())
                .thenReturn(new File("foobar"));
        new Migration(configuration);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createMigration_noJavaSourceDirs_throw() {
        Mockito.when(configuration.getBaseDirectory())
                .thenReturn(new File("foo"));
        Mockito.when(configuration.getClassFinder())
                .thenReturn(Mockito.mock(ClassFinder.class));
        Mockito.when(configuration.getCompiledClassDirectory())
                .thenReturn(new File("foobar"));
        new Migration(configuration);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createMigration_emptyJavaSourceDirs_throw() {
        Mockito.when(configuration.getBaseDirectory())
                .thenReturn(new File("foo"));
        Mockito.when(configuration.getClassFinder())
                .thenReturn(Mockito.mock(ClassFinder.class));
        Mockito.when(configuration.getCompiledClassDirectory())
                .thenReturn(new File("foobar"));
        Mockito.when(configuration.getJavaSourceDirectories())
                .thenReturn(new File[] {});
        new Migration(configuration);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createMigration_noCompiledClassDir_throw() {
        Mockito.when(configuration.getBaseDirectory())
                .thenReturn(new File("foo"));
        Mockito.when(configuration.getClassFinder())
                .thenReturn(Mockito.mock(ClassFinder.class));
        Mockito.when(configuration.getJavaSourceDirectories())
                .thenReturn(new File[] { new File("bar") });
        new Migration(configuration);
    }

    @Test
    public void createMigration_allRequiredConfigParamsAreSet_doesnThrow() {
        Mockito.when(configuration.getBaseDirectory())
                .thenReturn(new File("foo"));
        Mockito.when(configuration.getClassFinder())
                .thenReturn(Mockito.mock(ClassFinder.class));
        Mockito.when(configuration.getJavaSourceDirectories())
                .thenReturn(new File[] { new File("bar") });
        Mockito.when(configuration.getCompiledClassDirectory())
                .thenReturn(new File("foobar"));

        new Migration(configuration);
    }

    @Test
    public void migrateNpmPassesHappyPath() throws MigrationFailureException,
            MigrationToolsException, IOException {
        Mockito.when(configuration.isPnpmEnabled()).thenReturn(false);
        FrontendTools tools = new FrontendTools(new File("foo").getPath(),
                () -> FrontendUtils.getVaadinHomeDirectory().getAbsolutePath());
        final List<String> npm = tools.getNpmExecutable();
        // Expected execution calls:
        // 1 - npm --no-update-notifier --no-audit --scripts-prepend-node-path=true install polymer-modulizer
        // 2 - node {tempFolder} i -F --confid.interactive=false -S
        // polymer#2.8.0
        // 3 - npm --no-update-notifier --no-audit --scripts-prepend-node-path=true i
        // 4 - node node_modules/polymer-modulizer/bin/modulizer.js --force
        // --out , --import-style=name

        // with .vadin path calls 1 and 3 will get node in the beginning.
        if(npm.get(0).contains(".vaadin")) {
            migratePassesHappyPath(Stream.of(7, 7, 6, 6).collect(Collectors.toCollection(LinkedList::new)));
        } else {
            migratePassesHappyPath(Stream.of(6, 7, 5, 6).collect(Collectors.toCollection(LinkedList::new)));
        }
    }

    @Ignore("Flaky test https://github.com/vaadin/flow/issues/10429")
    @Test
    public void migratePnpmPassesHappyPath() throws MigrationFailureException,
            MigrationToolsException, IOException {
        Mockito.when(configuration.isPnpmEnabled()).thenReturn(true);
        targetFolder = makeTempDirectoryStructure();

        File homeNodeDir = Paths
                .get(FrontendUtils.getVaadinHomeDirectory().getAbsolutePath(),
                        "node").toFile();
        File baseDir = new File(targetFolder.getAbsolutePath(), "foo");
        FrontendTools tools = new FrontendTools(baseDir.getAbsolutePath(),
                () -> baseDir.getAbsolutePath());
        List<String> pnpmExecutable = tools.getPnpmExecutable();
        if (homeNodeDir.exists() || !pnpmExecutable.get(0).contains("npx")) {
            // locally installed node

            // Expected execution calls:
            // 1 - node node_modules/npm/bin/npx-cli.js --yes --quiet pnpm
            //     --shamefully-hoist=true install polymer-modulizer
            // 2 - node {tempFolder} i -F --confid.interactive=false -S
            //     polymer#2.8.0
            // 3 - node node_modules/npm/bin/npx-cli.js --yes --quiet pnpm
            //     --shamefully-hoist=true i
            // 4 - node node_modules/polymer-modulizer/bin/modulizer.js --force
            //     --out , --import-style=name
            migratePassesHappyPath(Stream.of(8, 7, 7, 6)
                    .collect(Collectors.toCollection(LinkedList::new)));
        } else {
            // globally installed node

            // Expected execution calls:
            // 1 - npx --yes --quiet pnpm --shamefully-hoist=true install
            //     polymer-modulizer
            // 2 - node {tempFolder} i -F --confid.interactive=false -S
            //     polymer#2.8.0
            // 3 - npx --yes --quiet pnpm --shamefully-hoist=true i
            // 4 - node node_modules/polymer-modulizer/bin/modulizer.js --force
            //     --out , --import-style=name
            migratePassesHappyPath(Stream.of(7, 7, 6, 6)
                    .collect(Collectors.toCollection(LinkedList::new)));
        }
    }

    private void migratePassesHappyPath(
            LinkedList<Integer> executeExpectations)
            throws MigrationFailureException, MigrationToolsException,
            IOException {
        File sourcesFolder = makeTempDirectoryStructure();
        File targetFolder = temporaryFolder.newFolder();

        Mockito.when(configuration.getBaseDirectory())
                .thenReturn(Paths.get(sourcesFolder.getPath(), "foo").toFile());
        Mockito.when(configuration.getTempMigrationFolder())
                .thenReturn(targetFolder);
        Mockito.when(configuration.getAnnotationRewriteStrategy())
                .thenReturn(AnnotationsRewriteStrategy.SKIP);
        Mockito.when(configuration.isKeepOriginalFiles()).thenReturn(true);
        Mockito.when(configuration.getClassFinder())
                .thenReturn(Mockito.mock(ClassFinder.class));
        Mockito.when(configuration.getJavaSourceDirectories())
                .thenReturn(new File[] {
                        Paths.get(sourcesFolder.getPath(), "bar").toFile() });
        Mockito.when(configuration.getCompiledClassDirectory()).thenReturn(
                Paths.get(sourcesFolder.getPath(), "foobar").toFile());

        Migration migration = new Migration(configuration) {
            @Override
            protected void prepareMigrationDirectory() {
                super.prepareMigrationDirectory();

                // Temporary folder is empty at this stage and whether to
                // install bower or not is determined by existence of bower
                // on path. To force deterministic test add bower mock to
                // temporary folder here.
                try {
                    populateTargetWithApplications(targetFolder);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            @Override
            protected boolean executeProcess(List<String> command,
                    String errorMsg, String successMsg, String exceptionMsg) {
                Assert.assertEquals(
                        "Unexpected command '" + command.toString() + "'",
                        (int) executeExpectations.pop(), command.size());
                // Skip actual execution of commands.
                return true;
            }
        };
        migration.migrate();
    }

    private void populateTargetWithApplications(File targetFolder)
            throws IOException {
        targetFolder.mkdirs();
        Path bowerBin = Files
                .createDirectories(Paths.get(targetFolder.getAbsolutePath(),
                        "node_modules", "bower", "bin"));
        new File(bowerBin.toFile(), "bower").createNewFile();

        // Add stub node for test. !note! will not work on windows which will
        // need to have node installed
        Path nodeDirectory = Files.createDirectories(
                Paths.get(targetFolder.getAbsolutePath(), "node"));
        File node = new File(nodeDirectory.toFile(), "node");
        node.createNewFile();
        node.setExecutable(true);
        FileUtils.write(node,
                "#!/bin/sh\n[ \"$1\" = -v ] && echo 8.0.0 || sleep 1\n",
                "UTF-8");
    }

    private File makeTempDirectoryStructure() throws IOException {
        File folder;
        if (targetFolder == null) {
            folder = temporaryFolder.newFolder();
            folder.mkdirs();
        } else {
            folder = targetFolder;
        }
        Files.createDirectories(Paths.get(folder.getAbsolutePath(), "foo",
                "src", "main", "webapp"));
        Files.createDirectories(Paths.get(folder.getAbsolutePath(), "bar",
                "src", "main", "java"));
        Files.createDirectories(Paths.get(folder.getAbsolutePath(), "foobar"));
        return folder;
    }
}
