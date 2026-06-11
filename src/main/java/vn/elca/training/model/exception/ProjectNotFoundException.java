package vn.elca.training.model.exception;

public class ProjectNotFoundException extends BusinessException {
    public ProjectNotFoundException(String message) {
        super(ErrorCode.PROJECT_NOT_FOUND);
    }
}
