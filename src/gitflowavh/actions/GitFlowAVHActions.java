package gitflowavh.actions;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import git4idea.branch.GitBranchUtil;
import git4idea.repo.GitRepository;
import gitflowavh.GitFlowAVH;
import gitflowavh.GitFlowAVHBranchUtil;
import gitflowavh.GitFlowAVHConfigUtil;
import org.jetbrains.annotations.NotNull;


/**
 * All actions associated with GitFlowAVH
 */
public class GitFlowAVHActions {
    Project myProject;
    GitFlowAVH myGitflow = ServiceManager.getService(GitFlowAVH.class);
    GitRepository repo;
    private GitFlowAVHBranchUtil branchUtil;

    String currentBranchName;

    private boolean noRemoteTrackBranches;
    private boolean noRemoteFeatureBranches;

    private boolean trackedAllFeatureBranches;
    private boolean trackedAllReleaseBranches;

    public GitFlowAVHActions(@NotNull Project project) {
        myProject = project;
        branchUtil = new GitFlowAVHBranchUtil(project);

        repo = GitBranchUtil.getCurrentRepository(myProject);

        if (repo != null) {
            currentBranchName = GitBranchUtil.getBranchNameOrRev(repo);
        }

        String featurePrefix = GitFlowAVHConfigUtil.getFeaturePrefix(myProject);
        String releasePrefix = GitFlowAVHConfigUtil.getReleasePrefix(myProject);

        if (featurePrefix != null) {
            noRemoteFeatureBranches = branchUtil.getRemoteBranchesWithPrefix(featurePrefix).isEmpty();
            trackedAllFeatureBranches = branchUtil.areAllBranchesTracked(featurePrefix);
        }
        if (releasePrefix != null) {
            noRemoteTrackBranches = branchUtil.getRemoteBranchesWithPrefix(releasePrefix).isEmpty();
            trackedAllReleaseBranches = branchUtil.areAllBranchesTracked(releasePrefix);
        }
    }

    public boolean hasGitflow() {
        return branchUtil.hasGitflow();
    }

    /**
     * Constructs the actions for the widget popup
     * @return ActionGroup
     */
    public ActionGroup getActions() {
        DefaultActionGroup actionGroup = new DefaultActionGroup(null, false);

        //gitflow not setup
        if (!branchUtil.hasGitflow()) {
            actionGroup.add(new InitRepoAction());
        } else {

            //FEATURE ACTIONS

            actionGroup.addSeparator("Feature");
            actionGroup.add(new StartFeatureAction());
            //feature only actions
            if (branchUtil.isCurrentBranchFeature()) {
                actionGroup.add(new FinishFeatureAction());

                //can't publish feature if it's already published
                if (!branchUtil.isCurrentBranchPublished()) {
                    actionGroup.add(new PublishFeatureAction());
                }
            }

            //make sure there's a feature to track, and that not all features are track
            if (!noRemoteFeatureBranches && !trackedAllFeatureBranches) {
                actionGroup.add(new TrackFeatureAction());
            }


            //RELEASE ACTIONS

            actionGroup.addSeparator("Release");
            actionGroup.add(new StartReleaseAction());
            //release only actions
            if (branchUtil.isCurrentBranchRelease()) {
                actionGroup.add(new FinishReleaseAction());

                //can't publish release if it's already published
                if (!branchUtil.isCurrentBranchPublished()) {
                    actionGroup.add(new PublishReleaseAction());
                }
            }

            //make sure there's something to track and that not all features are tracked
            if (!noRemoteTrackBranches && !trackedAllReleaseBranches) {
                actionGroup.add(new TrackReleaseAction());
            }

            // BUGFIX ACTIONS
            actionGroup.addSeparator("Bugfix");

            actionGroup.add(new StartBugfixAction());
            if (branchUtil.isCurrentBranchBugfix()) {
                actionGroup.add(new FinishBugfixAction());

                // Can't publish bugfix if it's already published
                if (!branchUtil.isCurrentBranchPublished()) {
                    actionGroup.add(new PublishBugfixAction());
                }
            }

            //HOTFIX ACTIONS
            actionGroup.addSeparator("Hotfix");

            //master only actions
            actionGroup.add(new StartHotfixAction());
            if (branchUtil.isCurrentBranchHotfix()) {
                actionGroup.add(new FinishHotfixAction());

                //can't publish hotfix if it's already published
                if (!branchUtil.isCurrentBranchPublished()) {
                    actionGroup.add(new PublishHotfixAction());
                }
            }
        }

        return actionGroup;
    }

    static void runMergeTool() {
        git4idea.actions.GitResolveConflictsAction resolveAction = new git4idea.actions.GitResolveConflictsAction();
        AsyncResult<DataContext> asyncResult = DataManager.getInstance().getDataContextFromFocus();
        DataContext dataContext = asyncResult.getResult();
        AnActionEvent e = new AnActionEvent(null, dataContext, ActionPlaces.UNKNOWN, new Presentation(""), ActionManager.getInstance(), 0);
        resolveAction.actionPerformed(e);
    }
}
