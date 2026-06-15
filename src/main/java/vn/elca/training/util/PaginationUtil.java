package vn.elca.training.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import vn.elca.training.model.exception.BadRequestException;

import java.util.HashSet;
import java.util.Set;

public class PaginationUtil {
    private static final int DEFAULT_PAGE = 1;
    private static final int MAX_PAGE = 50;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;
    private static final String DEFAULT_SORT_BY = "id";
    private static final Set<String> ALLOWED_SORT_FIELDS;

    static {
        ALLOWED_SORT_FIELDS = new HashSet<>();
        ALLOWED_SORT_FIELDS.add("id");
        ALLOWED_SORT_FIELDS.add("status");
        ALLOWED_SORT_FIELDS.add("name");
        ALLOWED_SORT_FIELDS.add("projectNumber");
    }

    public static Pageable buildDefaultPagination(){
        Sort sort = Sort.by(DEFAULT_SORT_BY).ascending();
        return PageRequest.of(DEFAULT_PAGE - 1, DEFAULT_SIZE, sort);
    }

    public static Pageable buildDefaultPageSizePagination(){
        return PageRequest.of(DEFAULT_PAGE-1, DEFAULT_SIZE);
    }

    public static Pageable buildPaginationWithCustomSorting(String sortBy, String sortDir){
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new BadRequestException("Invalid sort field: " + sortBy);
        }
        Sort sort = "asc".equalsIgnoreCase(sortDir) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        return PageRequest.of(DEFAULT_PAGE - 1, DEFAULT_SIZE, sort);
    }

    public static  Pageable buildCustomPaginationWithPageAndSize(int page, int size){
        int validPage = page < DEFAULT_PAGE ? DEFAULT_PAGE : Math.min(page - 1, MAX_PAGE);
        int validSize = size < DEFAULT_SIZE ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);

        return PageRequest.of(validPage, validSize);
    }

    public static Pageable buildFullCustomPagination(int page, int size, String sortBy, String sortDir){

        int validPage = page < DEFAULT_PAGE ? DEFAULT_PAGE : Math.min(page - 1, MAX_PAGE);
        int validSize = size < DEFAULT_SIZE ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new BadRequestException("Invalid sort field: " + sortBy);
        }

        Sort sort = "asc".equalsIgnoreCase(sortDir) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        return PageRequest.of(validPage, validSize, sort);
    }
}
