package worker.aims.DTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("白名单分页查询请求参数")
public class GetWhitelistRequest {

    @ApiModelProperty(value = "工厂ID", required = true, example = "factory_001")
    private String factoryId;

    @ApiModelProperty(value = "页码", required = true, example = "1")
    private Integer page = 1;

    @ApiModelProperty(value = "每页条数", required = true, example = "10")
    private Integer pageSize = 10;

    @ApiModelProperty(value = "状态 (PENDING/ACTIVE/EXPIRED)", example = "PENDING")
    private String status;

    @ApiModelProperty(value = "搜索手机号", example = "13800000000")
    private String search;
}

