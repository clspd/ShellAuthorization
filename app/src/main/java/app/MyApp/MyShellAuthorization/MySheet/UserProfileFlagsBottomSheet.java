package app.MyApp.MyShellAuthorization.MySheet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.materialswitch.MaterialSwitch;

import top.clspd.shellauthorization.R;

public class UserProfileFlagsBottomSheet extends BottomSheetDialogFragment {

    public static final String RESULT_KEY = "profile_flags";
    public static final String RESULT_NO_NEW_PRIVS = "no_new_privs";

    private static final String ARG_NO_NEW_PRIVS = "no_new_privs";

    private MaterialSwitch noNewPrivsSwitch;

    public static UserProfileFlagsBottomSheet newInstance(boolean noNewPrivs) {
        Bundle args = new Bundle();
        args.putBoolean(ARG_NO_NEW_PRIVS, noNewPrivs);
        UserProfileFlagsBottomSheet sheet = new UserProfileFlagsBottomSheet();
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_user_profile_flags, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayout container = view.findViewById(R.id.container_flags);
        noNewPrivsSwitch = (MaterialSwitch) LayoutInflater.from(requireContext())
                .inflate(R.layout.item_user_profile_flag, container, false);
        noNewPrivsSwitch.setText(R.string.edit_user_profile_flag_no_new_privs);
        boolean checked = getArguments() != null
                && getArguments().getBoolean(ARG_NO_NEW_PRIVS, false);
        noNewPrivsSwitch.setChecked(checked);
        container.addView(noNewPrivsSwitch);

        Button ok = view.findViewById(R.id.button_ok);
        ok.setOnClickListener(v -> deliverResult());
        Button cancel = view.findViewById(R.id.button_cancel);
        cancel.setOnClickListener(v -> dismiss());
    }

    private void deliverResult() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(RESULT_NO_NEW_PRIVS, noNewPrivsSwitch != null && noNewPrivsSwitch.isChecked());
        getParentFragmentManager().setFragmentResult(RESULT_KEY, bundle);
        dismiss();
    }
}
