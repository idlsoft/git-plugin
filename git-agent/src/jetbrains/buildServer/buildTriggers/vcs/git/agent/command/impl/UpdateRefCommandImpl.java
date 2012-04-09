/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

package jetbrains.buildServer.buildTriggers.vcs.git.agent.command.impl;

import com.intellij.execution.configurations.GeneralCommandLine;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.buildTriggers.vcs.git.agent.command.UpdateRefCommand;
import jetbrains.buildServer.vcs.VcsException;
import org.jetbrains.annotations.NotNull;

/**
 * @author dmitry.neverov
 */
public class UpdateRefCommandImpl implements UpdateRefCommand {

  private final GeneralCommandLine myCmd;
  private String myRef;
  private String myRevision;

  public UpdateRefCommandImpl(@NotNull GeneralCommandLine cmd) {
    myCmd = cmd;
  }

  @NotNull
  public UpdateRefCommand setRef(@NotNull String ref) {
    myRef = ref;
    return this;
  }

  @NotNull
  public UpdateRefCommand setRevision(@NotNull String revision) {
    myRevision = revision;
    return this;
  }

  public void call() throws VcsException {
    myCmd.addParameters("update-ref", "-m", "setting revision to checkout", myRef, myRevision);
    ExecResult r = CommandUtil.runCommand(myCmd);
    CommandUtil.failIfNotEmptyStdErr(myCmd, r);
  }
}