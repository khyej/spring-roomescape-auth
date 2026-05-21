package roomescape.theme;

public class Theme {

    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnail;
    private final Long storeId;

    public Theme(String name, String description, String thumbnail, Long storeId) {
        this(null, name, description, thumbnail, storeId);
    }

    public Theme(Long id, String name, String description, String thumbnail, Long storeId) {
        validate(name, description, thumbnail);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
        this.storeId = storeId;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void validateStore(Long storeId) {
        if (this.storeId == null || !this.storeId.equals(storeId)) {
            throw new roomescape.exception.ForbiddenException("해당 매장의 테마만 관리할 수 있습니다.");
        }
    }

    private void validate(String name, String description, String thumbnail) {
        validateName(name);
        validateDescription(description);
        validateThumbnail(thumbnail);
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("테마 이름은 비어있을 수 없습니다.");
        }
    }

    private void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("테마 설명은 비어있을 수 없습니다.");
        }
    }

    private void validateThumbnail(String thumbnail) {
        if (thumbnail == null || thumbnail.isBlank()) {
            throw new IllegalArgumentException("테마 썸네일은 비어있을 수 없습니다.");
        }
    }

}
