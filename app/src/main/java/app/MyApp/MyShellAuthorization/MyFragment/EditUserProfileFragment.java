package app.MyApp.MyShellAuthorization.MyFragment;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Process;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.AppLogic.MainAppLogic.MyShellAuthorization.MyNative.AntiTamper;
import top.clspd.shellauthorization.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditUserProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditUserProfileFragment extends Fragment {

    public EditUserProfileFragment() {
        // Required empty public constructor
    }

    public static String getTitle(Context ctx) {
        return ctx.getString(R.string.fragment_edit_user_profile_title);
    }

    public static EditUserProfileFragment newInstance(Bundle args) {
        EditUserProfileFragment fragment = new EditUserProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            AntiTamper.AntiTamper_ValueMustWithin2(1755873346, Process.myTid(), Process.myPid(), Process.FIRST_APPLICATION_UID, Process.LAST_APPLICATION_UID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_user_profile, container, false);
    }
}