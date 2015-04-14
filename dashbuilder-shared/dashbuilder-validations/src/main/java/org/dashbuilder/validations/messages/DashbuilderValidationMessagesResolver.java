/*
 * Copyright 2010 Google Inc.
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
package org.dashbuilder.validations.messages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.ConstantsWithLookup;
import com.google.gwt.validation.client.AbstractValidationMessageResolver;
import com.google.gwt.validation.client.UserValidationMessagesResolver;
import org.dashbuilder.dataset.validation.DataSetValidationMessages;

/**
 * @since 0.3.0
 */
// TODO: Delegate to different bundles by a given prefix?
public class DashbuilderValidationMessagesResolver extends AbstractValidationMessageResolver
    implements UserValidationMessagesResolver {

  // instead of a separate class.
  protected DashbuilderValidationMessagesResolver() {
    super((ConstantsWithLookup) GWT.create(DataSetValidationMessages.class));
  }
}