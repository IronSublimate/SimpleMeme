package com.ironsublimate.simplememe.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.ALog;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.ironsublimate.simplememe.GlobalConfig;
import com.ironsublimate.simplememe.R;
import com.ironsublimate.simplememe.adapter.ExpressionListAdapter;
import com.ironsublimate.simplememe.bean.EventMessage;
import com.ironsublimate.simplememe.bean.Expression;
import com.ironsublimate.simplememe.bean.ExpressionFolder;
import com.ironsublimate.simplememe.callback.GetExpListListener;
import com.ironsublimate.simplememe.callback.SaveImageToGalleryListener;
import com.ironsublimate.simplememe.callback.TaskListener;
import com.ironsublimate.simplememe.task.AddExpListToExpFolderTask;
import com.ironsublimate.simplememe.task.DeleteExpFolderTask;
import com.ironsublimate.simplememe.task.DeleteImageTask;
import com.ironsublimate.simplememe.task.EditExpFolderNameTask;
import com.ironsublimate.simplememe.task.GetExpListTask;
import com.ironsublimate.simplememe.task.MoveExpTask;
import com.ironsublimate.simplememe.task.SaveFolderToLocalTask;
import com.ironsublimate.simplememe.task.ShowAllExpFolderTask;
import com.ironsublimate.simplememe.view.ExpImageDialog;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;


/**
 * ??????????????????????????????????????????????????????????????????
 */
public class ExpLocalFolderDetailActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    @BindView(R.id.download_time_tip)
    TextView downloadTimeTip;
    @BindView(R.id.download_time)
    TextView downloadTime;
    @BindView(R.id.select_all)
    TextView selectAll;
    @BindView(R.id.select_delete_button)
    TextView selectDeleteButton;
    @BindView(R.id.select_delete)
    RelativeLayout selectDelete;
    @BindView(R.id.to_select)
    TextView toSelect;
    @BindView(R.id.exit_select)
    TextView exitSelect;
    @BindView(R.id.to_move)
    TextView toMove;
    @BindView(R.id.to_copy)
    TextView toCopy;


    private ExpImageDialog expressionDialog;


    private List<Expression> expressionList;
    private ExpressionListAdapter adapter;
    private int dirId;
    private String dirName;
    private int clickPosition = -1;
    /**
     * ????????????checkbox
     */
    private boolean isShowCheck = false;
    /**
     * ???????????????checkbox
     */
    private List<String> checkList = new ArrayList<>();
    List<Expression> deleteExpList = new ArrayList<>();
    private String createTime;
    GridLayoutManager gridLayoutManager;

    private View notDataView;

    public static void actionStart(Activity activity, int dirId, String dirName, String createTime) {
        Intent intent = new Intent(activity, ExpLocalFolderDetailActivity.class);
        intent.putExtra("id", dirId);
        intent.putExtra("folderName", dirName);
        intent.putExtra("time", createTime);
        activity.startActivityForResult(intent, 1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exp_local_folder_detail);
        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        initData();

        initView();

        initListener();

        refreshLayout.autoRefresh();
    }

    private void initView() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        notDataView = getLayoutInflater().inflate(R.layout.item_empty_view, (ViewGroup) recyclerView.getParent(), false);

        refreshLayout.setEnableLoadMore(false);
        toolbar.setTitle(dirName);
        gridLayoutManager = new GridLayoutManager(this, 4);
        recyclerView.setLayoutManager(gridLayoutManager);
        adapter = new ExpressionListAdapter(expressionList, true);
        recyclerView.setAdapter(adapter);

        expressionDialog = new ExpImageDialog.Builder(Objects.requireNonNull(this))
                .setContext(this, null, 2)
                .build();
        downloadTime.setText(createTime);

    }


    private void initData() {
        if (getIntent() != null) {
            dirId = getIntent().getIntExtra("id", 1);
            dirName = getIntent().getStringExtra("folderName");
            createTime = getIntent().getStringExtra("time");
        }
    }


    private void setAdapter() {

        new GetExpListTask(new GetExpListListener() {
            @Override
            public void onFinish(List<Expression> expressions) {
                expressionList = expressions;
                adapter.setNewData(expressions);
                adapter.notifyDataSetChanged();
                refreshLayout.finishRefresh(true);
                refreshLayout.setEnableRefresh(false);

                if (expressionList.size() == 0) {
                    adapter.setNewData(null);
                    adapter.setEmptyView(notDataView);
                }
            }
        }, true).execute(dirName);
    }

    private void initListener() {

        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                setAdapter();
            }
        });

        exitSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContraryCheck();
            }
        });
        toSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContraryCheck();
            }
        });

        toMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //????????????????????????
                new ShowAllExpFolderTask(new TaskListener() {
                    @Override
                    public void onFinish(Object result) {
                        //?????????????????????
                        new MoveExpTask(expressionList, checkList, (String) result, ExpLocalFolderDetailActivity.this, false, new TaskListener() {
                            @Override
                            public void onFinish(Object result) {
                                if ((Boolean) result){
                                    Toasty.success(ExpLocalFolderDetailActivity.this, "????????????", Toast.LENGTH_SHORT).show();
                                    for (int i = 0; i < checkList.size(); i++) {
                                        adapter.remove(Integer.parseInt(checkList.get(i)));
                                    }
                                    setContraryCheck();
                                }
                            }
                        }).execute();
                    }
                }, ExpLocalFolderDetailActivity.this, "", false).execute();

            }
        });

        toCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //????????????????????????
                new ShowAllExpFolderTask(new TaskListener() {
                    @Override
                    public void onFinish(Object result) {
                        //?????????????????????
                        new MoveExpTask(expressionList, checkList, (String) result, ExpLocalFolderDetailActivity.this, true,null).execute();
                    }
                }, ExpLocalFolderDetailActivity.this, "", false).execute();

            }
        });
        selectDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //??????????????????
                new DeleteImageTask(false, expressionList, checkList, dirName, ExpLocalFolderDetailActivity.this, new TaskListener() {
                    @Override
                    public void onFinish(Object result) {
                        Toasty.success(ExpLocalFolderDetailActivity.this, "????????????", Toast.LENGTH_SHORT).show();
                        for (int i = 0; i < checkList.size(); i++) {
                            adapter.remove(Integer.parseInt(checkList.get(i)));
                        }
                        setContraryCheck();
                    }
                }).execute();
            }
        });

        selectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAdapterAllSelected();
                selectAll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setAdapterAllNotSelected();
                    }
                });
            }
        });


        //????????????
        adapter.setOnItemClickListener(new ExpressionListAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                clickPosition = position;
                if (isShowCheck) {//???????????????????????????
                    CheckBox checkBox = view.findViewById(R.id.cb_item);
                    checkBox.setChecked(!checkBox.isChecked());//?????????????????????????????????

                    if (checkList.contains(String.valueOf(position))) {
                        checkList.remove(String.valueOf(position));
                    } else {
                        checkList.add(String.valueOf(position));
                    }
                } else {
                    Expression expression = expressionList.get(position);
                    expressionDialog.setImageData(expression);
                    expressionDialog.show();
                }
            }
        });
        //????????????
        adapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
                setContraryCheck();
                return false;
            }
        });
    }

    /**
     * ???????????????????????????????????????
     */
    private void setAdapterAllSelected() {
        //?????????????????????
        adapter.setAllCheckboxNotSelected();
        selectAll.setText("????????????");
        selectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAdapterAllNotSelected();
            }
        });
    }

    /**
     * ?????????????????????????????????
     */
    private void setAdapterAllNotSelected() {
        selectAll.setText("??????");
        selectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAdapterAllSelected();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (isShowCheck) {
            setContraryCheck();
        } else {
            finish();
        }
    }

    public void setContraryCheck() {
        if (isShowCheck) {//????????????
            selectDelete.setVisibility(View.GONE);
            adapter.setShowCheckBox(false);
            adapter.notifyDataSetChanged();
            checkList.clear();
        } else {//????????????
            adapter.setShowCheckBox(true);
            adapter.notifyDataSetChanged();
            selectDelete.setVisibility(View.VISIBLE);
        }
        isShowCheck = !isShowCheck;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshUI(final EventMessage eventBusMessage) {
        if (Objects.equals(eventBusMessage.getType(), EventMessage.DESCRIPTION_SAVE) && clickPosition != -1) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ALog.d("????????????" + clickPosition);
                    ALog.d(eventBusMessage.toString());
                    View view = gridLayoutManager.findViewByPosition(clickPosition).findViewById(R.id.notice);
                    view.setVisibility(View.GONE);
                    expressionList.get(clickPosition).setDesStatus(1);
                    expressionList.get(clickPosition).setDescription(eventBusMessage.getMessage());
                    EventBus.getDefault().post(new EventMessage(EventMessage.LOCAL_DESCRIPTION_SAVE, eventBusMessage.getMessage(), eventBusMessage.getMessage2(), String.valueOf(clickPosition)));

                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_local_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.re_add) {
            //??????????????????
            Matisse.from(ExpLocalFolderDetailActivity.this)
                    .choose(MimeType.ofAll(), false)
                    .countable(true)
                    .maxSelectable(90)
                    .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                    .thumbnailScale(0.85f)
                    .theme(R.style.Matisse_Dracula)
                    .imageEngine(new MyGlideEngine())
                    .forResult(1998);
        } else if (item.getItemId() == R.id.re_edit) {
            //?????????????????????
            new MaterialDialog.Builder(this)
                    .title("?????????????????????????????????")
                    .content("???????????????????????????????????????????????????")
                    .inputType(InputType.TYPE_CLASS_TEXT)
                    .input("????????????", dirName, new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(final MaterialDialog dialog, CharSequence input) {
                            List<ExpressionFolder> temExpFolderList = LitePal.where("name = ?", dialog.getInputEditText().getText().toString()).find(ExpressionFolder.class);
                            if (temExpFolderList.size() > 0) {
                                Toasty.error(ExpLocalFolderDetailActivity.this, "????????????????????????????????????", Toast.LENGTH_SHORT).show();
                            } else {
                                //??????????????????????????????
                                new EditExpFolderNameTask(ExpLocalFolderDetailActivity.this, expressionList.size(), dirName, dialog.getInputEditText().getText().toString(), new SaveImageToGalleryListener() {
                                    @Override
                                    public void onFinish(Boolean result) {
                                        //?????????????????????????????????
                                        File dir = new File(GlobalConfig.appDirPath + dirName);
                                        if (dir.exists()) {
                                            dir.renameTo(new File(GlobalConfig.appDirPath + dialog.getInputEditText().getText().toString()));
                                        }
                                        toolbar.setTitle(dialog.getInputEditText().getText().toString());
                                    }
                                }).execute(expressionList);
                            }
                        }
                    }).show();
//        } else if (item.getItemId() == R.id.all_download) {
//            //???????????????
//            new MaterialDialog.Builder(this)
//                    .title("????????????")
//                    .content("???[????????????]????????????????????????????????????????????????????????????[???????????????]??????????????????????????????????????????\n\n [???????????????]?????????????????????????????????????????????????????????")
//                    .negativeText("??????????????????")
//                    .positiveText("????????????")
//                    .onPositive(new MaterialDialog.SingleButtonCallback() {
//                        @Override
//                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                            new SaveFolderToLocalTask(ExpLocalFolderDetailActivity.this, expressionList.size(), dirName).execute(expressionList);
//                        }
//                    })
//                    .show();

        } else if (item.getItemId() == R.id.all_delete) {
            //??????????????????
            new MaterialDialog.Builder(this)
                    .title("????????????")
                    .content("????????????????????????????????????????????????????????????\n\n??????????????????????????????????????????????????????????????????????????????")
                    .negativeText("??????")
                    .positiveText("????????????")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            new DeleteExpFolderTask(dirName, ExpLocalFolderDetailActivity.this).execute();
                        }
                    })
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1998) {

            //???????????????????????????
            if (data != null) {
                List<String> addExpList = Matisse.obtainPathResult(data);
                new AddExpListToExpFolderTask(ExpLocalFolderDetailActivity.this, addExpList, dirName, new TaskListener() {
                    @Override
                    public void onFinish(Object result) {
                        refreshLayout.setEnableRefresh(true);
                        refreshLayout.autoRefresh();
                    }
                }).execute();
            }
        }
    }
}
