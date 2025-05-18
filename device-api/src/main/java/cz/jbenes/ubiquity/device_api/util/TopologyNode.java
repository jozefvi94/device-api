package cz.jbenes.ubiquity.device_api.util;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TopologyNode {
    private String macAddress;
    private List<TopologyNode> children = new ArrayList<>();

    public TopologyNode(String macAddress) {
        this.macAddress = macAddress;
    }

    public void addChild(TopologyNode child) {
        this.children.add(child);
    }
}
