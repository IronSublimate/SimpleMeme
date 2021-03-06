package com.ironsublimate.simplememe.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.ALog;
import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback;
import com.chad.library.adapter.base.listener.OnItemDragListener;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.ironsublimate.simplememe.MySharePreference;
import com.ironsublimate.simplememe.R;
import com.ironsublimate.simplememe.adapter.ExpMyRecyclerViewAdapter;
import com.ironsublimate.simplememe.bean.EventMessage;
import com.ironsublimate.simplememe.bean.ExpressionFolder;
import com.ironsublimate.simplememe.callback.TaskListener;
import com.ironsublimate.simplememe.callback.UpdateDatabaseListener;
import com.ironsublimate.simplememe.task.AddExpListToExpFolderTask;
import com.ironsublimate.simplememe.task.ShowAllExpFolderTask;
import com.ironsublimate.simplememe.task.UpdateDatabaseTask;
import com.ironsublimate.simplememe.util.CheckPermissionUtils;
import com.ironsublimate.simplememe.util.DateUtil;
import com.ironsublimate.simplememe.util.UIUtil;
import com.ironsublimate.simplememe.view.MyGlideEngine;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import pub.devrel.easypermissions.EasyPermissions;

/**
 *
 * ????????????
 */
public class MyActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks{

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    View notDataView;

    //?????????
    private ExpMyRecyclerViewAdapter adapter;

    private List<ExpressionFolder> expressionFolderList = new ArrayList<>();

    OnItemDragListener onItemDragListener = new OnItemDragListener() {
        int start = 0;
        float end = 0;
        @Override
        public void onItemDragStart(RecyclerView.ViewHolder viewHolder, int pos){
            start = pos;
            ALog.d("??????" + pos);
        }
        @Override
        public void onItemDragMoving(RecyclerView.ViewHolder source, int from, RecyclerView.ViewHolder target, int to) {
            ALog.d("??????" + from + " || ??????" + to);

        }
        @Override
        public void onItemDragEnd(RecyclerView.ViewHolder viewHolder, int pos) {
            //????????????????????????????????????????????????????????? = ??????????????????
            ALog.d("??????" + pos);
            end = pos;
            if (start > end){//?????????
                expressionFolderList.get((int) end).setOrderValue(end + 0.5);
            }else {//?????????
                expressionFolderList.get((int) end).setOrderValue(end + 1.5);
            }
            expressionFolderList.get((int) end).save();
            EventBus.getDefault().post(new EventMessage(EventMessage.MAIN_DATABASE));


        }
    };

    public static void actionStart(Activity activity){
        Intent intent = new Intent(activity,MyActivity.class);
        activity.startActivityForResult(intent,2);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        ButterKnife.bind(this);

        EventBus.getDefault().register(this);


        initView();

        initListener();

        refreshLayout.autoRefresh();

        initTapView();


    }


    private void initView() {

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        notDataView = getLayoutInflater().inflate(R.layout.item_empty_view, (ViewGroup) recyclerView.getParent(), false);
        refreshLayout.setEnableLoadMore(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(UIUtil.getContext()));
        adapter = new ExpMyRecyclerViewAdapter(expressionFolderList,this);
        ItemDragAndSwipeCallback itemDragAndSwipeCallback = new ItemDragAndSwipeCallback(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemDragAndSwipeCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // ????????????
        adapter.enableDragItem(itemTouchHelper, R.id.item_view, true);
        adapter.setOnItemDragListener(onItemDragListener);

        recyclerView.setAdapter(adapter);
    }


    private void initTapView(){
        if (MySharePreference.getUserUsedStatus("isAddNew") == 0){
            toolbar.inflateMenu(R.menu.menu_my);
            TapTargetView.showFor(this, TapTarget.forToolbarMenuItem(toolbar,R.id.re_add,"???????????????","?????????????????????????????????????????????\n\n??????????????????????????????????????????????????????????????????")
                    .cancelable(false)
                    .drawShadow(true)
                    .tintTarget(true).targetCircleColor(android.R.color.black)//???????????????
                    .titleTextColor(R.color.text_primary_dark)
                    .descriptionTextColor(R.color.text_secondary_dark)
                    , new TapTargetView.Listener() {
                @Override
                public void onTargetClick(TapTargetView view) {
                    super.onTargetClick(view);
                }

                @Override
                public void onOuterCircleClick(TapTargetView view) {
                    super.onOuterCircleClick(view);
                    //Toast.makeText(view.getContext(), "You clicked the outer circle!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onTargetDismissed(TapTargetView view, boolean userInitiated) {
                }
            });
        }
    }




    /**
     * ??????????????????????????????????????????????????????
     */
    private void initData() {
        //???????????????????????????????????????????????????????????????status?????????-1?????????????????????
        new Thread(new Runnable() {
            @Override
            public void run() {
                expressionFolderList = LitePal.order("ordervalue").find(ExpressionFolder.class);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ALog.d("listSize",expressionFolderList.size());
                        if (expressionFolderList.size() == 0){
                            adapter.setNewData(null);
                            adapter.setEmptyView(notDataView);
                        }else {
                            adapter.setNewData(expressionFolderList);
                        }
                        refreshLayout.finishRefresh();
                        refreshLayout.setEnableRefresh(false);
                    }
                });
            }
        }).start();


    }

    /**
     * ????????????
     */
    private void initListener() {
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                initData();
            }
        });
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_my, menu);

        return true;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return super.onMenuOpened(featureId, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.re_update){
            //?????????????????????
            String[] notPermission = CheckPermissionUtils.checkPermission(UIUtil.getContext());
            if (notPermission.length != 0) {//????????????????????????????????????
                ActivityCompat.requestPermissions(this, notPermission, 100);
            }else {
                updateDatabase();
            }

        }else if (item.getItemId() == android.R.id.home) {
            finish();
        }else if (item.getItemId() == R.id.re_add){
            //?????????????????????
            new MaterialDialog.Builder(this)
                    .title("?????????????????????")
                    .content("???????????????????????????????????????????????????")
                    .inputType(InputType.TYPE_CLASS_TEXT)
                    .input("????????????", "", new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(MaterialDialog dialog, CharSequence input) {
                            // Do something
                            List<ExpressionFolder> temExpFolderList = LitePal.where("name = ?",dialog.getInputEditText().getText().toString()).find(ExpressionFolder.class);
                            if (temExpFolderList.size()>0){
                                Toasty.error(MyActivity.this,"?????????????????????????????????",Toast.LENGTH_SHORT).show();
                            }else {
                                ExpressionFolder expressionFolder = new ExpressionFolder(1,0,dialog.getInputEditText().getText().toString(),null,null, DateUtil.getNowDateStr(),null,null,-1);
                                expressionFolder.save();
                                initData();
                            }
                            UIUtil.autoBackUpWhenItIsNecessary();
                            EventBus.getDefault().post(new EventMessage(EventMessage.DATABASE));
                        }
                    }).show();
        }else if (item.getItemId() == R.id.arrange_local_exp){//??????????????????
            new MaterialDialog.Builder(this)
                    .title("????????????")
                    .content("?????????????????????????????????????????????????????????\n\n?????????????????????????????????????????????????????????????????????")
                    .positiveText("??????")
                    .negativeText("??????")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Matisse.from(MyActivity.this)
                                    .choose(MimeType.ofAll(), false)
                                    .countable(true)
                                    .maxSelectable(90)
                                    .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                                    .thumbnailScale(0.85f)
                                    .theme(R.style.Matisse_Dracula)
                                    .imageEngine(new MyGlideEngine())
                                    .forResult(1999);
                        }
                    })
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void refreshUI(EventMessage eventBusMessage){
        if (Objects.equals(eventBusMessage.getType(), EventMessage.DATABASE)){
            refreshLayout.setEnableRefresh(true);
            refreshLayout.autoRefresh();
        }
    }

    private void updateDatabase(){
        new MaterialDialog.Builder(this)
                .title("????????????")
                .content("????????????????????????????????????:\n\n" +
                        "1. ??????????????????????????????\n" +
                        "2. ??????????????????????????????????????????????????????????????????????????????")
                .positiveText("?????????")
                .negativeText("?????????????????????????????????????????????")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        UpdateDatabaseTask task = new UpdateDatabaseTask(MyActivity.this,new UpdateDatabaseListener() {

                            private MaterialDialog updateLoadingDialog;

                            @Override
                            public void onFinished() {
                                updateLoadingDialog.setContent("??????????????????");
                                /*updateLoadingDialog.dismiss();
                                Toasty.success(MyActivity.this,"????????????", Toast.LENGTH_SHORT).show();*/
                                //??????RecyclerView ??????
                                initData();

                            }

                            @Override
                            public void onProgress(int progress,int max) {
                                if (max > 0){
                                    if (!updateLoadingDialog.isShowing()){
                                        updateLoadingDialog.setMaxProgress(max);
                                        updateLoadingDialog.show();
                                    }

                                    if (progress > 0){
                                        updateLoadingDialog.setProgress(progress);
                                    }

                                }
                            }

                            @Override
                            public void onStart() {
                                updateLoadingDialog = new MaterialDialog.Builder(MyActivity.this)
                                        .title("??????????????????")
                                        .content("?????????????????????????????????????????????")
                                        .progress(false, 0, true)
                                        .build();

                            }
                        });
                        task.execute();
                    }
                })
                .show();

    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        //?????????????????????
        Toast.makeText(UIUtil.getContext(), "????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // ???????????????
        Toast.makeText(UIUtil.getContext(), "???????????????????????????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1999) {
            if (data != null) {
                //????????????????????????????????????
                new ShowAllExpFolderTask(new TaskListener() {
                    @Override
                    public void onFinish(Object result) {
                        List<String> addExpList = Matisse.obtainPathResult(data);
                        new AddExpListToExpFolderTask(MyActivity.this, addExpList, (String) result, new TaskListener() {
                            @Override
                            public void onFinish(Object result) {

                            }
                        }).execute();
                    }
                },MyActivity.this,"",false).execute();
            }
        }
    }

}
