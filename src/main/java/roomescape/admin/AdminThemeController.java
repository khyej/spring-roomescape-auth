package roomescape.admin;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.Auth;
import roomescape.auth.LoginUser;
import roomescape.theme.ThemeService;
import roomescape.theme.dto.PageThemesResponse;
import roomescape.theme.dto.ThemeRequest;
import roomescape.theme.dto.ThemeResponse;

@RestController
@RequestMapping("/api/admin/themes")
public class AdminThemeController {

    private final ThemeService themeService;

    public AdminThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ResponseEntity<PageThemesResponse> read(
            @Auth LoginUser loginUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(themeService.readByStore(loginUser, page, size));
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> create(@Auth LoginUser loginUser, @Valid @RequestBody ThemeRequest themeRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(themeService.create(loginUser, themeRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Auth LoginUser loginUser, @PathVariable Long id) {
        themeService.delete(loginUser, id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
