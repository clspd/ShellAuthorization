package top.clspd.shellauthorization.Shared.ShellProvider.intf;

import android.content.Context;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import top.clspd.shellauthorization.Shared.ShellProvider.data.ShellProviderParameter;
import top.clspd.shellauthorization.Shared.ShellProvider.data.ShellProviderSetupAction;

public interface ShellProviderInterface extends ShellInterface {
    /**
     * Save the context to somewhere for later use.
     * @param ctx Context
     */
    public void setContext(Context ctx);

    /**
     * Get the name of the provider.
     * @return A determinant value like "root" or "shell".
     */
    public String getProviderName();

    /**
     * Generate provider identifier
     * for unique providers like RootProvider and ShizukuProvider, this
     * returns a determinant value like "root"; for user-customizable provider,
     * this returns a unique thing like a randomly-generated UUID.
     * @return the provider identifier.
     */
    public String generateProviderIdentifier();

    /**
     * Get the provider icon resource identifier. Will be used in add provider selection.
     * @return The identifier.
     */
    @DrawableRes int getProviderIconResourceIdentifier();

    /**
     * Get the user-friendly name of the provider. Recommended to load from R.string.
     * Will be used in add provider selection.
     * @return A determinant, user-friendly value like "Root" or "Shell".
     */
    public String getProviderFriendlyName();

    /**
     * Whether the friendly name can be edited by the user.
     * If true, user can customize the friendly name.
     * @return the config
     */
    public boolean isProviderFriendlyNameEditableByUser();

    /**
     * Get the description of the provider. Recommended to load from R.string.
     * Will be used in add provider selection.
     * @return the description
     */
    public String getProviderDescription();
    public String getProviderDetailedDescription();

    /**
     * Get required parameters of this provider.
     * For example, for Root provider, the required parameter will be a "su path".
     * @return the list of required parameters.
     */
    public List<ShellProviderParameter> getRequiredParameters();

    public List<ShellProviderParameter> getParameters();
    @Nullable
    public ShellProviderParameter getParameter(String key);
    public void setParameters(List<ShellProviderParameter> parameters);
    public void setParameter(String key, ShellProviderParameter value);

    // Design: the parameters will be the only required thing to let the provider work.
    // The higher application flow: Loads all registered providers, load them to the user interface,
    // then we will build the user interface dynamically and let user choose one provider to add.
    // Then, a new dialog will open and the detailed description will be shown.
    // The dialog contains the parameters field (if it has), and the action buttons (if it has).
    // When user completed all parameters field and clicked and finished all action buttons,
    // save button is available. Finally, the parameters are saved to database.
    // When it comes to load, setParameters will be called to restore the state.

    /**
     * Get required actions of this provider.
     * An "action" is a special function that does some initialization or permission check,
     * for example, for Shizuku provider, the user need to authorize first so that we can use it.
     * @return the list of required actions.
     */
    public List<ShellProviderSetupAction> getRequiredSetupActions();

    /**
     * Perform the specified setup action. This will be triggered when the user clicked the action button.
     * @param actionId The identifier of the action. Same as the value specified in ShellProviderSetupAction.
     * @return A completable future indicating whether the action is successfully finished. The user interface show progress dialog during this progress.
     */
    public CompletableFuture<Boolean> performSetupAction(int actionId);
}
