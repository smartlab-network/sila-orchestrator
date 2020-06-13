package de.fau.clients.orchestrator;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

@SuppressWarnings("serial")
class FeatureTreeRenderer extends DefaultTreeCellRenderer {

    private static final Icon serverIcon = new ImageIcon("src/main/resources/icons/server-online.png");
    private static final Icon silaIcon = new ImageIcon("src/main/resources/icons/sila-feature.png");
    private static final Icon commandIcon = new ImageIcon("src/main/resources/icons/command.png");
    private static final Icon propertyIcon = new ImageIcon("src/main/resources/icons/property.png");
    private static final Icon metaIcon = new ImageIcon("src/main/resources/icons/meta.png");

    public FeatureTreeRenderer() {
        this.openIcon = null;
        this.closedIcon = null;
        this.leafIcon = null;
    }

    @Override
    public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean isSelected,
            boolean isExpanded,
            boolean isLeaf,
            int rowIdx,
            boolean hasFocus) {

        super.getTreeCellRendererComponent(
                tree,
                value,
                isSelected,
                isExpanded,
                isLeaf,
                rowIdx,
                hasFocus);

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        final Object obj = node.getUserObject();
        if (obj == null || !(obj instanceof FeatureTreeType)) {
            return this;
        }

        final FeatureTreeType nodeInfo = (FeatureTreeType) (obj);
        switch (nodeInfo.nodeEnum) {
            case SERVER:
                this.setIcon(serverIcon);
                break;
            case FEATURE:
                this.setIcon(silaIcon);
                break;
            case COMMAND:
                this.setIcon(commandIcon);
                break;
            case PROPERTY:
                this.setIcon(propertyIcon);
                break;
            case META:
                this.setIcon(metaIcon);
                break;
            case DEFAULT:
            default:
            // no icon on default
        }

        final String desc = nodeInfo.getDescripton();
        if (desc != null) {
            this.setToolTipText(desc);
        }

        return this;
    }
}
