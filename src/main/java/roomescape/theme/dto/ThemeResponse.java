package roomescape.theme.dto;

import roomescape.theme.Theme;

public record ThemeResponse(long id, String name, String description, String thumbnail, Long storeId) {

    public static ThemeResponse from(Theme theme) {
        return new ThemeResponse(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnail(),
                theme.getStoreId()
        );
    }
}
