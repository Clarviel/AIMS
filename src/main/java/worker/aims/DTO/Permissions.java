package worker.aims.DTO;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Permissions {
    private Map<String, Boolean> modules;
    private List<String> features;
    private String role;
    private String userType;   // platform 或 factory
    private Integer roleLevel; // -1 / 0 / 5 / 10 / 50
    private String department;
}

