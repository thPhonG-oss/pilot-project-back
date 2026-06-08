package vn.elca.training.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import vn.elca.training.model.exception.BadRequestException;

import java.util.HashSet;
import java.util.Set;

public class PaginationUtil {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final String DEFAULT_SORT_BY = "id";
    private static final Set<String> ALLOWED_SORT_FIELDS;

    static {
        ALLOWED_SORT_FIELDS = new HashSet<>();
        ALLOWED_SORT_FIELDS.add("id");
        ALLOWED_SORT_FIELDS.add("createdAt");
        ALLOWED_SORT_FIELDS.add("status");
        ALLOWED_SORT_FIELDS.add("totalAmount");
        ALLOWED_SORT_FIELDS.add("orderCode");
    }

    public static Pageable buildDefaultPagination(){
        Sort sort = Sort.by(DEFAULT_SORT_BY).ascending();
        return PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE, sort);
    }

    public static Pageable buildCustomPagination(int page, int size, String sortBy, String sortDir){

        int finalPage = page < DEFAULT_PAGE ? DEFAULT_PAGE : Math.min(page, MAX_SIZE);

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new BadRequestException("Invalid sort field: " + sortBy);
        }

        Sort sort = "asc".equalsIgnoreCase(sortDir) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        return PageRequest.of(finalPage, size, sort);
    }
}
