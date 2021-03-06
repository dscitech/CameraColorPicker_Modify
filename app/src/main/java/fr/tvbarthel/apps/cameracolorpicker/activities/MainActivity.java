package fr.tvbarthel.apps.cameracolorpicker.activities;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.melnykov.fab.FloatingActionButton;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import cameracolorpicker.flavors.MainActivityFlavor;
import fr.tvbarthel.apps.cameracolorpicker.R;
import fr.tvbarthel.apps.cameracolorpicker.adapters.MainPagerAdapter;
import fr.tvbarthel.apps.cameracolorpicker.data.ColorItem;
import fr.tvbarthel.apps.cameracolorpicker.data.ColorItems;
import fr.tvbarthel.apps.cameracolorpicker.fragments.AboutDialogFragment;
import fr.tvbarthel.apps.cameracolorpicker.views.ColorItemListPage;
import fr.tvbarthel.apps.cameracolorpicker.views.PaletteListPage;
import fr.tvbarthel.apps.cameracolorpicker.utils.FileSystemController;

/**
 * An {@link android.support.v7.app.AppCompatActivity} that shows the list of the colors that the user saved.
 * <p/>
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        ViewPager.OnPageChangeListener, ColorItemListPage.Listener, PaletteListPage.Listener {

    @IntDef({PAGE_ID_COLOR_ITEM_LIST, PAGE_ID_PALETTE_LIST})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PageId {
    }

    /**
     * The id associated with the color item list page.
     */
    private static final int PAGE_ID_COLOR_ITEM_LIST = 1;

    /**
     * The id associated with the palette list page.
     */
    private static final int PAGE_ID_PALETTE_LIST = 2;

    /**
     * A reference to the current {@link android.widget.Toast}.
     * <p/>
     * Used for hiding the current {@link android.widget.Toast} before showing a new one or when the activity is paused.
     * {@link }
     */
    private Toast mToast;

    /**
     * A {@link cameracolorpicker.flavors.MainActivityFlavor} for behaviors specific to flavors.
     */
    private MainActivityFlavor mMainActivityFlavor;

    /**
     * The {@link Toolbar} of this {@link MainActivity}.
     */
    private Toolbar mToolbar;

    /**
     * The {@link PagerSlidingTabStrip} for displaying the tabs.
     */
    private PagerSlidingTabStrip mTabs;

    /**
     * A {@link com.melnykov.fab.FloatingActionButton} for launching the {@link ColorPickerBaseActivity}.
     */
    private FloatingActionButton mFab;

    /**
     * A {@link ViewPager} that displays two pages: {@link ColorItemListPage} and {@link PaletteListPage}.
     */
    private ViewPager mViewPager;

    /**
     * The {@link ColorItemListPage} being displayed in the {@link ViewPager}.
     */
    private ColorItemListPage mColorItemListPage;

    /**
     * The {@link PaletteListPage} being displayed in the {@link ViewPager}.
     */
    private PaletteListPage mPaletteListPage;

    /**
     * The id of the current page selected.
     * <p/>
     * {@link fr.tvbarthel.apps.cameracolorpicker.activities.MainActivity.PageId}
     * Used for updating the icon of the {@link FloatingActionButton} when the user scrolls between pages.
     */
    private int mCurrentPageId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(MainActivity.this).setTitle(R.string.require_permission_title).setMessage(R.string.require_permission_description).setPositiveButton("好", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, 1);
                }
            }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    new AlertDialog.Builder(MainActivity.this).setTitle(R.string.require_permission_title).setMessage(R.string.permission_not_granted).setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            finish();
                        }
                    }).show();
                }
            }).show();
        }
        mToolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        mToolbar.setTitle(R.string.app_name);
        setSupportActionBar(mToolbar);

        mCurrentPageId = PAGE_ID_COLOR_ITEM_LIST;
        mColorItemListPage = new ColorItemListPage(this);
        mColorItemListPage.setListener(this);
        mPaletteListPage = new PaletteListPage(this);
        mPaletteListPage.setListener(this);

        mFab = (FloatingActionButton) findViewById(R.id.activity_main_fab);
        mFab.setOnClickListener(this);

        final MyPagerAdapter adapter = new MyPagerAdapter();
        mTabs = (PagerSlidingTabStrip) findViewById(R.id.activity_main_tabs);
        mViewPager = (ViewPager) findViewById(R.id.activity_main_view_pager);
        mViewPager.setAdapter(adapter);
        mTabs.setViewPager(mViewPager);
        mTabs.setOnPageChangeListener(this);

        mMainActivityFlavor = new MainActivityFlavor(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ColorItems.getSavedColorItems(this).size() <= 1) {
            animateFab(mFab);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideToast();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // Inflate the menu specific to the flavor.
        mMainActivityFlavor.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        boolean handled;

        switch (itemId) {
            case R.id.menu_main_action_licenses:
                handled = true;
                final EditText inputServer = new EditText(this);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle(R.string.menu_main_action_licenses).setView(inputServer).setNegativeButton(getString(R.string.cancel_operation_text), null);
                alertDialogBuilder.setPositiveButton(getString(R.string.confirm_operation_text), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String inputName = inputServer.getText().toString();
                        FileSystemController fileSystemController = new FileSystemController();
                        fileSystemController.openFileByWrite(MainActivity.this.getFilesDir() + "/common.txt", inputName);
                        String s = fileSystemController.openFileByRead(MainActivity.this.getFilesDir() + "/common.txt");
                        Log.e("jishu", s);
                        showToast(R.string.operation_successfully);
                    }
                });
                alertDialogBuilder.show();
                break;

            case R.id.menu_main_action_about:
                handled = true;
                ArrayList<String> colors = new ArrayList<>();
                FileSystemController fileSystemController = new FileSystemController();

                String s = fileSystemController.openFileByRead(MainActivity.this.getFilesDir() + "/common.txt");

                List<ColorItem> currentColors = ColorItems.getSavedColorItems(this);
                for (int i = 0; i < currentColors.size(); i++) {
                    String totlValue = getTotlValue(currentColors.get(i).getColor(), currentColors.get(0).getColor());
                    colors.add(totlValue);
                }
                AboutDialogFragment.newInstance(colors, s).show(getSupportFragmentManager(), null);
                break;

            case R.id.menu_main_action_contact_us:
                handled = true;
                for (int j = ColorItems.getSavedColorItems(MainActivity.this).size(); j > 0; j--) {
                    ColorItems.deleteColorItem(MainActivity.this, ColorItems.getSavedColorItems(MainActivity.this).get(j - 1));
                }
                Intent intent2 = getIntent();
                finish();
                startActivity(intent2);
                break;

            default:
                handled = mMainActivityFlavor.onOptionsItemSelected(item);
                if (!handled) {
                    handled = super.onOptionsItemSelected(item);
                }
        }

        return handled;
    }

    /**
     * 获取计算结果
     *
     * @param sample 样本颜色值
     * @param base   基准颜色值
     * @return
     */
    public String getTotlValue(int sample, int base) {
        double sampleValue = 0.3 * getRed(sample) + 0.59 * getGreen(sample) + 0.11 * getBlue(sample);
        double baseValue = 0.3 * getRed(base) + 0.59 * getGreen(base) + 0.11 * getBlue(base);

        DecimalFormat df = new DecimalFormat("#.00");//保留两位小数

        return df.format(sampleValue / baseValue * Integer.parseInt(getBaseValue()));
    }

    /**
     * 获取设置的基准值
     * @return
     */
    public String getBaseValue(){
        FileSystemController fileSystemController = new FileSystemController();
        String s = fileSystemController.openFileByRead(MainActivity.this.getFilesDir() + "/common.txt");
        return s;
    }

    /**
     * 获取颜色R值
     *
     * @param value
     * @return
     */
    public int getRed(int value) {
        return Color.red(value);
    }

    /**
     * 获取颜色R值
     *
     * @param value
     * @return
     */
    public int getGreen(int value) {
        return Color.green(value);
    }

    /**
     * 获取颜色B值
     *
     * @param value
     * @return
     */
    public int getBlue(int value) {
        return Color.blue(value);
    }

    @Override
    public void onClick(View v) {
        final int viewId = v.getId();

        switch (viewId) {
            case R.id.activity_main_fab:
                if (mCurrentPageId == PAGE_ID_COLOR_ITEM_LIST) {
                 /*   if (ColorItems.getSavedColorItems(this).size() >= 5) {
                        new AlertDialog.Builder(MainActivity.this).setTitle(R.string.picker_finish_title).setMessage(R.string.activity_main_error_limit_detect).setPositiveButton("好", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        }).show();
                    } else {

                    }*/
                    final Intent intentColorPickerActivity = new Intent(this, ColorPickerActivity.class);
                    startActivity(intentColorPickerActivity);
                } else if (mCurrentPageId == PAGE_ID_PALETTE_LIST) {
                    // Check if there is at least two color items.
                    // Creating a color palette with 1 or 0 colors make no sense.
                    if (ColorItems.getSavedColorItems(this).size() <= 1) {
                        onEmphasisOnPaletteCreationRequested();
                        showToast(R.string.activity_main_error_not_enough_colors);
                    } else {
                        final Intent intentColorPaletteActivity = new Intent(this, PaletteCreationActivity.class);
                        startActivity(intentColorPaletteActivity);
                    }
                }
                break;

            default:
                throw new IllegalArgumentException("View clicked unsupported. Found " + v);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        int pageId;
        if (position == 0) {
            if (positionOffset <= 0.5) {
                // In range [0; 0.5]
                // Scrolling from/to the first tab
                // We re-map the positionOffset in range [0; 1]
                // With 0 being the position where the first tab is fully visible.
                positionOffset *= 2;
                pageId = PAGE_ID_COLOR_ITEM_LIST;
            } else {
                // In range [0.5; 1]
                // Scrolling from/to the second tab
                // We re-map the positionOffset in range [0;1]
                // With 0 being the position where the second tab is fully visible.
                positionOffset = (1 - positionOffset) * 2;
                pageId = PAGE_ID_PALETTE_LIST;
            }
        } else {
            positionOffset = 0;
            pageId = PAGE_ID_PALETTE_LIST;
        }

        mFab.setTranslationY((((FrameLayout) mFab.getParent()).getHeight() - mFab.getTop()) * positionOffset);
        if (pageId != mCurrentPageId) {
            setCurrentPage(pageId);
        }
    }

    @Override
    public void onPageSelected(int position) {
        // Nothing to do.
        // The current page is already set in the onPageScrolled.
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        // Nothing to do.
    }

    @Override
    public void onEmphasisOnAddColorActionRequested() {
        animateFab(mFab, 0);
    }

    @Override
    public void onEmphasisOnPaletteCreationRequested() {
        if (ColorItems.getSavedColorItems(this).size() <= 1) {
            // needs more color to create a palette.
            mViewPager.setCurrentItem(0, true);
            animateFab(mFab, 300);
        } else {
            // touch the fab to create a palette.
            animateFab(mFab, 0);
        }
    }

    /**
     * Set the current page id.
     *
     * @param pageId the {@link fr.tvbarthel.apps.cameracolorpicker.activities.MainActivity.PageId} of the current selected page.
     */
    private void setCurrentPage(@PageId int pageId) {
        mCurrentPageId = pageId;
        if (pageId == PAGE_ID_COLOR_ITEM_LIST) {
            mFab.setImageResource(R.drawable.ic_fab_color_picker_action);
        } else if (pageId == PAGE_ID_PALETTE_LIST) {
            mFab.setImageResource(R.drawable.ic_fab_palette_creation_action);
        }
    }

    /**
     * Hide the current {@link android.widget.Toast}.
     */
    private void hideToast() {
        if (mToast != null) {
            mToast.cancel();
            mToast = null;
        }
    }

    /**
     * Show a toast text message.
     *
     * @param resId The resource id of the string resource to use.
     */
    private void showToast(@StringRes int resId) {
        hideToast();
        String toastText = getString(resId);
        if (!TextUtils.isEmpty(toastText)) {
            mToast = Toast.makeText(this, resId, Toast.LENGTH_SHORT);
            mToast.show();
        }
    }

    /**
     * Make a subtle animation for a {@link com.melnykov.fab.FloatingActionButton} drawing attention to the button.
     * Apply a default delay of 400ms.
     * <p/>
     * See also : {@link MainActivity#animateFab(FloatingActionButton, int)}
     *
     * @param fab the {@link com.melnykov.fab.FloatingActionButton} to animate.
     */
    private void animateFab(final FloatingActionButton fab) {
        animateFab(fab, 400);
    }

    /**
     * Make a subtle animation for a {@link com.melnykov.fab.FloatingActionButton} drawing attention to the button.
     *
     * @param fab   the {@link com.melnykov.fab.FloatingActionButton} to animate.
     * @param delay delay before the animation start.
     */
    private void animateFab(final FloatingActionButton fab, int delay) {
        fab.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Play a subtle animation
                final long duration = 300;

                final ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(fab, View.SCALE_X, 1f, 1.2f, 1f);
                scaleXAnimator.setDuration(duration);
                scaleXAnimator.setRepeatCount(1);

                final ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(fab, View.SCALE_Y, 1f, 1.2f, 1f);
                scaleYAnimator.setDuration(duration);
                scaleYAnimator.setRepeatCount(1);

                scaleXAnimator.start();
                scaleYAnimator.start();

                final AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.play(scaleXAnimator).with(scaleYAnimator);
                animatorSet.start();
            }
        }, delay);
    }

    private class MyPagerAdapter extends MainPagerAdapter {

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.activity_main_view_pager_title_color);

                case 1:
                    return getString(R.string.activity_main_view_pager_title_palette);

                default:
                    return getString(R.string.activity_main_view_pager_title_unknown);
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final View view;

            if (position == 0) {
                view = mColorItemListPage;
            } else if (position == 1) {
                view = mPaletteListPage;
            } else {
                throw new IllegalArgumentException("Invalid position. Positions supported are 0 & 1, found " + position);
            }

            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}
