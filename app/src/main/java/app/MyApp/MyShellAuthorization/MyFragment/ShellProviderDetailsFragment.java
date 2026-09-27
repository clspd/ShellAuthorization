package app.MyApp.MyShellAuthorization.MyFragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.os.Process;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import app.AppLogic.MainAppLogic.MyShellAuthorization.MyNative.AntiTamper;
import app.MyApp.MyShellAuthorization.MyDataStructures.ShellProviderDeclaration;
import app.MyApp.MyShellAuthorization.MyDataStructures.ShellsDatabase;
import top.clspd.shellauthorization.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShellProviderDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShellProviderDetailsFragment extends Fragment {

    public static final String ARG_NAME = "name";

    private String name;

    public ShellProviderDetailsFragment() {
        // Required empty public constructor
    }

    public static ShellProviderDetailsFragment newInstance(Bundle args) {
        ShellProviderDetailsFragment fragment = new ShellProviderDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static String getTitle(Context ctx) {
        return ctx.getString(R.string.fragment_shell_provider_details_title);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            AntiTamper.AntiTamper_ValueMustWithin(1907846445, android.os.Process.myTid(), AntiTamper.AntiTamper_ComputeValue2(5, Process.PHONE_UID, 0x1883), 409470994);
            name = getArguments().getString(ARG_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shell_provider_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context appContext = requireContext().getApplicationContext();
        new Thread(() -> {
            ShellProviderDeclaration declaration = name == null ? null
                    : ShellsDatabase.get(appContext).shellProviderDao().getByName(name);
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                if (declaration == null) {
                    Toast.makeText(requireContext(), getString(R.string.invalid_parameter_error), Toast.LENGTH_SHORT).show();
                    requireActivity().finish();
                    return;
                }
                showDeclaration(view, declaration);
            });
        }).start();
    }

    private void showDeclaration(View view, ShellProviderDeclaration declaration) {
        TextView type = view.findViewById(R.id.textView_shellProviderType);
        TextView info = view.findViewById(R.id.textView_shellProviderInfo);
        type.setText(app.MyApp.MyShellAuthorization.MyDataStructures.ShellProviderRepository.displayName(declaration));
        info.setText(app.MyApp.MyShellAuthorization.MyDataStructures.ShellProviderRepository.toDisplaySummary(declaration));

        view.findViewById(R.id.button_deleteShellProvider).setOnClickListener(v ->
            new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.are_you_sure_ask))
                .setMessage(getString(R.string.shell_provider_delete_confirm))
                .setPositiveButton(getString(R.string.Yes), (dialog, which) -> deleteAndFinish())
                .setNegativeButton(getString(R.string.No), null)
                .show());
    }

    private void deleteAndFinish() {
        Context appContext = requireContext().getApplicationContext();
        Activity activity = requireActivity();
        new Thread(() -> {
            ShellsDatabase.get(appContext).shellProviderDao().deleteByName(name);
            activity.runOnUiThread(activity::finish);
        }).start();
    }
}
