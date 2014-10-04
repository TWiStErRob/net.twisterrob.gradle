package net.twisterrob.gradle.vcs

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.tmatesoft.svn.cli.SVN
import org.tmatesoft.svn.core.SVNException
import org.tmatesoft.svn.core.wc.*

import java.security.Permission

class SVNPlugin implements Plugin<Project> {
    void apply(Project project) {
        def svn = project.VCS.extensions.create("svn", SVNPluginExtension)
        svn.project = project // TODO better solution
    }
}

class SVNPluginExtension implements VCSExtension {
    Project project

    private SVNStatus open() {
        ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
        SVNClientManager clientManager = SVNClientManager.newInstance(options);
        SVNStatusClient statusClient = clientManager.getStatusClient();
        return statusClient.doStatus(project.rootDir, false);
    }

    String getRevision() {
        return String.valueOf(getRevisionNumber());
    }

    // Same as: new XmlSlurper().parseText(SVN.cli('info', '--xml')).entry.commit.@revision
    int getRevisionNumber() {
        return open().revision.number
    }

    // Same as 'svn info'.execute([], project.rootDir).waitFor() == 0
    boolean isAvailable() {
        try {
            return open().versioned
        } catch(SVNException ignore) {
            return false
        }
    }

    // key method/closure - used as: def out = doSvnMain( 'your', 'svn', 'args', 'go', 'here' );
    String cli(String... aSvnArgs) {
        System.setSecurityManager(new NonExitingSecurityManager()) // stop SVN.main from doing a System.exit call
        def baos = new ByteArrayOutputStream()
        PrintStream oldSystemOut = System.out
        System.setOut(new PrintStream(baos))
        try {
            SVN.main(aSvnArgs as String[])
            System.out.flush()
            return baos.toString()
        } finally {
            System.setOut(oldSystemOut)
            System.setSecurityManager(null);
        }
    };

    class NonExitingSecurityManager extends SecurityManager {
        @Override
        public void checkPermission(Permission perm) {}

        @Override
        public void checkExit(int status) {
            throw new SecurityException();
        }
    }
}
