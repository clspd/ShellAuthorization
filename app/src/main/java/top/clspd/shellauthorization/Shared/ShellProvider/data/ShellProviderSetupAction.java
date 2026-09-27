package top.clspd.shellauthorization.Shared.ShellProvider.data;

public class ShellProviderSetupAction {
    private final int id;
    private final String description;
    private final String actionButtonText;

    public ShellProviderSetupAction(int id, String description, String actionButtonText) {
        this.id = id;
        this.description = description;
        this.actionButtonText = actionButtonText;
    }

    public int getId() { return id; }
    public String getDescription() { return description; }
    public String getActionButtonText() { return actionButtonText; }
}
