package worker.aims.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@ApiModel("添加白名单请求参数")
public class AddWhitelistRequest {

    @ApiModelProperty(value = "工厂ID", required = true, example = "factory_001")
    private String factoryId;

    @ApiModelProperty(value = "用户ID", required = true, example = "1001")
    private Integer uid;

    @ApiModelProperty(value = "电话号码列表", required = true, example = "[\"13800000000\",\"13900000000\"]")
    private List<String> phoneNumbers;

    @ApiModelProperty(value = "过期时间 (格式: yyyy-MM-dd HH:mm:ss)", example = "2025-08-26 12:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;
}

