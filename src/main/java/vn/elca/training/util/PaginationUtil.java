package vn.elca.training.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import vn.elca.training.model.exception.BusinessException;
import vn.elca.training.model.exception.ErrorCode;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PaginationUtil {
    private static final int DEFAULT_PAGE = 1;
    private static final int MAX_PAGE = 50;
    private static final int DEFAULT_SIZE = 5;
    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 50;
    private static final Set<String> ALLOWED_SORT_FIELDS;

    static {
        ALLOWED_SORT_FIELDS = new HashSet<>();
        ALLOWED_SORT_FIELDS.add("id");
        ALLOWED_SORT_FIELDS.add("status");
        ALLOWED_SORT_FIELDS.add("name");
        ALLOWED_SORT_FIELDS.add("customer");
        ALLOWED_SORT_FIELDS.add("projectNumber");
        ALLOWED_SORT_FIELDS.add("startDate");
    }


    public static Pageable buildDefaultPageSizePagination(){
        return PageRequest.of(DEFAULT_PAGE-1, DEFAULT_SIZE);
    }


    public static Pageable buildFullCustomPagination(int page, int size, String sortBy, boolean ascending){
        if (page < DEFAULT_PAGE || page > MAX_PAGE) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        if (size < MIN_SIZE || size > MAX_SIZE) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        int validPage = page - 1;
        int validSize = size;

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new BusinessException(ErrorCode.INVALID_SORT_FIELD);
        }

        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        return PageRequest.of(validPage, validSize, sort);
    }
}
