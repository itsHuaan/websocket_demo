package com.example.websocket_demo.dto.response.router;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArpTableData {
    private List<ArpEntry> entries;
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ArpEntry {
        private String device;
        private String macaddr;
        private String ipaddr;
    }
}
