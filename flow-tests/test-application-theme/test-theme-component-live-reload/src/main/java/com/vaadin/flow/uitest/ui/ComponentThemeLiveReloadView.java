/*
 * Copyright 2000-2021 Vaadin Ltd.
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

package com.vaadin.flow.uitest.ui;

import java.util.Random;

import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;

@Theme(themeFolder = "app-theme")
@Route(value = "com.vaadin.flow.uitest.ui.ComponentThemeLiveReloadView")
@NpmPackage(value = "@vaadin/vaadin-themable-mixin", version = "1.6.1")
public class ComponentThemeLiveReloadView extends Div {

    public static final String ATTACH_IDENTIFIER = "attach-identifier";
    public static final String THEMED_COMPONENT_ID = "themed-component-id";

    private static final Random random = new Random();

    private final Span attachIdLabel = new Span();

    public ComponentThemeLiveReloadView() {
        TestThemedTextField testThemedTextField = new TestThemedTextField();
        testThemedTextField.withId(THEMED_COMPONENT_ID);
        add(testThemedTextField);

        add(new Paragraph("This is a Paragraph to test the applied font"));

        attachIdLabel.setId(ATTACH_IDENTIFIER);
        add(attachIdLabel);
        addAttachListener(
                e -> attachIdLabel.setText(Integer.toString(random.nextInt())));
    }
}
