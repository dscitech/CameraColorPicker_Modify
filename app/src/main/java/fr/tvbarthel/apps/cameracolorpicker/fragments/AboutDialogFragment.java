package fr.tvbarthel.apps.cameracolorpicker.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import fr.tvbarthel.apps.cameracolorpicker.R;
import fr.tvbarthel.apps.cameracolorpicker.utils.Versions;

/**
 * A simple {@link android.support.v4.app.DialogFragment} for displaying some information about this application.
 */
public class AboutDialogFragment extends DialogFragment {

    private static final String URL_LICENSES = "file:///android_asset/index.html#base=40&sample=30,40,50,20";

    /**
     * Create a new instance of {@link fr.tvbarthel.apps.cameracolorpicker.fragments.AboutDialogFragment}.
     *
     * @return the newly created {@link fr.tvbarthel.apps.cameracolorpicker.fragments.AboutDialogFragment}.
     */
    public static AboutDialogFragment newInstance(ArrayList<String> list, String base) {
        AboutDialogFragment aboutDialogFragment = new AboutDialogFragment();

        Bundle args = new Bundle();
        args.putStringArrayList("list", list);
        args.putString("base", base);
        aboutDialogFragment.setArguments(args);

        return aboutDialogFragment;
    }

    /**
     * Default Constructor.
     * <p/>
     * lint [ValidFragment]
     * http://developer.android.com/reference/android/app/Fragment.html#Fragment()
     * Every fragment must have an empty constructor, so it can be instantiated when restoring its activity's state.
     */
    public AboutDialogFragment() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity activity = getActivity();
        final View dialogView = LayoutInflater.from(activity).inflate(R.layout.fragment_dialog_about, null);
        final WebView webView = (WebView) dialogView.findViewById(R.id.wb_component);
        //webView.setWebChromeClient(new WebChromeClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        Bundle arguments = getArguments();
        String base = arguments.getString("base");
        ArrayList<String> list = arguments.getStringArrayList("list");
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i < list.size() - 1) {
                stringBuilder.append(list.get(i) + ",");
            } else {
                stringBuilder.append(list.get(i));

            }
        }
        String URL_LICENSES = "file:///android_asset/index.html#base=40&sample=30,40,50,20";

        webView.loadUrl("file:///android_asset/index.html#base=" + base + "&sample=" + stringBuilder.toString());

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setView(dialogView)
                .setCancelable(true);
        //.setPositiveButton(android.R.string.ok, null);

        return builder.create();
    }
}
