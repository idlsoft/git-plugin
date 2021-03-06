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

package jetbrains.buildServer.buildTriggers.vcs.git.agent;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.SimpleCommandLineProcessRunner;
import jetbrains.buildServer.buildTriggers.vcs.git.agent.command.*;
import jetbrains.buildServer.buildTriggers.vcs.git.agent.command.impl.*;
import jetbrains.buildServer.ssh.VcsRootSshKeyManager;
import jetbrains.buildServer.vcs.VcsException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author dmitry.neverov
 */
public class NativeGitFacade implements GitFacade {

  private final GitAgentSSHService mySsh;
  private final AskPassGenerator myAskPassGen;
  private final String myGitPath;
  private final File myRepositoryDir;
  private final File myTmpDir;
  private final boolean myDeleteTempFiles;
  private final GitProgressLogger myLogger;
  private final GitExec myGitExec;
  private VcsRootSshKeyManager mySshKeyManager;

  public NativeGitFacade(@NotNull GitAgentSSHService ssh,
                         @NotNull String gitPath,
                         @NotNull File repositoryDir,
                         @NotNull File tmpDir,
                         boolean deleteTempFiles,
                         @NotNull GitProgressLogger logger,
                         @NotNull GitExec gitExec) {
    mySsh = ssh;
    myTmpDir = tmpDir;
    myAskPassGen = makeAskPassGen();
    myGitPath = gitPath;
    myRepositoryDir = repositoryDir;
    myDeleteTempFiles = deleteTempFiles;
    myLogger = logger;
    myGitExec = gitExec;
  }

  public NativeGitFacade(@NotNull String gitPath, @NotNull GitProgressLogger logger) {
    mySsh = null;
    myTmpDir = new File(FileUtil.getTempDirectory());
    myAskPassGen = makeAskPassGen();
    myGitPath = gitPath;
    myRepositoryDir = new File(".");
    myDeleteTempFiles = true;
    myLogger = logger;
    myGitExec = null;
  }


  @NotNull
  public InitCommand init() {
    return new InitCommandImpl(createCommandLine());
  }

  @NotNull
  public CreateBranchCommand createBranch() {
    return new CreateBranchCommandImpl(createCommandLine());
  }

  @NotNull
  public DeleteBranchCommand deleteBranch() {
    return new DeleteBranchCommandImpl(createCommandLine());
  }

  @NotNull
  public DeleteTagCommand deleteTag() {
    return new DeleteTagCommandImpl(createCommandLine());
  }

  @NotNull
  public AddRemoteCommand addRemote() {
    return new AddRemoteCommandImpl(createCommandLine());
  }

  @NotNull
  public CleanCommand clean() {
    return new CleanCommandImpl(createCommandLine());
  }

  @NotNull
  public ResetCommand reset() {
    return new ResetCommandImpl(createCommandLine());
  }

  @NotNull
  public UpdateRefCommand updateRef() {
    return new UpdateRefCommandImpl(createCommandLine());
  }

  @NotNull
  public CheckoutCommand checkout() {
    return new CheckoutCommandImpl(createCommandLine());
  }

  @NotNull
  public BranchCommand branch() {
    return new BranchCommandImpl(createCommandLine());
  }

  @NotNull
  public GetConfigCommand getConfig() {
    return new GetConfigCommandImpl(createCommandLine());
  }

  @NotNull
  public SetConfigCommand setConfig() {
    return new SetConfigCommandImpl(createCommandLine());
  }

  @NotNull
  public FetchCommand fetch() {
    return new FetchCommandImpl(createCommandLine());
  }

  @NotNull
  public LogCommand log() {
    return new LogCommandImpl(createCommandLine());
  }

  @NotNull
  public SubmoduleInitCommand submoduleInit() {
    return new SubmoduleInitCommandImpl(createCommandLine());
  }

  @NotNull
  public SubmoduleSyncCommand submoduleSync() {
    return new SubmoduleSyncCommandImpl(createCommandLine());
  }

  @NotNull
  public SubmoduleUpdateCommand submoduleUpdate() {
    return new SubmoduleUpdateCommandImpl(createCommandLine());
  }

  @NotNull
  public ShowRefCommand showRef() {
    return new ShowRefCommandImpl(createCommandLine());
  }

  @NotNull
  public VersionCommand version() {
    return new VersionCommandImpl(createCommandLine());
  }

  @NotNull
  public LsRemoteCommand lsRemote() {
    return new LsRemoteCommandImpl(createCommandLine());
  }

  @NotNull
  public PackRefs packRefs() {
    return new PackRefsImpl(createCommandLine());
  }

  @NotNull
  public GcCommand gc() {
    return new GcCommandImpl(createCommandLine());
  }

  @NotNull
  public RepackCommand repack() {
    return new RepackCommandImpl(createCommandLine());
  }

  @NotNull
  public String resolvePath(@NotNull File f) throws VcsException {
    try {
      if (myGitExec.isCygwin()) {
        String cygwinBin = myGitExec.getCygwinBinPath();
        GeneralCommandLine cmd = new GeneralCommandLine();
        cmd.setWorkDirectory(cygwinBin);
        cmd.setExePath(new File(cygwinBin, "cygpath.exe").getCanonicalPath());
        cmd.addParameter(f.getCanonicalPath());
        ExecResult res = SimpleCommandLineProcessRunner.runCommandSecure(cmd, cmd.getCommandLineString(), null, new ProcessTimeoutCallback(30));
        Throwable error = res.getException();
        if (error != null)
          throw error;
        return res.getStdout().trim();
      } else {
        return f.getCanonicalPath();
      }
    } catch (Throwable e) {
      throw new VcsException("Error while resolving path " + f.getAbsolutePath(), e);
    }
  }

  @NotNull
  private GitCommandLine createCommandLine() {
    GitCommandLine cmd = new GitCommandLine(mySsh, myAskPassGen, myTmpDir, myDeleteTempFiles, myLogger);
    cmd.setExePath(myGitPath);
    cmd.setWorkingDirectory(myRepositoryDir);
    cmd.setSshKeyManager(mySshKeyManager);
    return cmd;
  }

  public void setSshKeyManager(@Nullable VcsRootSshKeyManager sshKeyManager) {
    mySshKeyManager = sshKeyManager;
  }

  @NotNull
  private AskPassGenerator makeAskPassGen() {
    return SystemInfo.isUnix ? new UnixAskPassGen(myTmpDir, new EscapeEchoArgumentUnix()) : new WinAskPassGen(myTmpDir, new EscapeEchoArgumentWin());
  }
}
