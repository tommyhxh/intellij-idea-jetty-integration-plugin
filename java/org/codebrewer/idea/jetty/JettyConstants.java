/*
 * Copyright 2007 Mark Scott
 *
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
package org.codebrewer.idea.jetty;

import org.jetbrains.annotations.NonNls;

/**
 * @author Mark Scott
 * @version $Id$
 */
public class JettyConstants
{
  /**
   * The name of the Jetty lib directory, relative to the Jetty installation
   * directory.
   */
  @NonNls public static final String JETTY_CONFIG_DIRECTORY_NAME = "etc";

  /**
   * The name of the environment variable conventionally used to indicate the
   * location of a Jetty installation.
   */
  @NonNls public static final String JETTY_HOME_ENV_VAR = "JETTY_HOME";

  /**
   * The name of the environment variable conventionally used to pass options to
   * a Jetty startup script.
   */
  @NonNls public static final String JETTY_OPTS_ENV_VAR = "JETTY_OPTS";

  /**
   * The name of the Jetty JSP 2.1 lib directory, relative to the Jetty
   * installation lib directory.
   */
  @NonNls public static final String JETTY_JSP_2_1_LIB_DIRECTORY_NAME = "jsp-2.1";

  /**
   * The name of the Jetty lib directory, relative to the Jetty installation
   * directory.
   */
  @NonNls public static final String JETTY_LIB_DIRECTORY_NAME = "lib";

  /**
   * The TCP port number on which Jetty by default listens for client
   * connections.
   */
  public static final int DEFAULT_PORT = 8080;

  /**
   * The URL prefix for the file scheme.
   */
  @NonNls public static final String FILE_SCHEME = "file://";

  /**
   * The URL prefix for the HTTP scheme.
   */
  @NonNls public static final String HTTP_SCHEME = "http://";

  /**
   * The name of the root element in a valid Jetty XML configuration file.
   */
  @NonNls public static final String JETTY_DOCTYPE_ELEMENT_NAME = "Configure";

  /**
   * The public ID of the DOCTYPE declaration in a valid Jetty XML configuration file.
   */
  @NonNls public static final String JETTY_DOCTYPE_PUBLIC_ID = "-//Mort Bay Consulting//DTD Configure//EN";

  /**
   * The system ID of the DOCTYPE declaration in a valid Jetty XML configuration file.
   */
  @NonNls public static final String JETTY_DOCTYPE_SYSTEM_ID = "http://jetty.mortbay.org/configure.dtd";

  private JettyConstants()
  {
    // Utility class
  }
}