package gitflowavh;

import com.intellij.openapi.project.Project;
import git4idea.GitLocalBranch;
import git4idea.GitRemoteBranch;
import git4idea.branch.GitBranchUtil;
import git4idea.repo.GitRemote;
import git4idea.repo.GitRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;


public class GitFlowAVHBranchUtil {

    Project myProject;
    GitRepository repo;

    String currentBranchName;
    String branchnameMaster;
    String prefixFeature;
    String prefixRelease;
    String prefixHotfix;
    String prefixBugfix;

    /**
     * @param project Project
     */
    public GitFlowAVHBranchUtil(Project project) {
        myProject = project;
        repo = GitBranchUtil.getCurrentRepository(project);

        assert repo != null;
        currentBranchName = GitBranchUtil.getBranchNameOrRev(repo);

        branchnameMaster = GitFlowAVHConfigUtil.getMasterBranch(project);
        prefixFeature = GitFlowAVHConfigUtil.getFeaturePrefix(project);
        prefixRelease = GitFlowAVHConfigUtil.getReleasePrefix(project);
        prefixHotfix = GitFlowAVHConfigUtil.getHotfixPrefix(project);
        prefixBugfix = GitFlowAVHConfigUtil.getBugfixPrefix(project);
    }

    /**
     * @return boolean
     */
    public boolean hasGitflow() {
        boolean hasGitflow;
        hasGitflow = GitFlowAVHConfigUtil.getMasterBranch(myProject) != null
                && GitFlowAVHConfigUtil.getDevelopBranch(myProject) != null
                && GitFlowAVHConfigUtil.getFeaturePrefix(myProject) != null
                && GitFlowAVHConfigUtil.getReleasePrefix(myProject) != null
                && GitFlowAVHConfigUtil.getHotfixPrefix(myProject) != null
                && GitFlowAVHConfigUtil.getBugfixPrefix(myProject) != null;

        return hasGitflow;
    }

    /**
     * Checks whether the current branch is master branch.
     *
     * @return boolean
     */
    /*public boolean isCurrentBranchMaster(){
        return currentBranchName.startsWith(branchnameMaster);
    }*/

    /**
     * Checks whether the current branch is a feature branch.
     *
     * @return boolean
     */
    public boolean isCurrentBranchFeature() {
        return currentBranchName.startsWith(prefixFeature);
    }

    /**
     * Checks whether the current branch is a release branch.
     *
     * @return boolean
     */
    public boolean isCurrentBranchRelease() {
        return currentBranchName.startsWith(prefixRelease);
    }

    /**
     * Checks whether the current branch is a hotfix branch.
     *
     * @return boolean
     */
    public boolean isCurrentBranchHotfix() {
        return currentBranchName.startsWith(prefixHotfix);
    }

    /**
     * Checks whether the current branch is a bugfix branch.
     *
     * @return boolean
     */
    public boolean isCurrentBranchBugfix() {
        return currentBranchName.startsWith(prefixBugfix);
    }

    /**
     * Checks whether the current branch also exists on the remote.
     *
     * @return boolean
     */
    public boolean isCurrentBranchPublished() {
        return !getRemoteBranchesWithPrefix(currentBranchName).isEmpty();
    }

    /**
     * Ff no prefix specified, returns all remote branches
     * @param prefix String
     * @return ArrayList<string>
     */
    public ArrayList<String> getRemoteBranchesWithPrefix(String prefix) {
        ArrayList<String> remoteBranches = getRemoteBranchNames();
        ArrayList<String> selectedBranches = new ArrayList<String>();

        for (String branch : remoteBranches) {
            if (branch.contains(prefix)) {
                selectedBranches.add(branch);
            }
        }

        return selectedBranches;
    }


    /**
     * @param inputBranches Collection<String>
     * @param prefix String
     * @return ArrayList<String>
     */
    public ArrayList<String> filterBranchListByPrefix(Collection<String> inputBranches, String prefix) {
        ArrayList<String> outputBranches = new ArrayList<String>();

        for (String branch : inputBranches) {
            if (branch.contains(prefix)) {
                outputBranches.add(branch);
            }
        }

        return outputBranches;
    }

    /**
     * @return ArrayList<String>
     */
    public ArrayList<String> getRemoteBranchNames() {
        ArrayList<GitRemoteBranch> remoteBranches = new ArrayList<GitRemoteBranch>(repo.getBranches().getRemoteBranches());
        ArrayList<String> branchNameList = new ArrayList<String>();

        for (GitRemoteBranch branch : remoteBranches) {
            branchNameList.add(branch.getName());
        }

        return branchNameList;
    }

    /**
     * @return ArrayList<String>
     */
    public ArrayList<String> getLocalBranchNames() {
        ArrayList<GitLocalBranch> localBranches = new ArrayList<GitLocalBranch>(repo.getBranches().getLocalBranches());
        ArrayList<String> branchNameList = new ArrayList<String>();

        for (GitLocalBranch branch : localBranches) {
            branchNameList.add(branch.getName());
        }

        return branchNameList;
    }

    /**
     * @param branchName String
     * @return GitRemote
     */
    public GitRemote getRemoteByBranch(String branchName) {
        GitRemote remote = null;

        ArrayList<GitRemoteBranch> remoteBranches = new ArrayList<GitRemoteBranch>(repo.getBranches().getRemoteBranches());

        for (GitRemoteBranch branch : remoteBranches) {
            if (Objects.equals(branch.getName(), branchName)) {
                remote = branch.getRemote();
                break;
            }
        }

        return remote;
    }

    /**
     * @param prefix String
     * @return boolean
     */
    public boolean areAllBranchesTracked(String prefix) {
        ArrayList<String> localBranches = filterBranchListByPrefix(getLocalBranchNames(), prefix);

        // To avoid a vacuous truth value. That is, when no branches at all exist,
        // they shouldn't be considered as "all pulled"
        if (localBranches.isEmpty()) {
            return false;
        }

        ArrayList<String> remoteBranches = getRemoteBranchNames();

        // Check that every local branch has a matching remote branch
        for (String localBranch : localBranches) {
            boolean hasMatchingRemoteBranch = false;

            for (String remoteBranch : remoteBranches) {
                if (remoteBranch.contains(localBranch)) {
                    hasMatchingRemoteBranch = true;
                    break;
                }
            }

            // At least one matching branch wasn't found
            if (!hasMatchingRemoteBranch) {
                return false;
            }
        }

        return true;
    }
}
