package vn.elca.training.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class PageResponse <T>{
    private int pageNumber;
    private int pageSize;

    private List<T> data;
    private int totalPages;
    private long totalElements;

    private boolean isLast;
}
