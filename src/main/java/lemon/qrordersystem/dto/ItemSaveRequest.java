package lemon.qrordersystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.misc.NotNull;

@Getter @Setter
public class ItemSaveRequest {

    @NotBlank
    private String name;

    @NotNull
    @Min(0)
    private Integer price;

    @NotNull
    private Long categoryId;  // 폼에서 cat.id를 보내면 Long으로 바인딩됨

    @NotNull
    private Boolean isActive;

    private String description;
}
