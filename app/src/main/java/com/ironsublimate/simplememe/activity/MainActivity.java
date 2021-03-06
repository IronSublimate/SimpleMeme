package com.ironsublimate.simplememe.activity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.folderselector.FileChooserDialog;
//import com.baidu.ocr.sdk.OCR;
//import com.baidu.ocr.sdk.OnResultListener;
//import com.baidu.ocr.sdk.exception.OCRError;
//import com.baidu.ocr.sdk.model.AccessToken;
import com.blankj.ALog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.canking.minipay.Config;
import com.canking.minipay.MiniPayUtils;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.ironsublimate.simplememe.GlobalConfig;
import com.ironsublimate.simplememe.MyDataBase;
import com.ironsublimate.simplememe.MySharePreference;
import com.ironsublimate.simplememe.R;
import com.ironsublimate.simplememe.adapter.ViewPagerAdapter;
import com.ironsublimate.simplememe.bean.EventMessage;
import com.ironsublimate.simplememe.bean.Expression;
import com.ironsublimate.simplememe.bean.ExpressionFolder;
import com.ironsublimate.simplememe.bean.OneDetail;
import com.ironsublimate.simplememe.bean.OneDetailList;
import com.ironsublimate.simplememe.callback.GestureListener;
import com.ironsublimate.simplememe.callback.RemoveCacheListener;
import com.ironsublimate.simplememe.callback.GetMainExpListener;
import com.ironsublimate.simplememe.callback.TaskListener;
import com.ironsublimate.simplememe.fragment.ExpressionContentFragment;
//import com.ironsublimate.simplememe.http.HttpUtil;
import com.ironsublimate.simplememe.task.CheckUpdateTask;
import com.ironsublimate.simplememe.task.GenerateScreenshotTask;
import com.ironsublimate.simplememe.task.RecoverDataTask;
import com.ironsublimate.simplememe.task.RemoveCacheTask;
import com.ironsublimate.simplememe.task.GetExpFolderTask;
import com.ironsublimate.simplememe.task.ScanAllIamgeTask;
import com.ironsublimate.simplememe.util.APKVersionCodeUtils;
import com.ironsublimate.simplememe.util.CheckPermissionUtils;
import com.ironsublimate.simplememe.util.DataCleanManager;
import com.ironsublimate.simplememe.util.DateUtil;
import com.ironsublimate.simplememe.util.FileUtil;
import com.ironsublimate.simplememe.util.ToastUtil;
import com.ironsublimate.simplememe.util.UIUtil;
import com.ironsublimate.simplememe.view.CustomImageView;
import com.ironsublimate.simplememe.view.ExpImageDialog;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.LitePal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import pub.devrel.easypermissions.EasyPermissions;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, FileChooserDialog.FileCallback {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tab_layout)
    TabLayout tabLayout;
    @BindView(R.id.viewPager)
    ViewPager viewPager;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.main_item)
    CoordinatorLayout mainItem;
    @BindView(R.id.top_image)
    CustomImageView topImage;
    @BindView(R.id.one_text)
    TextView oneText;
    @BindView(R.id.add_exp)
    ImageView addExp;
    @BindView(R.id.fab_search)
    FloatingActionButton fabSearch;
    @BindView(R.id.appbar)
    AppBarLayout appbar;
    @BindView(R.id.search_input)
    EditText searchInput;


    private Drawer result;
    private List<ExpressionFolder> expressionFolderList = new ArrayList<>();

    //??????
    private long lastClickTime = -1;
    private long thisClickTime = -1;
    private int clickTimes = 0;
    long startTime = 0;


    private MenuItem refreshItem;

    private int oneItem = 0;//one?????????

    private ViewPagerAdapter adapter;

    private SecondaryDrawerItem removeCache;
    private CheckUpdateTask checkUpdateTask;

    private boolean isSearching;//???????????????????????????

    /**
     * ??????????????????????????????
     *
     * @param activity
     */
    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        EventBus.getDefault().register(this);


        //??????????????????????????????
        initView(savedInstanceState);


        //???????????????
        initData();


        //????????????????????????
        setTabLayout(false);

        //?????????????????????
        initPermission();

        //?????????
        initListener();

        getOne(refreshItem);

        //??????????????????
        setCacheSize();

        //?????????????????????????????????
        initAccessTokenWithAkSk();

        initTapView();

        initDefaultFolder();

    }


    /**
     * ????????????????????????
     * ???????????????????????????apk??????????????????????????????????????????
     */
    private void initData() {

    }

    /**
     * ???????????????
     *
     * @param savedInstanceState
     */
    private void initView(Bundle savedInstanceState) {

        //??????????????????
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withCompactStyle(false)
                .withHeaderBackground(R.drawable.header)
                .withSavedInstance(savedInstanceState)
                .withOnAccountHeaderSelectionViewClickListener(new AccountHeader.OnAccountHeaderSelectionViewClickListener() {
                    @Override
                    public boolean onClick(View view, IProfile profile) {
                        if (lastClickTime == -1) {
                            lastClickTime = System.currentTimeMillis();
                            thisClickTime = System.currentTimeMillis();
                            ToastUtil.showMessageShort("?????????????????????");
                        } else {//????????????????????????
                            thisClickTime = System.currentTimeMillis();
                            if (thisClickTime - lastClickTime < 500) {//??????0.8???????????????
                                lastClickTime = thisClickTime;
                                clickTimes++;
                                UIUtil.goodEgg(clickTimes, new TaskListener() {
                                    @Override
                                    public void onFinish(Object result2) {
                                        result.closeDrawer();//???????????????
                                    }
                                });
                            } else {//??????????????????????????????????????????????????????
                                lastClickTime = -1;
                                thisClickTime = -1;
                                clickTimes = 0;
                            }

                        }
                        return false;
                    }
                })
                .build();
        removeCache = new SecondaryDrawerItem().withName("????????????").withIcon(GoogleMaterial.Icon.gmd_delete).withSelectable(false);

        result = new DrawerBuilder()
                .withActivity(this)
                .withAccountHeader(headerResult)
                .withToolbar(toolbar)
                .withFullscreen(true)
                .withSelectedItem(-1)
                .addDrawerItems(
                        new SecondaryDrawerItem().withName("????????????").withIcon(GoogleMaterial.Icon.gmd_home).withSelectable(false),//1
                        new SecondaryDrawerItem().withName("????????????").withIcon(GoogleMaterial.Icon.gmd_photo_library).withSelectable(false),//3
                        new SecondaryDrawerItem().withName("????????????").withIcon(GoogleMaterial.Icon.gmd_scanner).withSelectable(false),//4
                        removeCache,
                        new SecondaryDrawerItem().withName("????????????").withIcon(GoogleMaterial.Icon.gmd_file_download).withSelectable(false),//5
                        new SecondaryDrawerItem().withName("????????????").withIcon(GoogleMaterial.Icon.gmd_backup).withSelectable(false),//6
                        new DividerDrawerItem(),//7
                        new SecondaryDrawerItem().withName("????????????").withIcon(R.drawable.logo).withSelectable(false),//8
                        new SecondaryDrawerItem().withName("????????????").withIcon(GoogleMaterial.Icon.gmd_favorite).withSelectable(false),//9
                        new SecondaryDrawerItem().withName("????????????").withIcon(GoogleMaterial.Icon.gmd_payment).withSelectable(false),//10
                        new SecondaryDrawerItem().withName("????????????").withIcon(GoogleMaterial.Icon.gmd_system_update_alt).withSelectable(false).withDescription("v" + APKVersionCodeUtils.getVerName(MainActivity.this) + "(" + APKVersionCodeUtils.getVersionCode(MainActivity.this) + ")"),//11
                        new SecondaryDrawerItem().withName("????????????").withIcon(GoogleMaterial.Icon.gmd_exit_to_app).withSelectable(false)//12
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        switch (position) {
                            case 1://???????????????????????????????????????
                                result.closeDrawer();
                                break;
                            case 3://????????????
                                //ShopActivity.actionStart(MainActivity.this);
                                ScanAllIamgeTask task = new ScanAllIamgeTask(MainActivity.this);
                                task.execute();
                                break;
                            case 2: //????????????????????????
                                MyActivity.actionStart(MainActivity.this);
                                break;
                            case 4://????????????
                                MaterialDialog dialog;
                                new MaterialDialog.Builder(MainActivity.this)
                                        .title("????????????")
                                        .content("????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????")
                                        .positiveText("??????")
                                        .negativeText("????????????????????????????????????")
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                new RemoveCacheTask(MainActivity.this, new RemoveCacheListener() {
                                                    @Override
                                                    public void onFinish() {
                                                        setCacheSize();
                                                    }
                                                }).execute();
                                            }
                                        })
                                        .show();
                                break;

                            case 5://????????????
                                new MaterialDialog.Builder(MainActivity.this)
                                        .title("????????????????????????")
                                        .content("????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????\n\n" +
                                                "????????????????????????[????????????]??????????????????????????????????????????????????????\n\n" +
                                                "????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????")
                                        .positiveText("????????????")
                                        .negativeText("??????")
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                FileUtil.copyFileToTarget(MainActivity.this.getDatabasePath("expBaby.db").getAbsolutePath(), GlobalConfig.appDirPath + "database/" + DateUtil.getNowDateStr() + ".db");
                                                Toasty.info(MainActivity.this, "??????????????????", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .show();
                                break;
                            case 6://????????????
                                //??????database???????????????????????????
                                new RecoverDataTask(MainActivity.this).execute();
                                break;

                            case 8://????????????
                                AboutActivity.actionStart(MainActivity.this);
                                break;
                            case 9://????????????
                                Uri uri = Uri.parse("market://details?id=" + UIUtil.getContext().getPackageName());
                                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                                try {
                                    startActivity(goToMarket);
                                } catch (ActivityNotFoundException e) {
                                    Toasty.error(MainActivity.this, "????????????????????????????????????", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case 10://??????
                                MiniPayUtils.setupPay(MainActivity.this, new Config.Builder("fkx15914zyuu4rnmbzr2td5", R.drawable.alipay, R.drawable.wechatpay).build());
                                break;

                            case 11://????????????
                                checkUpdateTask = new CheckUpdateTask(MainActivity.this, getPackageManager());
                                checkUpdateTask.execute();
                                break;
                            case 12://????????????
                                finish();
                                break;
                        }

                        return true;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .withOnDrawerListener(new Drawer.OnDrawerListener() {
                    @Override
                    public void onDrawerOpened(View drawerView) {
                        setCacheSize();
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {

                    }

                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {

                    }
                })
                .build();


        //?????????TabLayout
        initTabLayout();


    }


    /**
     * ?????????TabLayout ??????
     */
    private void initTabLayout() {
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(ExpressionContentFragment.fragmentInstant("??????", true, 0));
        List<String> pageTitleList = new ArrayList<>();
        pageTitleList.add("??????");
        adapter = new ViewPagerAdapter(getSupportFragmentManager(), fragments, pageTitleList);
        //??????ViewPager
        viewPager.setAdapter(adapter);
        bindTabWithViewPager();
    }

    private void setTabLayout(boolean isUpdate) {
        //??????viewPager
        setViewPager(viewPager, isUpdate);
        bindTabWithViewPager();
    }

    private void bindTabWithViewPager() {
        //tabLayout??????
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        viewPager.setOffscreenPageLimit(1);//?????????????????????????????????????????????1??????????????????????????????????????????????????????
    }


    /**
     * ??????ViewPager
     */
    private void setViewPager(final ViewPager viewPager, boolean isUpdate) {
        if (isUpdate) {
            viewPager.removeAllViewsInLayout();
        }
        ALog.d("??????????????????" + expressionFolderList.size());

        new GetExpFolderTask(new GetMainExpListener() {
            @Override
            public void onFinish(List<Fragment> fragmentList, List<String> pageTitleList) {
                //???????????????
                adapter = new ViewPagerAdapter(getSupportFragmentManager(), fragmentList, pageTitleList);
                //??????ViewPager
                viewPager.setAdapter(adapter);
            }
        }).execute();

    }


    /**
     * ?????????ak???sk?????????
     */
    private void initAccessTokenWithAkSk() {
//        OCR.getInstance(this).initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
//            @Override
//            public void onResult(AccessToken result) {
//                String token = result.getAccessToken();
//            }
//
//            @Override
//            public void onError(OCRError error) {
//                error.printStackTrace();
//                MainActivity.this.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toasty.info(MainActivity.this, "????????????????????????????????????").show();
//                    }
//                });
//            }
//        }, getApplicationContext(), "6AsWoPOwdFEn5G17glMkGFVd", "014yBWxaRMBaQRnZD5Brg83sAzujGNOK");
    }


    private void initTapView() {
        if (MySharePreference.getUserUsedStatus("isFirstEnter") == 0) {
            toolbar.inflateMenu(R.menu.menu_main);
            new TapTargetSequence(this)
                    .targets(TapTarget.forToolbarMenuItem(toolbar, R.id.refresh, "?????? ?????????one", "????????????????????????6?????????????????????one???????????????\n\n?????????????????????????????????")
                                    .cancelable(false)
                                    .drawShadow(true)
                                    .titleTextColor(R.color.text_primary_dark)
                                    .descriptionTextColor(R.color.text_secondary_dark)
                                    .tintTarget(true)
                                    .targetCircleColor(android.R.color.black)//???????????????
                                    .id(1),

                            TapTarget.forView(findViewById(R.id.fab_search), "??????????????????????????????", "???????????????????????????????????????????????????\n\n????????????????????????????????????????????????")
                                    .textTypeface(Typeface.SANS_SERIF) //????????????
                                    .drawShadow(true).cancelable(false).tintTarget(true)//
                                    .icon(getResources().getDrawable(R.drawable.ic_search_black_24dp))//??????target??????
                                    .targetCircleColor(android.R.color.black)//???????????????
                                    .titleTextColor(R.color.text_primary_dark)
                                    .descriptionTextColor(R.color.text_secondary_dark).id(2),

                            TapTarget.forView(findViewById(R.id.add_exp), "?????????????????????", "???????????????????????????????????????????????????\n\n?????????????????????????????????????????????")
                                    .textTypeface(Typeface.SANS_SERIF) //????????????
                                    .drawShadow(true)
                                    .cancelable(false)
                                    .tintTarget(true)//
                                    .icon(getResources().getDrawable(R.drawable.ic_add_black_24dp))//??????target??????
                                    .targetCircleColor(android.R.color.black)//???????????????
                                    .titleTextColor(R.color.text_primary_dark)
                                    .descriptionTextColor(R.color.text_secondary_dark).id(3)

                    )
                    .listener(new TapTargetSequence.Listener() {
                        @Override
                        public void onSequenceFinish() {

                        }

                        @Override
                        public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                            switch (lastTarget.id()) {
                                case 1:
                                    getOne(refreshItem);
                                    break;
                                case 2:
                                    fabSearch.performClick();
                                    break;
                                case 3:
//                                    ShopActivity.actionStart(MainActivity.this);
                                    break;
                            }
                        }

                        @Override
                        public void onSequenceCanceled(TapTarget lastTarget) {

                        }
                    })
                    .start();
        }
    }

    private void initListener() {

        //???????????????????????????
        topImage.setLongClickable(true);
        topImage.setOnTouchListener(new MyGestureListener(this));

        fabSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSearching) {
                    if (Objects.equals(searchInput.getText().toString(), "")) {
                        isSearching = false;
                        searchInput.setVisibility(View.GONE);
                    } else {
                        ResultActivity.actionStart(MainActivity.this, searchInput.getText().toString());
                    }

                } else {
                    searchInput.setVisibility(View.VISIBLE);
                    isSearching = true;
                }

            }
        });
        addExp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyActivity.actionStart(MainActivity.this);
            }
        });

        oneText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager clipboardManager = (ClipboardManager) MainActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                clipboardManager.setPrimaryClip(ClipData.newPlainText(null, oneText.getText()));
                Toasty.success(MainActivity.this, "????????????", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }


    private void initPermission() {
        String[] notPermission = CheckPermissionUtils.checkPermission(UIUtil.getContext());
        if (notPermission.length != 0) {//????????????????????????????????????
            ActivityCompat.requestPermissions(this, notPermission, 100);
        }
    }

    private void setCacheSize() {
        //????????????????????????(/data/data/com.example.androidclearcache/cache)
        final File file = new File(getCacheDir().getPath());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String cacheSize = DataCleanManager.getCacheSize(file);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ALog.d("cahceSize", cacheSize);
                            removeCache.withDescription(cacheSize);
                            result.updateItem(removeCache);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }


    private void updateData() {
        expressionFolderList = LitePal.findAll(ExpressionFolder.class, true);
    }


    /**
     * ????????????????????????
     *
     * @param item
     */
    private void getOne(MenuItem item) {
        if (item != null) {
            //??????????????????
            showRefreshAnimation(item);
        }

        if (MyDataBase.isNeedGetOnes()) {//?????????????????????????????????????????????????????????
//            HttpUtil.getOnes(new Callback<OneDetailList>() {
//                @Override
//                public void onResponse(@NonNull Call<OneDetailList> call, @NonNull Response<OneDetailList> response) {
//
//                    //???????????????????????????????????????
//                    LitePal.deleteAll(OneDetailList.class);
//                    LitePal.deleteAll(OneDetail.class);
//
//                    //??????????????????
//                    final OneDetailList oneDetailList = response.body();
//                    if (oneDetailList != null){
//                        oneDetailList.save();
//
//                        for (int i = 0; i < oneDetailList.getCount(); i++) {
//                            OneDetail oneDetail = oneDetailList.getOneDetailList().get(i);
//                            oneDetail.setOneDetailList(oneDetailList);
//                            oneDetail.save();
//                        }
//
//                        setOneUI(oneDetailList);
//                    }
//                }
//
//                @Override
//                public void onFailure(@NonNull Call<OneDetailList> call, @NonNull Throwable t) {
//                    //???????????????
//                    Toasty.error(MainActivity.this, "????????????????????????????????????", Toast.LENGTH_SHORT).show();
//                    ALog.d("????????????" + t.getMessage());
//                }
//            });
        } else {
            setOneUI(LitePal.findFirst(OneDetailList.class, true));
        }

    }

    /**
     * ????????????????????????
     *
     * @param oneDetailLists
     */
    private void setOneUI(final OneDetailList oneDetailLists) {
        final List<OneDetail> oneDetailList = oneDetailLists.getOneDetailList();
        final int currentItem = oneItem % oneDetailList.size();
        OneDetail oneDetail = oneDetailList.get(currentItem);
        oneText.setText(oneDetail.getText());

        Glide.with(this).load(oneDetail.getImgUrl())
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        topImage.setImageDrawable(resource);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
//                                    sleep(1500);
                                    sleep(0);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        hideRefreshAnimation();
                                    }
                                });
                            }
                        }).start();
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        Toasty.error(MainActivity.this, "????????????????????????????????????", Toast.LENGTH_SHORT).show();
                    }
                });
        oneItem++;//?????????????????????????????????

        topImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //????????????
                final Expression expression = new Expression(3, oneDetailLists.getDate().substring(0, 10) + (currentItem) + ".jpg", oneDetailList.get(currentItem).getImgUrl(), "??????");
                final ExpImageDialog expImageDialog = new ExpImageDialog.Builder(MainActivity.this)
                        .setContext(MainActivity.this, null, 3)
                        .build();
                expImageDialog.setImageData(expression);

                //??????????????????????????????
                File file = new File(GlobalConfig.appDirPath + expression.getFolderName() + "/" + expression.getName());
                if (file.exists()) {
                    expImageDialog.show();
                } else {
                    new GenerateScreenshotTask(MainActivity.this, oneText.getText().toString(), expression, new TaskListener() {
                        @Override
                        public void onFinish(Object result) {
                            expImageDialog.show();
                        }
                    }).execute();
                }

            }
        });
    }

    /**
     * ??????????????????
     *
     * @param item
     */
    public void showRefreshAnimation(MenuItem item) {

        hideRefreshAnimation();
        refreshItem = item;

        //??????????????????ImageView?????????MenuItem???ActionView????????????????????????????????????ImageView?????????????????????
        View refreshActionView = getLayoutInflater().inflate(R.layout.item_refresh_menu, null);

        item.setActionView(refreshActionView);

        Animation rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate);
        refreshActionView.setAnimation(rotateAnimation);
        refreshActionView.startAnimation(rotateAnimation);
    }

    /**
     * ??????????????????
     */
    private void hideRefreshAnimation() {
        if (refreshItem != null) {
            View view = refreshItem.getActionView();
            if (view != null) {
                view.clearAnimation();
                refreshItem.setActionView(null);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        refreshItem = menu.findItem(R.id.refresh);
        showRefreshAnimation(refreshItem);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            getOne(item);
        } else if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 124) {
            //????????????????????????
            new MaterialDialog.Builder(this)
                    .title("????????????")
                    .content("???????????????????????????????????????????????????????????????app?????????????????????????????????????????????")
                    .positiveText("??????")
                    .negativeText("???????????????")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                dialog.dismiss();
                                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                                startActivityForResult(intent, 125);
                            } else {
                                dialog.dismiss();
                                Toasty.info(MainActivity.this, "?????????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();

                            }
                        }
                    })
                    .show();
        } else {
            EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
        }
    }


    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        if (requestCode == 100) {
            //?????????????????????
            Toasty.success(UIUtil.getContext(), "????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
        } else if (requestCode == 124) {
            checkUpdateTask.installApk();
        }

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // ???????????????
        if (requestCode == 100) {
            Toasty.error(UIUtil.getContext(), "?????????????????????????????????????????????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
        } else if (requestCode == 124) {
            Toasty.error(UIUtil.getContext(), "android 8.0???????????????????????????????????????", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onBackPressed() {

        if (isSearching) {
            searchInput.setVisibility(View.GONE);
            isSearching = false;
            searchInput.setText("");
        } else {
            if (result.isDrawerOpen()) {
                result.closeDrawer();
            } else {
                long currentTime = System.currentTimeMillis();
                if ((currentTime - startTime) >= 2000) {
                    Toast.makeText(MainActivity.this, "??????????????????", Toast.LENGTH_SHORT).show();
                    startTime = currentTime;
                } else {
                    finish();
                }
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void refreshUI(EventMessage eventBusMessage) {
        if (Objects.equals(eventBusMessage.getType(), EventMessage.DATABASE) || Objects.equals(eventBusMessage.getType(), EventMessage.MAIN_DATABASE)) {
            updateData();
            setTabLayout(true);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 125) {
            checkUpdateTask.installApk();
        }
    }

    @Override
    public void onFileSelection(@NonNull FileChooserDialog dialog, @NonNull File file) {
        ALog.d("???????????????" + file.getAbsolutePath());
        ALog.d(file.getParentFile().getAbsolutePath() + "|" + GlobalConfig.appDirPath + "database");
        boolean isExist = false;//????????????????????????????????????????????????
        if (Objects.equals(file.getParentFile().getAbsolutePath(), GlobalConfig.appDirPath + "database")) {
            if (file.exists()) {
                isExist = true;
                ALog.d("????????????????????????");
            }
        }
        if (!isExist) {
            FileUtil.copyFileToTarget(file.getAbsolutePath(), GlobalConfig.appDirPath + "database" + "/" + file.getName());
        }

        ALog.d("AAA" + GlobalConfig.appDirPath + "database" + "/" + file.getName());
        FileUtil.copyFileToTarget(GlobalConfig.appDirPath + "database" + "/" + file.getName(), this.getDatabasePath("expBaby.db").getAbsolutePath());
        EventBus.getDefault().post(new EventMessage(EventMessage.DATABASE));
        Toasty.success(this, "??????????????????").show();
    }

    @Override
    public void onFileChooserDismissed(@NonNull FileChooserDialog dialog) {

    }

    /**
     * ??????GestureListener?????????left???right??????
     */
    private class MyGestureListener extends GestureListener {
        public MyGestureListener(Context context) {
            super(context);
        }

        @Override
        public boolean left() {
            Toasty.info(MainActivity.this, "?????????????????????????????????????????????").show();
            return super.left();
        }

        @Override
        public boolean right() {
            Toasty.info(MainActivity.this, "?????????????????????????????????????????????").show();
            return super.right();
        }
    }

    //????????????????????????????????????????????????
    private void initDefaultFolder() {
        // Do something
        for (String name : new String[]{this.getString(R.string.default_meme_folder), this.getString(R.string.favourite_meme_folder)}) {
            List<ExpressionFolder> temExpFolderList = LitePal.where("name = ?", name).find(ExpressionFolder.class);
            if (temExpFolderList.size() > 0) {

            } else {
                ExpressionFolder expressionFolder = new ExpressionFolder(1, 0, name, null, null, DateUtil.getNowDateStr(), null, null, -1);
                expressionFolder.save();
                UIUtil.autoBackUpWhenItIsNecessary();
                EventBus.getDefault().post(new EventMessage(EventMessage.DATABASE));
            }
        }
    }

}


