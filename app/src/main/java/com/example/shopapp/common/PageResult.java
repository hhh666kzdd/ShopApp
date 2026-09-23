package com.example.shopapp.common;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页返回结构（接口文档 1.5）。
 */
public class PageResult<T> {

    private List<T> records = new ArrayList<>(); // 当前页数据
    private long total;                          // 总记录数
    private int page;                            // 当前页码，从 1 开始
    private int size;                            // 每页条数
    private int pages;                           // 总页数

    public PageResult() {
    }

    public PageResult(List<T> records, long total, int page, int size) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.size = size;
        this.pages = size <= 0 ? 0 : (int) ((total + size - 1) / size);
    }

    /** 是否还有下一页，用于列表上拉加载更多 */
    public boolean hasMore() {
        return page < pages;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }
}
