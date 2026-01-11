package org.emsi.service;

import org.emsi.entities.*;

/**
 * Service d'export XML pour le standard LOM
 */
public class XmlExportService {

    public String exportToXml(LomSchema lom) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<lom xmlns=\"http://ltsc.ieee.org/xsd/LOM\">\n");

        if (lom.getGeneral() != null) {
            xml.append("  <general>\n");
            appendTag(xml, "    ", "title", lom.getGeneral().getTitle());
            appendTag(xml, "    ", "language", lom.getGeneral().getLanguage());
            appendTag(xml, "    ", "description", lom.getGeneral().getDescription());
            appendTag(xml, "    ", "keyword", lom.getGeneral().getKeyword());
            xml.append("  </general>\n");
        }

        if (lom.getLifecycle() != null) {
            xml.append("  <lifeCycle>\n");
            appendTag(xml, "    ", "version", lom.getLifecycle().getVersion());
            appendTag(xml, "    ", "status", lom.getLifecycle().getStatus());
            xml.append("  </lifeCycle>\n");
        }

        if (lom.getTechnical() != null) {
            xml.append("  <technical>\n");
            appendTag(xml, "    ", "format", lom.getTechnical().getFormat());
            appendTag(xml, "    ", "size", lom.getTechnical().getSize());
            appendTag(xml, "    ", "location", lom.getTechnical().getLocation());
            xml.append("  </technical>\n");
        }

        if (lom.getEducational() != null) {
            xml.append("  <educational>\n");
            appendTag(xml, "    ", "interactivityType", lom.getEducational().getInteractivityType());
            appendTag(xml, "    ", "learningResourceType", lom.getEducational().getLearningResourceType());
            appendTag(xml, "    ", "difficulty", String.valueOf(lom.getEducational().getDifficulty()));
            xml.append("  </educational>\n");
        }

        if (lom.getRights() != null) {
            xml.append("  <rights>\n");
            appendTag(xml, "    ", "cost", lom.getRights().getCost());
            appendTag(xml, "    ", "description", lom.getRights().getDescription());
            xml.append("  </rights>\n");
        }

        xml.append("</lom>");
        return xml.toString();
    }

    private void appendTag(StringBuilder sb, String indent, String tagName, String value) {
        if (value != null && !value.isEmpty()) {
            sb.append(indent).append("<").append(tagName).append(">");
            sb.append(escapeXml(value));
            sb.append("</").append(tagName).append(">\n");
        }
    }

    private String escapeXml(String input) {
        if (input == null)
            return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
