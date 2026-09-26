package app.MyApp.MyShellAuthorization.MyFragment;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;

import app.MyApp.MyShellAuthorization.MyDataStructures.ShellProviderDeclaration;
import top.clspd.shellauthorization.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShellProviderDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShellProviderDetailsFragment extends Fragment {

    public static final String ARG_FILE = "file";

    private File file;

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
            String path = getArguments().getString(ARG_FILE);
            if (path != null) file = new File(path);
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

        ShellProviderDeclaration declaration = file == null ? null : ShellProviderDeclaration.read(file);
        if (declaration == null) {
            Toast.makeText(requireContext(), getString(R.string.invalid_parameter_error), Toast.LENGTH_SHORT).show();
            requireActivity().finish();
            return;
        }

        TextView type = view.findViewById(R.id.textView_shellProviderType);
        TextView info = view.findViewById(R.id.textView_shellProviderInfo);
        switch (declaration.type) {
            case ShellProviderDeclaration.TYPE_ROOT:
                type.setText(getString(R.string.shell_provider_type_root));
                info.setText(getString(R.string.shell_provider_root_su_format,
                        declaration.su == null ? "" : declaration.su));
                break;
            case ShellProviderDeclaration.TYPE_SHIZUKU:
                type.setText(getString(R.string.shell_provider_type_shizuku));
                info.setText(getString(R.string.shell_provider_shizuku_version_format,
                        declaration.getShizukuVersion()));
                break;
            default:
                type.setText(declaration.type);
                info.setText("");
                break;
        }

        view.findViewById(R.id.button_deleteShellProvider).setOnClickListener(v ->
            new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.are_you_sure_ask))
                .setMessage(getString(R.string.shell_provider_delete_confirm))
                .setPositiveButton(getString(R.string.Yes), (dialog, which) -> {
                    if (file != null) file.delete();
                    requireActivity().finish();
                })
                .setNegativeButton(getString(R.string.No), null)
                .show());
    }
}
