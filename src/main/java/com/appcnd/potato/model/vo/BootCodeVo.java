package com.appcnd.potato.model.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author nihao 2021/08/25
 */
@Data
public class BootCodeVo {
    private List<Label> labels;
    private List<ApiTable> tabList;

    public void addLabel(Label label) {
        if (labels == null) {
            labels = new ArrayList<>();
        }
        labels.add(label);
    }

    @Data
    public static class ApiTable {
        private String table;
        private List<ApiUrl> urls;
    }

    @Data
    public static class ApiUrl {
        private String desc;
        private String url;
    }

    @Data
    public static class Label {
        private String content;
        private String label;
        private List<Label> children;

        public Label getChild(String label) {
            if (children != null) {
                for (Label label1 : children) {
                    if (label.equals(label1.getLabel())) {
                        return label1;
                    }
                }
            }
            return null;
        }

        public void addChild(Label label) {
            if (children == null) {
                children = new ArrayList<>();
            }
            children.add(label);
        }

        public Label(String label) {
            this.label = label;
        }

        public Label(String label, String content) {
            this.content = content;
            this.label = label;
        }

        public Label() {
        }
    }
}
