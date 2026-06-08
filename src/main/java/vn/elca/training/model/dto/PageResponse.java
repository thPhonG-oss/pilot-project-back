package vn.elca.training.model.dto;

import java.util.List;

public class PageResponse <T>{
    private List<T> data;
    private int totalPages;
    private long totalElements;

    private boolean isLast;

    public PageResponse(List<T> data, int totalPages, long totalElements, boolean isLast) {
        this.data = data;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.isLast = isLast;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public boolean isLast() {
        return isLast;
    }

    public void setLast(boolean last) {
        isLast = last;
    }
}
