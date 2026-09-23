package com.example.shopapp.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopapp.R;
import com.example.shopapp.dao.DaoFactory;
import com.example.shopapp.entity.Category;
import com.example.shopapp.ui.base.BaseActivity;
import com.example.shopapp.util.ApiCallback;
import com.example.shopapp.util.ApiExecutor;
import com.example.shopapp.util.SessionManager;
import com.example.shopapp.util.ValidateUtil;

import java.util.List;

/**
 * 后台分类管理【管理员】（接口文档 3.2 / 3.3 / 3.4）：列表含停用分类，新增 / 编辑通过弹框完成。
 */
public class AdminCategoryActivity extends BaseActivity implements AdminCategoryAdapter.Listener {

    private AdminCategoryAdapter adapter;
    private View emptyView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SessionManager.isAdmin()) {
            toast("无权限");
            finish();
            return;
        }
        setContentView(R.layout.activity_admin_category);
        setupToolbar("分类管理", true);

        emptyView = findViewById(R.id.empty_view);
        RecyclerView rv = findViewById(R.id.recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminCategoryAdapter(this);
        rv.setAdapter(adapter);
        findViewById(R.id.btn_add).setOnClickListener(v -> showEditDialog(null));

        load();
    }

    private void load() {
        ApiExecutor.run(() -> DaoFactory.category().listAllCategories(userId()), new ApiCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> data) {
                adapter.setData(data);
                toggleEmpty(emptyView, adapter.isEmpty());
            }

            @Override
            public void onError(int code, String message) {
                toast(message);
            }
        });
    }

    /**
     * 新增 / 编辑弹框。
     *
     * @param category null 表示新增（不显示状态选项）
     */
    private void showEditDialog(Category category) {
        View view = getLayoutInflater().inflate(R.layout.dialog_admin_category, null);
        EditText etName = view.findViewById(R.id.et_name);
        EditText etIcon = view.findViewById(R.id.et_icon);
        EditText etSort = view.findViewById(R.id.et_sort);
        View tvStatusLabel = view.findViewById(R.id.tv_status_label);
        RadioGroup rgStatus = view.findViewById(R.id.rg_status);

        if (category == null) {
            tvStatusLabel.setVisibility(View.GONE);
            rgStatus.setVisibility(View.GONE);
        } else {
            etName.setText(category.getName());
            etIcon.setText(category.getIcon());
            etSort.setText(String.valueOf(category.getSort() == null ? 0 : category.getSort()));
            boolean enabled = category.getStatus() == null || category.getStatus() == 1;
            rgStatus.check(enabled ? R.id.rb_enabled : R.id.rb_disabled);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(category == null ? "新增分类" : "编辑分类")
                .setView(view)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", null)
                .create();
        dialog.show();
        // 手动接管确定按钮，校验失败时不关闭弹框
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String icon = etIcon.getText().toString().trim();
            Integer sort = ValidateUtil.parseInt(etSort.getText().toString());
            if (name.isEmpty()) {
                toast("分类名称不能为空");
                return;
            }
            if (sort == null) sort = 0;
            String iconValue = icon.isEmpty() ? null : icon;
            final Integer sortValue = sort;
            Integer status = rgStatus.getCheckedRadioButtonId() == R.id.rb_disabled ? 0 : 1;

            showLoading("保存中…");
            ApiExecutor.run(() -> category == null
                            ? DaoFactory.category().addCategory(userId(), name, iconValue, sortValue)
                            : DaoFactory.category().updateCategory(userId(), category.getId(), name, iconValue, sortValue, status),
                    new ApiCallback<Category>() {
                        @Override
                        public void onSuccess(Category data) {
                            hideLoading();
                            toast("保存成功");
                            dialog.dismiss();
                            load();
                        }

                        @Override
                        public void onError(int code, String message) {
                            hideLoading();
                            toast(message);
                        }
                    });
        });
    }

    @Override
    public void onEdit(Category category) {
        showEditDialog(category);
    }

    @Override
    public void onDelete(Category category) {
        new AlertDialog.Builder(this)
                .setMessage("确定删除分类「" + category.getName() + "」？分类下存在商品时无法删除。")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (d, w) -> {
                    showLoading();
                    ApiExecutor.run(() -> DaoFactory.category().deleteCategory(userId(), category.getId()), new ApiCallback<Void>() {
                        @Override
                        public void onSuccess(Void data) {
                            hideLoading();
                            toast("已删除");
                            load();
                        }

                        @Override
                        public void onError(int code, String message) {
                            hideLoading();
                            toast(message);
                        }
                    });
                })
                .show();
    }
}
