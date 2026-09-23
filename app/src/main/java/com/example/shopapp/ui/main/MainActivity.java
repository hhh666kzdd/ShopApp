package com.example.shopapp.ui.main;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.shopapp.R;
import com.example.shopapp.ui.base.BaseActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * 商城主界面：首页 / 分类 / 购物车 / 我的 四个 Tab，使用 show / hide 切换保留状态。
 */
public class MainActivity extends BaseActivity {

    private static final String TAG_HOME = "home";
    private static final String TAG_CATEGORY = "category";
    private static final String TAG_CART = "cart";
    private static final String TAG_MINE = "mine";

    /** 通过 Intent 指定初始 Tab，如下单后回到购物车 */
    public static final String EXTRA_TAB = "tab";

    private String currentTag;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                switchTo(TAG_HOME);
            } else if (id == R.id.nav_category) {
                switchTo(TAG_CATEGORY);
            } else if (id == R.id.nav_cart) {
                switchTo(TAG_CART);
            } else if (id == R.id.nav_mine) {
                switchTo(TAG_MINE);
            }
            return true;
        });
        int tab = getIntent().getIntExtra(EXTRA_TAB, 0);
        bottomNav.setSelectedItemId(tabMenuId(tab));
    }

    private int tabMenuId(int tab) {
        switch (tab) {
            case 1:
                return R.id.nav_category;
            case 2:
                return R.id.nav_cart;
            case 3:
                return R.id.nav_mine;
            default:
                return R.id.nav_home;
        }
    }

    /** 外部（如首页「分类」入口）切换 Tab */
    public void selectTab(int tab) {
        bottomNav.setSelectedItemId(tabMenuId(tab));
    }

    private Fragment createFragment(String tag) {
        switch (tag) {
            case TAG_CATEGORY:
                return new CategoryFragment();
            case TAG_CART:
                return new CartFragment();
            case TAG_MINE:
                return new MineFragment();
            default:
                return new HomeFragment();
        }
    }

    private void switchTo(String tag) {
        if (tag.equals(currentTag)) return;
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if (currentTag != null) {
            Fragment current = fm.findFragmentByTag(currentTag);
            if (current != null) ft.hide(current);
        }
        Fragment target = fm.findFragmentByTag(tag);
        if (target == null) {
            target = createFragment(tag);
            ft.add(R.id.fragment_container, target, tag);
        } else {
            ft.show(target);
        }
        ft.commitAllowingStateLoss();
        currentTag = tag;
    }
}
