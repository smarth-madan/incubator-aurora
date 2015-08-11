/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.aurora.auth;

import java.util.Objects;
import java.util.Optional;

import javax.inject.Provider;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;

import org.apache.shiro.subject.Subject;

/**
 * Uses context from Shiro for audit messages if available, otherwise defaults to a placeholder
 * indicating the audit record is unsecure.
 */
class UnsecureSessionContext implements SessionValidator.SessionContext {
  @VisibleForTesting
  static final String UNSECURE = "UNSECURE";

  private final Provider<Optional<Subject>> subjectProvider;

  @Inject
  UnsecureSessionContext(Provider<Optional<Subject>> subjectProvider) {
    this.subjectProvider = Objects.requireNonNull(subjectProvider);
  }

  @Override
  public String getIdentity() {
    return subjectProvider.get()
        .map(Subject::getPrincipals)
        .map(principalCollection -> principalCollection.oneByType(String.class))
        .orElse(UNSECURE);
  }
}
