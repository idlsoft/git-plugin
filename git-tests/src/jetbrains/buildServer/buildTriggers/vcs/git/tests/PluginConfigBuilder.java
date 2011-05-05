/*
 * Copyright 2000-2011 JetBrains s.r.o.
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

import jetbrains.buildServer.buildTriggers.vcs.git.PluginConfig;
import jetbrains.buildServer.buildTriggers.vcs.git.PluginConfigImpl;
import jetbrains.buildServer.serverSide.ServerPaths;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author dmitry.neverov
 */
public class PluginConfigBuilder {

  private final PluginConfigImpl myDelegate;
  private Integer myCurrentVersionCacheSize;
  private Integer myStreamFileThreshold;
  private Integer myFetchTimeout;
  private Integer myCloneTimeout;
  private Boolean mySeparateProcessForFetch;
  private Boolean myRunNativeGC;
  private String  myPathToGit;
  private Integer myNativeGCQuota;
  private String  myFetchClassPath;
  private String  myFetcherClassName;

  public PluginConfigBuilder(@NotNull ServerPaths paths) {
    myDelegate = new PluginConfigImpl(paths);
  }


  public PluginConfig build() {
    return new PluginConfig() {
      @NotNull
      public File getCachesDir() {
        return myDelegate.getCachesDir();
      }

      public int getCurrentVersionCacheSize() {
        return myCurrentVersionCacheSize != null ? myCurrentVersionCacheSize.intValue() : myDelegate.getCurrentVersionCacheSize();
      }

      public int getStreamFileThreshold() {
        return myStreamFileThreshold != null ? myStreamFileThreshold.intValue() : myDelegate.getStreamFileThreshold();
      }

      public int getFetchTimeout() {
        return myFetchTimeout != null ? myFetchTimeout.intValue() : myDelegate.getFetchTimeout();
      }

      public int getCloneTimeout() {
        return myCloneTimeout != null ? myCloneTimeout.intValue() : myDelegate.getCloneTimeout();
      }

      public boolean isPrintDebugInfoOnEachCommit() {
        return myDelegate.isPrintDebugInfoOnEachCommit();
      }

      public String getFetchProcessJavaPath() {
        return myDelegate.getFetchProcessJavaPath();
      }

      public String getFetchProcessMaxMemory() {
        return myDelegate.getFetchProcessMaxMemory();
      }

      public boolean isSeparateProcessForFetch() {
        return mySeparateProcessForFetch != null ? mySeparateProcessForFetch.booleanValue() : myDelegate.isSeparateProcessForFetch();
      }

      public boolean isRunNativeGC() {
        return myRunNativeGC != null ? myRunNativeGC.booleanValue() : myDelegate.isRunNativeGC();
      }

      public String getPathToGit() {
        return myPathToGit != null ? myPathToGit : myDelegate.getPathToGit();
      }

      public int getNativeGCQuotaMinutes() {
        return myNativeGCQuota != null ? myNativeGCQuota.intValue() : myDelegate.getNativeGCQuotaMinutes();
      }

      public String getFetchClasspath() {
        return myFetchClassPath != null ? myFetchClassPath : myDelegate.getFetchClasspath();
      }

      public String getFetcherClassName() {
        return myFetcherClassName != null ? myFetcherClassName : myDelegate.getFetcherClassName();
      }
    };
  }


  PluginConfigBuilder setCurrentVersionCacheSize(int size) {
    myCurrentVersionCacheSize = size;
    return this;
  }


  PluginConfigBuilder setStreamFileThreshold(int threshold) {
    myStreamFileThreshold = threshold;
    return this;
  }


  PluginConfigBuilder setFetchTimeout(int timeout) {
    myFetchTimeout = timeout;
    return this;
  }


  PluginConfigBuilder setCloneTimeout(int timeout) {
    myCloneTimeout = timeout;
    return this;
  }


  PluginConfigBuilder setPrintDebugInfoOnEachCommit(boolean debug) {
    return this;
  }


  PluginConfigBuilder setFetchProcessJavaPath(String path) {
    return this;
  }


  PluginConfigBuilder setFetchProcessMaxMemory(String memory) {
    return this;
  }


  PluginConfigBuilder setSeparateProcessForFetch(boolean separateProcess) {
    mySeparateProcessForFetch = separateProcess;
    return this;
  }


  PluginConfigBuilder setRunNativeGC(boolean run) {
    myRunNativeGC = run;
    return this;
  }


  PluginConfigBuilder setPathToGit(String path) {
    myPathToGit = path;
    return this;
  }


  PluginConfigBuilder setNativeGCQuotaMinutes(int quota) {
    myNativeGCQuota = quota;
    return this;
  }


  PluginConfigBuilder setFetchClasspath(String classpath) {
    myFetchClassPath = classpath;
    return this;
  }


  PluginConfigBuilder setFetcherClassName(String className) {
    myFetcherClassName = className;
    return this;
  }
}