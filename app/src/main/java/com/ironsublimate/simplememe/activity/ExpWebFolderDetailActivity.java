package com.ironsublimate.simplememe.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.ALog;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.ironsublimate.simplememe.R;
import com.ironsublimate.simplememe.adapter.ExpressionListAdapter;
import com.ironsublimate.simplememe.bean.Expression;
import com.ironsublimate.simplememe.http.HttpUtil;
import com.ironsublimate.simplememe.task.DownloadImageTask;
import com.ironsublimate.simplememe.util.UIUtil;
import com.ironsublimate.simplememe.view.AvatarImageView;
import com.ironsublimate.simplememe.view.ExpImageDialog;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * 显示网络的一个表情包合集
 */
public class ExpWebFolderDetailActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    @BindView(R.id.owner_name)
    TextView ownerName;
    @BindView(R.id.select_add)
    RelativeLayout selectAdd;
    @BindView(R.id.download_all)
    TextView downloadAll;
    @BindView(R.id.select_all)
    TextView selectAll;
    @BindView(R.id.select_add_button)
    TextView selectAddButton;
    @BindView(R.id.owner_avatar)
    AvatarImageView ownerAvatar;
    @BindView(R.id.exit_select)
    TextView exitSelect;
    private ExpImageDialog expressionDialog;
    View notDataView;


    private String ownerNameString = "";
    private String ownerAvatarString = "";

    private int dirId = 0;
    private String dirName;
    private List<Expression> expressionList = new ArrayList<>();
    private ExpressionListAdapter adapter;
    int currentPosition = 0;
    int currentPage = 0;
    int totalCount = 0;
    Call<List<Expression>> call;

    /**
     * 是否显示checkbox
     */
    private boolean isShowCheck = false;
    /**
     * 记录选中的checkbox
     */
    private List<String> checkList = new ArrayList<>();
    private List<Expression> addExpList = new ArrayList<>();


    public static void actionStart(Activity activity, int dir, String dirName, int totalCount, String ownerName, String ownerAvatar) {
        Intent intent = new Intent(activity, ExpWebFolderDetailActivity.class);
        intent.putExtra("dir", dir);
        intent.putExtra("dirName", dirName);
        intent.putExtra("count", totalCount);
        intent.putExtra("ownerName", ownerName);
        intent.putExtra("ownerAvatar", ownerAvatar);
        activity.startActivityForResult(intent, 1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exp_folder_detail);
        ButterKnife.bind(this);

        initData();

        initView();

        initListener();

        refreshLayout.autoRefresh();
    }


    private void initData() {
        if (getIntent() != null) {
            dirId = getIntent().getIntExtra("dir", 0);
            dirName = getIntent().getStringExtra("dirName");
            totalCount = getIntent().getIntExtra("count", 0);
            ownerNameString = getIntent().getStringExtra("ownerName");
            ownerAvatarString = getIntent().getStringExtra("ownerAvatar");

        }
    }


    private void initView() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        toolbar.setTitle(dirName);
        notDataView = getLayoutInflater().inflate(R.layout.item_empty_view, (ViewGroup) recyclerView.getParent(), false);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4);
        recyclerView.setLayoutManager(gridLayoutManager);
        adapter = new ExpressionListAdapter(expressionList, false);
        recyclerView.setAdapter(adapter);

        expressionDialog = new ExpImageDialog.Builder(Objects.requireNonNull(this))
                .setContext(this, null,1)
                .build();

        ownerName.setText(ownerNameString);
        UIUtil.setImageToImageView(2, ownerAvatarString, ownerAvatar);
    }


    private void requestData(final int page) {
        currentPage = page;
        if (page > 1 && (page - 1) * 30 > totalCount) {
            refreshLayout.finishLoadMoreWithNoMoreData();//没有更多数据了,显示不能加载更多提示
            ALog.d("当前页数page", currentPage);
            ALog.d("pageSize", expressionList.size());
        } else {
            call = HttpUtil.getExpressionList(dirId, page, 30, dirName, new Callback<List<Expression>>() {
                @Override
                public void onResponse(@NonNull Call<List<Expression>> call, @NonNull final Response<List<Expression>> response) {
                    if (response.isSuccessful()) {
                        Toasty.success(UIUtil.getContext(), "请求成功", Toast.LENGTH_SHORT).show();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (page == 1) {//如果是上拉刷新，则需要新清空数据
                                    expressionList.clear();
                                }
                                expressionList.addAll(response.body());

                                ExpWebFolderDetailActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (page == 1) {//上拉刷新数据
                                            if (response.body().size() == 0 || response.body() == null) {//显示空布局
                                                adapter.setNewData(null);
                                                adapter.setEmptyView(notDataView);
                                            } else {
                                                adapter.setNewData(response.body());
                                            }
                                            refreshLayout.finishRefresh(true);
                                        } else {//下拉增加数据
                                            adapter.addData(response.body());
                                            refreshLayout.finishLoadMore(true);
                                            ALog.d("增加数据当前的页数" + currentPage);
                                        }
                                        currentPage++;
                                    }
                                });
                            }
                        }).start();
                    } else {
                        Toasty.error(ExpWebFolderDetailActivity.this, "请求失败", Toast.LENGTH_SHORT).show();
                        if (page == 1) {
                            refreshLayout.finishRefresh(false);
                        } else {
                            refreshLayout.finishLoadMore(false);
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<Expression>> call, @NonNull Throwable t) {
                    Toasty.info(ExpWebFolderDetailActivity.this, "请求失败或取消请求", Toast.LENGTH_SHORT).show();
                    refreshLayout.finishRefresh(false);
                    if (page == 1) {
                        refreshLayout.finishRefresh(false);
                    } else {
                        refreshLayout.finishLoadMore(false);
                    }
                }
            });
        }
    }

    private void initListener() {

        exitSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContraryCheck();
            }
        });

        downloadAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContraryCheck();
            }
        });

        selectAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //执行下载操作
                //Toast.makeText(ExpWebFolderDetailActivity.this, checkList.toString(), Toast.LENGTH_SHORT).show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        addExpList.clear();
                        for (int i = 0; i < checkList.size(); i++) {
                            addExpList.add(expressionList.get(Integer.parseInt(checkList.get(i))));
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new DownloadImageTask(addExpList, dirName, checkList.size(), ExpWebFolderDetailActivity.this).execute();
                                setContraryCheck();
                            }
                        });
                    }
                }).start();
                setResult(1);
            }
        });

        selectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAdapterAllSelected();
                selectAll.setText("取消全选");
                selectAll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setAdapterAllNotSelected();
                    }
                });
            }
        });


        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                requestData(1);
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                requestData(currentPage);
            }
        });

        //点击监听
        adapter.setOnItemClickListener(new ExpressionListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

                if (isShowCheck) {//如果是在多选的状态
                    CheckBox checkBox = view.findViewById(R.id.cb_item);
                    checkBox.setChecked(!checkBox.isChecked());//多选项设置为相反的状态

                    if (checkList.contains(String.valueOf(position))) {
                        checkList.remove(String.valueOf(position));
                    } else {
                        checkList.add(String.valueOf(position));
                    }
                } else {
                    currentPosition = position;
                    Expression expression = expressionList.get(position);
                    expressionDialog.setImageData(expression);
                    expressionDialog.show();
                }
            }
        });
        //长按监听
        adapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
                setContraryCheck();
                return false;
            }
        });
    }


    /**
     * 让所有的表情都在选中的状态
     */
    private void setAdapterAllSelected() {
        selectAll.setText("取消全选");
        selectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAdapterAllNotSelected();
            }
        });
    }

    /**
     * 取消所有表情的选中状态
     */
    private void setAdapterAllNotSelected() {
        selectAll.setText("全选");
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
        if (isShowCheck) {//取消批量
            selectAdd.setVisibility(View.GONE);
            ((ExpressionListAdapter) adapter).setShowCheckBox(false);
            adapter.notifyDataSetChanged();
            checkList.clear();
        } else {//显示批量
            ((ExpressionListAdapter) adapter).setShowCheckBox(true);
            adapter.notifyDataSetChanged();
            selectAdd.setVisibility(View.VISIBLE);
        }
        isShowCheck = !isShowCheck;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (call != null) {
            call.cancel();
        }
    }
}
