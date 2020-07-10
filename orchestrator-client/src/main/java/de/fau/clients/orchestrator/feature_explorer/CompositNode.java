package de.fau.clients.orchestrator.feature_explorer;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import sila_java.library.core.models.SiLAElement;

@Slf4j
final class CompositNode implements SilaNode {

    private final TypeDefLut typeDefs;
    private final List<SiLAElement> elements;
    private final List<SilaNode> children = new ArrayList<>();

    private CompositNode(
            @NonNull final TypeDefLut typeDefs,
            @NonNull final List<SiLAElement> elements) {
        this.typeDefs = typeDefs;
        this.elements = elements;
    }

    protected final static CompositNode create(
            @NonNull final TypeDefLut typeDefs,
            @NonNull final List<SiLAElement> elements) {
        final CompositNode node = new CompositNode(typeDefs, elements);
        for (final SiLAElement elem : node.elements) {
            node.children.add(NodeFactory.createFromDataType(typeDefs, elem.getDataType()));
        }
        return node;
    }

    protected final static CompositNode createFromJson(
            @NonNull final TypeDefLut typeDefs,
            @NonNull final List<SiLAElement> elements,
            @NonNull JsonNode jsonNode,
            boolean isEditable) {
        final CompositNode node = new CompositNode(typeDefs, elements);
        for (final SiLAElement elem : node.elements) {
            node.children.add(NodeFactory.createFromJson(
                    typeDefs,
                    elem.getDataType(),
                    jsonNode.get(elem.getIdentifier()),
                    isEditable));
        }
        return node;
    }

    @Override
    public SilaNode cloneNode() {
        return CompositNode.create(this.typeDefs, this.elements);
    }

    @Override
    public String toJsonString() {
        String jsonElem = "";
        String child;

        for (int i = 0; i < elements.size(); i++) {
            child = children.get(i).toJsonString();
            if (child.isEmpty()) {
                continue;
            }
            jsonElem += "\"" + elements.get(i).getIdentifier() + "\":" + child + ", ";
        }

        if (jsonElem.isEmpty()) {
            return "";
        }
        return "{" + jsonElem.substring(0, jsonElem.length() - 2) + "}";
    }

    @Override
    public JComponent getComponent() {
        final int elemCount = elements.size();
        final Box structBox = Box.createVerticalBox();
        structBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        structBox.setAlignmentY(JComponent.TOP_ALIGNMENT);

        if (elemCount >= 2) {
            structBox.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEtchedBorder(),
                    BorderFactory.createEmptyBorder(4, 4, 0, 4)));
        }

        for (int i = 0; i < elemCount; i++) {
            final Box vbox = Box.createVerticalBox();
            vbox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            vbox.setAlignmentY(JComponent.TOP_ALIGNMENT);
            vbox.add(new JLabel(elements.get(i).getDisplayName()));
            SilaNode node = children.get(i);
            if (node == null) {
                continue;
            }
            final JComponent comp = node.getComponent();
            comp.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            comp.setAlignmentY(JComponent.TOP_ALIGNMENT);
            vbox.add(comp);
            structBox.add(vbox);
            structBox.add(Box.createVerticalStrut(10));
        }
        return structBox;
    }
}
