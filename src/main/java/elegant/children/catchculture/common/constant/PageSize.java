package elegant.children.catchculture.common.constant;

import org.springframework.data.domain.PageRequest;

public class PageSize {

    public static final int OTHER_PAGE_SIZE = 13;
    public static final int CULTURAL_EVENT_PAGE_SIZE = 8;

    public static PageRequest createPageRequest(int offset) {
        return PageRequest.of(offset, PageSize.CULTURAL_EVENT_PAGE_SIZE);
    }


}
