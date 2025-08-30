package worker.aims.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class FactoryIdGenerator {

    /**
     * 生成智能工厂ID
     * 格式: FCT_YYYY_XXX (例: FCT_2024_001)
     */
    public String generateFactoryId(String name, String industry, String address) {
        // 简化版本，实际项目中可能需要更复杂的逻辑
        int currentYear = LocalDateTime.now().getYear();
        String prefix = "FCT_" + currentYear + "_";
        
        // 这里应该查询数据库获取当前年份的工厂数量
        // 暂时使用随机数模拟
        int sequence = (int) (Math.random() * 999) + 1;
        return prefix + String.format("%03d", sequence);
    }

    /**
     * 生成新格式的工厂ID（智能推断版本）
     */
    public Map<String, Object> generateNewFactoryId(Map<String, Object> factoryData) {
        Map<String, Object> result = new HashMap<>();
        
        // 模拟智能推断逻辑
        String industryCode = inferIndustryCode((String) factoryData.get("industry"));
        String regionCode = inferRegionCode((String) factoryData.get("address"));
        int factoryYear = LocalDateTime.now().getYear();
        int sequenceNumber = generateSequenceNumber(industryCode, regionCode, factoryYear);
        
        String factoryId = String.format("FCT_%s_%s_%d_%03d", 
                industryCode, regionCode, factoryYear, sequenceNumber);
        
        result.put("factoryId", factoryId);
        result.put("industryCode", industryCode);
        result.put("regionCode", regionCode);
        result.put("factoryYear", factoryYear);
        result.put("sequenceNumber", sequenceNumber);
        result.put("industryName", getIndustryName(industryCode));
        result.put("regionName", getRegionName(regionCode));
        result.put("confidence", calculateConfidence(factoryData));
        result.put("needsConfirmation", false);
        result.put("reasoning", "基于工厂名称、行业和地址信息自动推断");
        
        return result;
    }

    private String inferIndustryCode(String industry) {
        if (industry == null) return "OTH";
        
        String lowerIndustry = industry.toLowerCase();
        if (lowerIndustry.contains("制造") || lowerIndustry.contains("制造")) return "MFG";
        if (lowerIndustry.contains("电子") || lowerIndustry.contains("电子")) return "ELC";
        if (lowerIndustry.contains("化工") || lowerIndustry.contains("化工")) return "CHE";
        if (lowerIndustry.contains("纺织") || lowerIndustry.contains("纺织")) return "TEX";
        if (lowerIndustry.contains("食品") || lowerIndustry.contains("食品")) return "FOD";
        if (lowerIndustry.contains("汽车") || lowerIndustry.contains("汽车")) return "AUT";
        if (lowerIndustry.contains("医药") || lowerIndustry.contains("医药")) return "MED";
        if (lowerIndustry.contains("钢铁") || lowerIndustry.contains("钢铁")) return "STL";
        if (lowerIndustry.contains("水泥") || lowerIndustry.contains("水泥")) return "CEM";
        if (lowerIndustry.contains("造纸") || lowerIndustry.contains("造纸")) return "PAP";
        
        return "OTH";
    }

    private String inferRegionCode(String address) {
        if (address == null) return "UNK";
        
        String lowerAddress = address.toLowerCase();
        if (lowerAddress.contains("北京")) return "BJ";
        if (lowerAddress.contains("上海")) return "SH";
        if (lowerAddress.contains("广州")) return "GZ";
        if (lowerAddress.contains("深圳")) return "SZ";
        if (lowerAddress.contains("杭州")) return "HZ";
        if (lowerAddress.contains("南京")) return "NJ";
        if (lowerAddress.contains("苏州")) return "SZ";
        if (lowerAddress.contains("无锡")) return "WX";
        if (lowerAddress.contains("宁波")) return "NB";
        if (lowerAddress.contains("青岛")) return "QD";
        if (lowerAddress.contains("大连")) return "DL";
        if (lowerAddress.contains("天津")) return "TJ";
        if (lowerAddress.contains("重庆")) return "CQ";
        if (lowerAddress.contains("成都")) return "CD";
        if (lowerAddress.contains("武汉")) return "WH";
        if (lowerAddress.contains("西安")) return "XA";
        if (lowerAddress.contains("长沙")) return "CS";
        if (lowerAddress.contains("郑州")) return "ZZ";
        if (lowerAddress.contains("济南")) return "JN";
        if (lowerAddress.contains("福州")) return "FZ";
        
        return "UNK";
    }

    private int generateSequenceNumber(String industryCode, String regionCode, int year) {
        // 简化版本，实际应该查询数据库获取当前组合的序号
        return (int) (Math.random() * 999) + 1;
    }

    private String getIndustryName(String industryCode) {
        Map<String, String> industryNames = new HashMap<>();
        industryNames.put("MFG", "制造业");
        industryNames.put("ELC", "电子业");
        industryNames.put("CHE", "化工业");
        industryNames.put("TEX", "纺织业");
        industryNames.put("FOD", "食品业");
        industryNames.put("AUT", "汽车业");
        industryNames.put("MED", "医药业");
        industryNames.put("STL", "钢铁业");
        industryNames.put("CEM", "水泥业");
        industryNames.put("PAP", "造纸业");
        industryNames.put("OTH", "其他行业");
        
        return industryNames.getOrDefault(industryCode, "未知行业");
    }

    private String getRegionName(String regionCode) {
        Map<String, String> regionNames = new HashMap<>();
        regionNames.put("BJ", "北京");
        regionNames.put("SH", "上海");
        regionNames.put("GZ", "广州");
        regionNames.put("SZ", "深圳");
        regionNames.put("HZ", "杭州");
        regionNames.put("NJ", "南京");
        regionNames.put("WX", "无锡");
        regionNames.put("NB", "宁波");
        regionNames.put("QD", "青岛");
        regionNames.put("DL", "大连");
        regionNames.put("TJ", "天津");
        regionNames.put("CQ", "重庆");
        regionNames.put("CD", "成都");
        regionNames.put("WH", "武汉");
        regionNames.put("XA", "西安");
        regionNames.put("CS", "长沙");
        regionNames.put("ZZ", "郑州");
        regionNames.put("JN", "济南");
        regionNames.put("FZ", "福州");
        regionNames.put("UNK", "未知地区");
        
        return regionNames.getOrDefault(regionCode, "未知地区");
    }

    private Map<String, Double> calculateConfidence(Map<String, Object> factoryData) {
        Map<String, Double> confidence = new HashMap<>();
        
        // 模拟置信度计算
        double industryConfidence = factoryData.get("industry") != null ? 0.85 : 0.5;
        double regionConfidence = factoryData.get("address") != null ? 0.90 : 0.3;
        double overallConfidence = (industryConfidence + regionConfidence) / 2;
        
        confidence.put("industry", industryConfidence);
        confidence.put("region", regionConfidence);
        confidence.put("overall", overallConfidence);
        
        return confidence;
    }
}



