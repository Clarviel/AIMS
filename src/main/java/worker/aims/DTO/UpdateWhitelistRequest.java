package worker.aims.DTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel("更新白名单请求参数")
public class UpdateWhitelistRequest {

    @ApiModelProperty(value = "白名单ID", required = true, example = "1")
    private Integer id;

    @ApiModelProperty(value = "状态 (PENDING/ACTIVE/REGISTERED/EXPIRED)", required = true, example = "ACTIVE")
    private String status;

    @ApiModelProperty(value = "过期时间", example = "2025-12-31T23:59:59")
    private LocalDateTime expiresAt;
}

