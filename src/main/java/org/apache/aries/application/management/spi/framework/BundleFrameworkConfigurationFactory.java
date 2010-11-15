/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIESOR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.aries.application.management.spi.framework;

import org.apache.aries.application.management.AriesApplication;
import org.osgi.framework.BundleContext;

public interface BundleFrameworkConfigurationFactory
{
  /**
   * Create a BundleFrameworkConfiguration with basic config
   * @param parentCtx
   * @return
   */
  public BundleFrameworkConfiguration createBundleFrameworkConfig(String frameworkId,
      BundleContext parentCtx);

  /**
   * Create a BundleFrameworkConiguration for an application framework based
   * on a given AriesApplication.
   * @param parentCtx
   * @param app
   * @return
   */
  public BundleFrameworkConfiguration createBundleFrameworkConfig(String frameworkId,
      BundleContext parentCtx, AriesApplication app);

}
