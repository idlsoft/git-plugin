/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.buildTriggers.vcs.git.tests;

import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.buildTriggers.vcs.git.*;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.util.cache.ResetCacheHandler;
import jetbrains.buildServer.util.cache.ResetCacheRegister;
import jetbrains.buildServer.vcs.MockVcsOperationProgressProvider;
import jetbrains.buildServer.vcs.VcsException;
import org.eclipse.jgit.errors.NotSupportedException;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.URIish;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GitSupportBuilder {

  private ServerPluginConfig myPluginConfig;
  private PluginConfigBuilder myPluginConfigBuilder;
  private ExtensionHolder myExtensionHolder;
  private ResetCacheRegister myResetCacheManager;
  private FetchCommand myFetchCommand;
  private Runnable myBeforeFetchHook;
  private ServerPaths myServerPaths;
  private RepositoryManager myRepositoryManager;
  private TransportFactory myTransportFactory;
  private MirrorManager myMirrorManager;
  private GitMapFullPath myMapFullPath;
  private CommitLoader myCommitLoader;
  private List<GitServerExtension> myExtensions = new ArrayList<GitServerExtension>();

  public static GitSupportBuilder gitSupport() {
    return new GitSupportBuilder();
  }

  @NotNull
  public GitVcsSupport build() {
    if (myPluginConfigBuilder == null && myServerPaths == null && myPluginConfig == null)
      throw new IllegalStateException("Plugin config or server paths should be set");
    if (myPluginConfig == null)
      myPluginConfig = myPluginConfigBuilder != null ? myPluginConfigBuilder.build() : new PluginConfigImpl(myServerPaths);
    if (myTransportFactory == null)
      myTransportFactory = new TransportFactoryImpl(myPluginConfig, new EmptyVcsRootSshKeyManager());

    Mockery context = new Mockery();
    if (myFetchCommand == null) {
      if (myBeforeFetchHook == null) {
        myFetchCommand = new FetchCommandImpl(myPluginConfig, myTransportFactory, new FetcherProperties(myPluginConfig), new EmptyVcsRootSshKeyManager());
      } else {
        final FetchCommand originalCommand = new FetchCommandImpl(myPluginConfig, myTransportFactory, new FetcherProperties(myPluginConfig), new EmptyVcsRootSshKeyManager());
        myFetchCommand = new FetchCommand() {
          public void fetch(@NotNull Repository db,
                            @NotNull URIish fetchURI,
                            @NotNull Collection<RefSpec> refspecs,
                            @NotNull FetchSettings settings)
            throws NotSupportedException, TransportException, VcsException {
            myBeforeFetchHook.run();
            originalCommand.fetch(db, fetchURI, refspecs, settings);
          }
        };
      }
    }
    myMirrorManager = new MirrorManagerImpl(myPluginConfig, new HashCalculatorImpl());
    myRepositoryManager = new RepositoryManagerImpl(myPluginConfig, myMirrorManager);
    final ResetCacheRegister resetCacheManager;
    if (myResetCacheManager == null) {
      context.setImposteriser(ClassImposteriser.INSTANCE);
      resetCacheManager = context.mock(ResetCacheRegister.class);
      context.checking(new Expectations() {{
        allowing(resetCacheManager).registerHandler(with(any(ResetCacheHandler.class)));
      }});
    } else {
      resetCacheManager = myResetCacheManager;
    }
    myMapFullPath = new GitMapFullPath(myPluginConfig);
    myCommitLoader = new CommitLoaderImpl(myRepositoryManager, myFetchCommand, myMapFullPath);
    GitVcsSupport git = new GitVcsSupport(myPluginConfig, resetCacheManager, myTransportFactory, myRepositoryManager, myMapFullPath, myCommitLoader,
                                          new EmptyVcsRootSshKeyManager(), new MockVcsOperationProgressProvider());
    git.addExtensions(myExtensions);
    git.setExtensionHolder(myExtensionHolder);
    return git;
  }

  @NotNull
  public ServerPluginConfig getPluginConfig() {
    if (myPluginConfig == null) throw new Error("Plugin config is not yet created. Call #build()");
    return myPluginConfig;
  }

  public GitSupportBuilder withPluginConfig(@NotNull PluginConfigBuilder config) {
    myPluginConfigBuilder = config;
    return this;
  }

  public GitSupportBuilder withPluginConfig(@NotNull ServerPluginConfig config) {
    myPluginConfig = config;
    return this;
  }

  public GitSupportBuilder withServerPaths(@NotNull ServerPaths paths) {
    myServerPaths = paths;
    return this;
  }

  public GitSupportBuilder withExtensionHolder(@Nullable ExtensionHolder holder) {
    myExtensionHolder = holder;
    return this;
  }

  public GitSupportBuilder withResetCacheManager(@NotNull ResetCacheRegister resetCacheManager) {
    myResetCacheManager = resetCacheManager;
    return this;
  }

  public GitSupportBuilder withFetchCommand(@NotNull FetchCommand fetchCommand) {
    myFetchCommand = fetchCommand;
    return this;
  }

  public GitSupportBuilder withExtension(@NotNull GitServerExtension extension) {
    myExtensions.add(extension);
    return this;
  }

  public GitSupportBuilder withBeforeFetchHook(@NotNull Runnable beforeFetchHook) {
    myBeforeFetchHook = beforeFetchHook;
    return this;
  }

  public GitSupportBuilder withTransportFactory(@NotNull TransportFactory factory) {
    myTransportFactory = factory;
    return this;
  }

  public RepositoryManager getRepositoryManager() {
    return myRepositoryManager;
  }

  public GitMapFullPath getMapFullPath() {
    return myMapFullPath;
  }

  public CommitLoader getCommitLoader() {
    return myCommitLoader;
  }

  public TransportFactory getTransportFactory() {
    return myTransportFactory;
  }
}
