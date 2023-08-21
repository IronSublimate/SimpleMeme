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
//    @BindView(R.id.top_image)
//    CustomImageView topImage;
//    @BindView(R.id.one_text)
//    TextView oneText;
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

    //毫秒
    private long lastClickTime = -1;
    private long thisClickTime = -1;
    private int clickTimes = 0;
    long startTime = 0;


    private MenuItem refreshItem;

    private int oneItem = 0;//one的序号

    private ViewPagerAdapter adapter;

    private SecondaryDrawerItem removeCache;
    private CheckUpdateTask checkUpdateTask;

    private boolean isSearching;//是否打开了搜索功能

    /**
     * 由启动页面启动主活动
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


        //初始化默认空数据布局
        initView(savedInstanceState);


        //初始化数据
        initData();


        //获取页面主要界面
        setTabLayout(false);

        //初始化权限申请
        initPermission();

        //监听器
        initListener();

        getOne(refreshItem);

        //获取缓存大小
        setCacheSize();

        //获取百度文字识别的认证
        initAccessTokenWithAkSk();

        initTapView();

        initDefaultFolder();

    }


    /**
     * 初始化表情包数据
     * 这个表情包是内置在apk中，用户无需下载即可直接使用
     */
    private void initData() {

    }

    /**
     * 初始化布局
     *
     * @param savedInstanceState
     */
    private void initView(Bundle savedInstanceState) {

        //初始化侧边栏
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
                            ToastUtil.showMessageShort("你戳我？很痛哎");
                        } else {//不是第一次点击的
                            thisClickTime = System.currentTimeMillis();
                            if (thisClickTime - lastClickTime < 500) {//是在0.8秒内点击的
                                lastClickTime = thisClickTime;
                                clickTimes++;
                                UIUtil.goodEgg(clickTimes, new TaskListener() {
                                    @Override
                                    public void onFinish(Object result2) {
                                        result.closeDrawer();//关闭侧边栏
                                    }
                                });
                            } else {//已经超过连续点击的时间，将变量初始化
                                lastClickTime = -1;
                                thisClickTime = -1;
                                clickTimes = 0;
                            }

                        }
                        return false;
                    }
                })
                .build();
        removeCache = new SecondaryDrawerItem().withName("清除缓存").withIcon(GoogleMaterial.Icon.gmd_delete).withSelectable(false);

        result = new DrawerBuilder()
                .withActivity(this)
                .withAccountHeader(headerResult)
                .withToolbar(toolbar)
                .withFullscreen(true)
                .withSelectedItem(-1)
                .addDrawerItems(
                        new SecondaryDrawerItem().withName("我的主页").withIcon(GoogleMaterial.Icon.gmd_home).withSelectable(false),//1
                        new SecondaryDrawerItem().withName("我的表情").withIcon(GoogleMaterial.Icon.gmd_photo_library).withSelectable(false),//3
                        new SecondaryDrawerItem().withName("扫描本机").withIcon(GoogleMaterial.Icon.gmd_scanner).withSelectable(false),//4
                        removeCache,
                        new SecondaryDrawerItem().withName("备份数据").withIcon(GoogleMaterial.Icon.gmd_file_download).withSelectable(false),//5
                        new SecondaryDrawerItem().withName("恢复数据").withIcon(GoogleMaterial.Icon.gmd_backup).withSelectable(false),//6
                        new DividerDrawerItem(),//7
                        new SecondaryDrawerItem().withName("关于应用").withIcon(R.drawable.logo).withSelectable(false),//8
                        new SecondaryDrawerItem().withName("五星好评").withIcon(GoogleMaterial.Icon.gmd_favorite).withSelectable(false),//9
                        new SecondaryDrawerItem().withName("捐赠我们").withIcon(GoogleMaterial.Icon.gmd_payment).withSelectable(false),//10
                        new SecondaryDrawerItem().withName("检查更新").withIcon(GoogleMaterial.Icon.gmd_system_update_alt).withSelectable(false).withDescription("v" + APKVersionCodeUtils.getVerName(MainActivity.this) + "(" + APKVersionCodeUtils.getVersionCode(MainActivity.this) + ")"),//11
                        new SecondaryDrawerItem().withName("退出应用").withIcon(GoogleMaterial.Icon.gmd_exit_to_app).withSelectable(false)//12
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        switch (position) {
                            case 1://我的首页，没卵用的一个按钮
                                result.closeDrawer();
                                break;
                            case 3://全盘扫描
                                //ShopActivity.actionStart(MainActivity.this);
                                ScanAllIamgeTask task = new ScanAllIamgeTask(MainActivity.this);
                                task.execute();
                                break;
                            case 2: //进入我的表情管理
                                MyActivity.actionStart(MainActivity.this);
                                break;
                            case 4://清除缓存
                                MaterialDialog dialog;
                                new MaterialDialog.Builder(MainActivity.this)
                                        .title("操作通知")
                                        .content("浏览网络信息或带来一些本地缓存，你可以选择清理他们，但再次访问需要重新下载，确定清理吗？")
                                        .positiveText("确定")
                                        .negativeText("先留着吧，手机空间有的是")
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

                            case 5://备份数据
                                new MaterialDialog.Builder(MainActivity.this)
                                        .title("为什么需要备份？")
                                        .content("本应用没有云端同步功能，所有表情文件信息存储在应用内容，一旦卸载将会丢失所有信息\n\n" +
                                                "备份数据后，点击[恢复数据]即可恢复所有表情文件（包含描述文字）\n\n" +
                                                "你也可以导出备份文件，将文件分享给别人，别人恢复你的备份也可以轻松获取你的表情包")
                                        .positiveText("开始备份")
                                        .negativeText("取消")
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                FileUtil.copyFileToTarget(MainActivity.this.getDatabasePath("expBaby.db").getAbsolutePath(), GlobalConfig.appDirPath + "database/" + DateUtil.getNowDateStr() + ".db");
                                                Toasty.info(MainActivity.this, "备份数据成功", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .show();
                                break;
                            case 6://恢复数据
                                //扫描database备份目录下面的文件
                                new RecoverDataTask(MainActivity.this).execute();
                                break;

                            case 8://关于我们
                                AboutActivity.actionStart(MainActivity.this);
                                break;
                            case 9://五星好评
                                Uri uri = Uri.parse("market://details?id=" + UIUtil.getContext().getPackageName());
                                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                                try {
                                    startActivity(goToMarket);
                                } catch (ActivityNotFoundException e) {
                                    Toasty.error(MainActivity.this, "无法启动应用市场，请重试", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case 10://捐赠
                                MiniPayUtils.setupPay(MainActivity.this, new Config.Builder("fkx15914zyuu4rnmbzr2td5", R.drawable.alipay, R.drawable.wechatpay).build());
                                break;

                            case 11://检查更新
                                checkUpdateTask = new CheckUpdateTask(MainActivity.this, getPackageManager());
                                checkUpdateTask.execute();
                                break;
                            case 12://退出应用
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


        //初始化TabLayout
        initTabLayout();


    }


    /**
     * 初始化TabLayout 数据
     */
    private void initTabLayout() {
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(ExpressionContentFragment.fragmentInstant("默认", true, 0));
        List<String> pageTitleList = new ArrayList<>();
        pageTitleList.add("默认");
        adapter = new ViewPagerAdapter(getSupportFragmentManager(), fragments, pageTitleList);
        //设置ViewPager
        viewPager.setAdapter(adapter);
        bindTabWithViewPager();
    }

    private void setTabLayout(boolean isUpdate) {
        //设置viewPager
        setViewPager(viewPager, isUpdate);
        bindTabWithViewPager();
    }

    private void bindTabWithViewPager() {
        //tabLayout绑定
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        viewPager.setOffscreenPageLimit(1);//参数为预加载数量，系统最小值为1。慎用！预加载数量过多低端机子受不了
    }


    /**
     * 设置ViewPager
     */
    private void setViewPager(final ViewPager viewPager, boolean isUpdate) {
        if (isUpdate) {
            viewPager.removeAllViewsInLayout();
        }
        ALog.d("表情包的数目" + expressionFolderList.size());

        new GetExpFolderTask(new GetMainExpListener() {
            @Override
            public void onFinish(List<Fragment> fragmentList, List<String> pageTitleList) {
                //新建适配器
                adapter = new ViewPagerAdapter(getSupportFragmentManager(), fragmentList, pageTitleList);
                //设置ViewPager
                viewPager.setAdapter(adapter);
            }
        }).execute();

    }


    /**
     * 用明文ak，sk初始化
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
//                        Toasty.info(MainActivity.this, "获取百度文字识别接口失败").show();
//                    }
//                });
//            }
//        }, getApplicationContext(), "6AsWoPOwdFEn5G17glMkGFVd", "014yBWxaRMBaQRnZD5Brg83sAzujGNOK");
    }


    private void initTapView() {
        if (MySharePreference.getUserUsedStatus("isFirstEnter") == 0) {
            toolbar.inflateMenu(R.menu.menu_main);
            new TapTargetSequence(this)
                    .targets(TapTarget.forToolbarMenuItem(toolbar, R.id.refresh, "来自 一个・one", "点击刷新，每天早6点自动更新来自one接口的数据\n\n如果喜欢可以分享给别人")
                                    .cancelable(false)
                                    .drawShadow(true)
                                    .titleTextColor(R.color.text_primary_dark)
                                    .descriptionTextColor(R.color.text_secondary_dark)
                                    .tintTarget(true)
                                    .targetCircleColor(android.R.color.black)//内圈的颜色
                                    .id(1),

                            TapTarget.forView(findViewById(R.id.fab_search), "搜索让一切变得有规律", "这里，你可以搜索你的本地所有表情包\n\n当然你必须给表情包填写相应的描述")
                                    .textTypeface(Typeface.SANS_SERIF) //指定字体
                                    .drawShadow(true).cancelable(false).tintTarget(true)//
                                    .icon(getResources().getDrawable(R.drawable.ic_search_black_24dp))//指定target图标
                                    .targetCircleColor(android.R.color.black)//内圈的颜色
                                    .titleTextColor(R.color.text_primary_dark)
                                    .descriptionTextColor(R.color.text_secondary_dark).id(2),

                            TapTarget.forView(findViewById(R.id.add_exp), "丰富的表情商店", "进入这里，你可以下载你需要的表情包\n\n你可以选择下载部分或者下载全套")
                                    .textTypeface(Typeface.SANS_SERIF) //指定字体
                                    .drawShadow(true)
                                    .cancelable(false)
                                    .tintTarget(true)//
                                    .icon(getResources().getDrawable(R.drawable.ic_add_black_24dp))//指定target图标
                                    .targetCircleColor(android.R.color.black)//内圈的颜色
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

        //监听图片的左右滑动
//        topImage.setLongClickable(true);
//        topImage.setOnTouchListener(new MyGestureListener(this));

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

//        oneText.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                ClipboardManager clipboardManager = (ClipboardManager) MainActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
//                clipboardManager.setPrimaryClip(ClipData.newPlainText(null, oneText.getText()));
//                Toasty.success(MainActivity.this, "复制成功", Toast.LENGTH_SHORT).show();
//                return false;
//            }
//        });
    }


    private void initPermission() {
        String[] notPermission = CheckPermissionUtils.checkPermission(UIUtil.getContext());
        if (notPermission.length != 0) {//需要的权限没有全部被运行
            ActivityCompat.requestPermissions(this, notPermission, 100);
        }
    }

    private void setCacheSize() {
        //获得应用内部缓存(/data/data/com.example.androidclearcache/cache)
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
     * 获取首页一个内容
     *
     * @param item
     */
    private void getOne(MenuItem item) {
        if (item != null) {
            //刷新头图信息
            showRefreshAnimation(item);
        }

        if (MyDataBase.isNeedGetOnes()) {//如果已经过时了，则需要从网络上请求数据
//            HttpUtil.getOnes(new Callback<OneDetailList>() {
//                @Override
//                public void onResponse(@NonNull Call<OneDetailList> call, @NonNull Response<OneDetailList> response) {
//
//                    //获取数据成功后删除旧的数据
//                    LitePal.deleteAll(OneDetailList.class);
//                    LitePal.deleteAll(OneDetail.class);
//
//                    //存储新的数据
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
//                    //什么也不做
//                    Toasty.error(MainActivity.this, "请求一个失败，请稍后重试", Toast.LENGTH_SHORT).show();
//                    ALog.d("请求失败" + t.getMessage());
//                }
//            });
        } else {
            setOneUI(LitePal.findFirst(OneDetailList.class, true));
        }

    }

    /**
     * 显示一个内容界面
     *
     * @param oneDetailLists
     */
    private void setOneUI(final OneDetailList oneDetailLists) {
        final List<OneDetail> oneDetailList = oneDetailLists.getOneDetailList();
        final int currentItem = oneItem % oneDetailList.size();
//        OneDetail oneDetail = oneDetailList.get(currentItem);
//        oneText.setText(oneDetail.getText());
//
//        Glide.with(this).load(oneDetail.getImgUrl())
//                .into(new SimpleTarget<Drawable>() {
//                    @Override
//                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
////                        topImage.setImageDrawable(resource);
//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                try {
////                                    sleep(1500);
//                                    sleep(0);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        hideRefreshAnimation();
//                                    }
//                                });
//                            }
//                        }).start();
//                    }
//
//                    @Override
//                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
//                        Toasty.error(MainActivity.this, "请求图片失败，请稍后重试", Toast.LENGTH_SHORT).show();
//                    }
//                });
//        oneItem++;//这样下次刷新显示下一条

//        topImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //生成截图
//                final Expression expression = new Expression(3, oneDetailLists.getDate().substring(0, 10) + (currentItem) + ".jpg", oneDetailList.get(currentItem).getImgUrl(), "头图");
//                final ExpImageDialog expImageDialog = new ExpImageDialog.Builder(MainActivity.this)
//                        .setContext(MainActivity.this, null, 3)
//                        .build();
//                expImageDialog.setImageData(expression);
//
//                //判断是否已经生成过了
//                File file = new File(GlobalConfig.appDirPath + expression.getFolderName() + "/" + expression.getName());
//                if (file.exists()) {
//                    expImageDialog.show();
//                } else {
//                    new GenerateScreenshotTask(MainActivity.this, oneText.getText().toString(), expression, new TaskListener() {
//                        @Override
//                        public void onFinish(Object result) {
//                            expImageDialog.show();
//                        }
//                    }).execute();
//                }
//
//            }
//        });
    }

    /**
     * 显示风车动画
     *
     * @param item
     */
    public void showRefreshAnimation(MenuItem item) {

        hideRefreshAnimation();
        refreshItem = item;

        //这里使用一个ImageView设置成MenuItem的ActionView，这样我们就可以使用这个ImageView显示旋转动画了
        View refreshActionView = getLayoutInflater().inflate(R.layout.item_refresh_menu, null);

        item.setActionView(refreshActionView);

        Animation rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate);
        refreshActionView.setAnimation(rotateAnimation);
        refreshActionView.startAnimation(rotateAnimation);
    }

    /**
     * 隐藏风车动画
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
            //请求安装未知应用
            new MaterialDialog.Builder(this)
                    .title("权限申请")
                    .content("即将前往设置界面，在设置界面先选择表情宝宝app，然后选中“允许安装应用”开关")
                    .positiveText("确定")
                    .negativeText("那不安装了")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                dialog.dismiss();
                                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                                startActivityForResult(intent, 125);
                            } else {
                                dialog.dismiss();
                                Toasty.info(MainActivity.this, "出现了一处逻辑错误，请反馈给作者，感谢", Toast.LENGTH_SHORT).show();

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
            //权限被申请成功
            Toasty.success(UIUtil.getContext(), "权限申请成功，愉快使用表情宝宝吧😁", Toast.LENGTH_SHORT).show();
        } else if (requestCode == 124) {
            checkUpdateTask.installApk();
        }

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // 权限被拒绝
        if (requestCode == 100) {
            Toasty.error(UIUtil.getContext(), "存储权限是本应用的基本权限，该软件运行过程中可能会闪退，请留意", Toast.LENGTH_SHORT).show();
        } else if (requestCode == 124) {
            Toasty.error(UIUtil.getContext(), "android 8.0必须获取此权限才能完成安装", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(MainActivity.this, "再按一次退出", Toast.LENGTH_SHORT).show();
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
        ALog.d("什么情况？" + file.getAbsolutePath());
        ALog.d(file.getParentFile().getAbsolutePath() + "|" + GlobalConfig.appDirPath + "database");
        boolean isExist = false;//备份文件是否已经存在在备份列表了
        if (Objects.equals(file.getParentFile().getAbsolutePath(), GlobalConfig.appDirPath + "database")) {
            if (file.exists()) {
                isExist = true;
                ALog.d("已经存在的文件了");
            }
        }
        if (!isExist) {
            FileUtil.copyFileToTarget(file.getAbsolutePath(), GlobalConfig.appDirPath + "database" + "/" + file.getName());
        }

        ALog.d("AAA" + GlobalConfig.appDirPath + "database" + "/" + file.getName());
        FileUtil.copyFileToTarget(GlobalConfig.appDirPath + "database" + "/" + file.getName(), this.getDatabasePath("expBaby.db").getAbsolutePath());
        EventBus.getDefault().post(new EventMessage(EventMessage.DATABASE));
        Toasty.success(this, "导入备份成功").show();
    }

    @Override
    public void onFileChooserDismissed(@NonNull FileChooserDialog dialog) {

    }

    /**
     * 继承GestureListener，重写left和right方法
     */
    private class MyGestureListener extends GestureListener {
        public MyGestureListener(Context context) {
            super(context);
        }

        @Override
        public boolean left() {
            Toasty.info(MainActivity.this, "点击顶部风车按钮切换图片文字哦").show();
            return super.left();
        }

        @Override
        public boolean right() {
            Toasty.info(MainActivity.this, "点击顶部风车按钮切换图片文字哦").show();
            return super.right();
        }
    }

    //建默认表情文件夹、收藏表情文件夹
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


