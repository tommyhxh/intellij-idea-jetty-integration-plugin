/*
 * Copyright 2007, 2010 Mark Scott, Peter Niederwieser
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

import com.intellij.javaee.deployment.DeploymentManager;
import com.intellij.javaee.deployment.DeploymentMethod;
import com.intellij.javaee.deployment.DeploymentModel;
import com.intellij.javaee.deployment.DeploymentProvider;
import com.intellij.javaee.deployment.DeploymentSource;
import com.intellij.javaee.deployment.DeploymentStatus;
import com.intellij.javaee.run.configuration.CommonModel;
import com.intellij.javaee.serverInstances.J2EEServerInstance;
import com.intellij.javaee.web.artifact.WebArtifactUtil;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.packaging.artifacts.ArtifactType;
import org.codebrewer.idea.jetty.versionsupport.JettyVersionHelper;
import org.jdom.Document;
import org.jdom.output.XMLOutputter;
import org.jetbrains.annotations.NonNls;

import javax.swing.JOptionPane;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;

import static org.codebrewer.idea.jetty.JettyConstants.JETTY_CONTEXT_DEPLOYER_CONFIG_FILE_NAME;

/**
 * @author Mark Scott
 * @author Peter Niederwieser
 */
public class JettyDeploymentProvider extends DeploymentProvider
{
  private static File getContextConfigurationFile(String contextName, File destinationDirectory)
  {
    return new File(destinationDirectory, FileUtil.sanitizeFileName(contextName) + ".xml");
  }

  private static void setDeploymentStatus(J2EEServerInstance instance,
                                          JettyModuleDeploymentModel model,
                                          DeploymentStatus status)
  {
    final CommonModel configuration = instance.getCommonModel();
    final JettyModel jettyConfiguration = ((JettyModel) configuration.getServerModel());
    final DeploymentManager deploymentManager = DeploymentManager.getInstance(jettyConfiguration.getProject());

    deploymentManager.setDeploymentStatus(model, status, configuration, instance);
  }

  public static void prepareServer(final JettyModel jettyModel) throws JettyException
  {
    final Project project = jettyModel.getProject();
    final File scratchDirectory = JettyUtil.getScratchDirectory(project);
    final File contextDeployerDirectory = JettyUtil.getContextDeployerConfigurationDirectory(scratchDirectory);
    final XMLOutputter xmlOutputter = JDOMUtil.createOutputter(System.getProperty("line.separator"));
    final JettyVersionHelper versionHelper = jettyModel.getJettyVersionHelper();
    assert versionHelper != null;
    final Document contextDeployerDocument = JettyUtil.getContextDeployerDocument(versionHelper, scratchDirectory);

    FileUtil.delete(scratchDirectory);
    jettyModel.setScratchDirectory(scratchDirectory);
    contextDeployerDirectory.mkdirs();
    scratchDirectory.deleteOnExit();
    contextDeployerDirectory.deleteOnExit();

    OutputStream out = null;
    final File contextDeployerFile = new File(scratchDirectory, JETTY_CONTEXT_DEPLOYER_CONFIG_FILE_NAME);

    try {
      out = new FileOutputStream(contextDeployerFile);
      xmlOutputter.output(contextDeployerDocument, out);
    }
    catch (IOException e) {
      throw new JettyException(JettyBundle.message("message.text.error.preparing.deployment"), e);
    }
    finally {
      if (out != null) {
        contextDeployerFile.deleteOnExit();

        try {
          out.close();
        }
        catch (IOException e) {
          // ignore
        }
      }
    }
  }

  public void doDeploy(final Project project, final J2EEServerInstance instance, final DeploymentModel model)
  {
    DeploymentStatus deploymentStatus = DeploymentStatus.NOT_DEPLOYED;

    final JettyModuleDeploymentModel moduleDeploymentModel = (JettyModuleDeploymentModel) model;

    try {
      final JettyModel jettyModel = (JettyModel) moduleDeploymentModel.getServerModel();
      final JettyVersionHelper versionHelper = jettyModel.getJettyVersionHelper();
      assert versionHelper != null;
      final Document moduleDeploymentDocument = JettyUtil.getContextDeploymentDocument(moduleDeploymentModel, versionHelper);
      final File destinationDirectory = JettyUtil.getContextDeployerConfigurationDirectory(jettyModel.getScratchDirectory());
      final File contextConfigurationFile = getContextConfigurationFile(moduleDeploymentModel.getContextPath(), destinationDirectory);
      final XMLOutputter xmlOutputter = JDOMUtil.createOutputter(System.getProperty("line.separator"));

      OutputStream out = null;

      try {
        out = new FileOutputStream(contextConfigurationFile);
        xmlOutputter.output(moduleDeploymentDocument, out);
        deploymentStatus = DeploymentStatus.DEPLOYED;
      }
      catch (IOException e) {
        deploymentStatus = DeploymentStatus.FAILED;
        Messages.showErrorDialog(
          project, e.getMessage(), JettyBundle.message("message.text.error.deploying", model.getDeploymentSource().getPresentableName()));
      }
      finally {
        if (out != null) {
          contextConfigurationFile.deleteOnExit();

          try {
            out.close();
          }
          catch (IOException e) {
            // ignore
          }
        }
      }
    }
    catch (JettyException e) {
      deploymentStatus = DeploymentStatus.FAILED;
      JOptionPane.showMessageDialog(null,
        e.getMessage(),
        JettyBundle.message("message.text.error.deploying", model.getDeploymentSource().getPresentableName()),
        JOptionPane.ERROR_MESSAGE);
    }

    setDeploymentStatus(instance, moduleDeploymentModel, deploymentStatus);
  }

  @Override
  public Collection<? extends ArtifactType> getSupportedArtifactTypes()
  {
    return Arrays.asList(
      WebArtifactUtil.getInstance().getExplodedWarArtifactType(), WebArtifactUtil.getInstance().getWarArtifactType());
  }

  @Override
  public DeploymentModel createNewDeploymentModel(CommonModel commonModel, DeploymentSource source)
  {
    return new JettyModuleDeploymentModel(commonModel, source);
  }

  @Override
  public SettingsEditor<DeploymentModel> createAdditionalDeploymentSettingsEditor(CommonModel commonModel, DeploymentSource source)
  {
    return new JettyDeploymentSettingsEditor(commonModel, source);
  }

  public void startUndeploy(J2EEServerInstance activeInstance, DeploymentModel model)
  {
    final JettyModuleDeploymentModel moduleDeploymentModel = (JettyModuleDeploymentModel) model;
    final JettyModel jettyModel = (JettyModel) moduleDeploymentModel.getServerModel();
    final File configurationDirectory = JettyUtil.getContextDeployerConfigurationDirectory(jettyModel.getScratchDirectory());
    final File contextConfigurationFile = getContextConfigurationFile(moduleDeploymentModel.getContextPath(), configurationDirectory);
    final DeploymentStatus deploymentStatus = contextConfigurationFile.delete() ? DeploymentStatus.NOT_DEPLOYED : DeploymentStatus.UNKNOWN;

    setDeploymentStatus(activeInstance, moduleDeploymentModel, deploymentStatus);
  }

  public void updateDeploymentStatus(final J2EEServerInstance instance, DeploymentModel model)
  {
    final JettyModuleDeploymentModel moduleDeploymentModel = (JettyModuleDeploymentModel) model;
    final JettyModel jettyModel = (JettyModel) moduleDeploymentModel.getServerModel();
    final File configurationDirectory = JettyUtil.getContextDeployerConfigurationDirectory(jettyModel.getScratchDirectory());
    final File contextConfigurationFile = getContextConfigurationFile(moduleDeploymentModel.getContextPath(), configurationDirectory);
    final DeploymentStatus deploymentStatus = contextConfigurationFile.exists() ? DeploymentStatus.DEPLOYED : DeploymentStatus.NOT_DEPLOYED;

    setDeploymentStatus(instance, (JettyModuleDeploymentModel) model, deploymentStatus);
  }

  @NonNls
  public String getHelpId()
  {
    return null;
  }

  public DeploymentMethod[] getAvailableMethods()
  {
    return null;
  }
}
