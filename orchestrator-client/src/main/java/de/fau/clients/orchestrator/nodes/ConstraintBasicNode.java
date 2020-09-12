package de.fau.clients.orchestrator.nodes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fau.clients.orchestrator.utils.DateTimeParser;
import de.fau.clients.orchestrator.utils.DocumentLengthFilter;
import de.fau.clients.orchestrator.utils.ImagePanel;
import de.fau.clients.orchestrator.utils.LocalDateSpinnerEditor;
import de.fau.clients.orchestrator.utils.LocalDateSpinnerModel;
import de.fau.clients.orchestrator.utils.LocalTimeSpinnerEditor;
import de.fau.clients.orchestrator.utils.LocalTimeSpinnerModel;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import javax.swing.AbstractSpinnerModel;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.AbstractDocument;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.SAXException;
import sila_java.library.core.models.BasicType;
import sila_java.library.core.models.Constraints;
import sila_java.library.core.models.Constraints.ContentType;
import sila_java.library.core.models.Feature;
import sila_java.library.core.models.Feature.Command;
import sila_java.library.core.models.Feature.DefinedExecutionError;
import sila_java.library.core.models.Feature.Metadata;
import sila_java.library.core.models.Feature.Property;
import sila_java.library.core.models.SiLAElement;

/**
 * A <code>BasicNode</code> with additional restrictions given by the SiLA-Constraint object.
 *
 * @see BasicNode
 * @see BasicNodeFactory
 */
@Slf4j
public class ConstraintBasicNode extends BasicNode {

    /**
     * The precision of the offset-limit of an exclusive float range (e.g. the exclusive upper-limit
     * of the value <code>1.0</code> could be <code>0.9</code>, <code>0.99</code>,
     * <code>0.999</code>, etc.)
     */
    private static final double REAL_EXCLUSIVE_OFFSET = 0.001;
    private static final double REAL_STEP_SIZE = 0.1;
    /**
     * The date-format used by the GUI-components.
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    /**
     * The time-format used by the GUI-components.
     */
    private static final String TIME_FORMAT = "HH:mm:ss";
    private static final String DATE_TIME_FORMAT = DATE_FORMAT + " " + TIME_FORMAT;
    private static final ImageIcon VALIDATON_OK = new ImageIcon(ConstraintBasicNode.class.getResource("/icons/status-ok.png"));
    private static final ImageIcon VALIDATON_WARN = new ImageIcon(ConstraintBasicNode.class.getResource("/icons/status-warning.png"));
    /**
     * The horizontal gap size between parameter component, condition description and validation
     * icon. Only used for components with constraints.
     */
    private static final int HORIZONTAL_STRUT = 5;
    private static final String LESS_THAN = "< ";
    private static final String GREATER_THAN = "> ";
    private static final String LESS_OR_EQUAL = "≤ "; // '\u2264'
    private static final String GREATER_OR_EQUAL = "≥ "; // '\u2265'
    private static final String AND_SIGN = " ∧ "; // '\u2227'
    private static final String INVALID_CONSTRAINT = "Invalid Constraint";
    private final TypeDefLut typeDefs;
    private final Constraints constraints;

    protected ConstraintBasicNode(
            @NonNull final TypeDefLut typeDefs,
            @NonNull final BasicType type,
            @NonNull final JComponent component,
            @NonNull final Supplier<String> valueSupplier,
            @NonNull final Constraints constraints) {
        super(type, component, valueSupplier);
        this.typeDefs = typeDefs;
        this.constraints = constraints;
    }

    protected static BasicNode create(
            final TypeDefLut typeDefs,
            final BasicType type,
            final Constraints constraints,
            final JsonNode jsonNode) {
        final JComponent comp;
        final Supplier<String> supp;
        switch (type) {
            case ANY:
                // TODO: implement
                comp = new JLabel("placeholder 03");
                supp = () -> ("not implemented 03");
                break;
            case BINARY:
                final ContentType contentType = constraints.getContentType();
                if (contentType != null) {
                    if (contentType.getType().equalsIgnoreCase("image")) {
                        final String subType = contentType.getSubtype();
                        if (subType.equalsIgnoreCase("jpeg")
                                || subType.equalsIgnoreCase("png")
                                || subType.equalsIgnoreCase("bmp")
                                || subType.equalsIgnoreCase("tiff")
                                || subType.equalsIgnoreCase("gif")) {
                            BufferedImage img = null;
                            try {
                                img = ImageIO.read(new ByteArrayInputStream(jsonNode.binaryValue()));
                            } catch (IOException ex) {
                                System.err.println(ex.getMessage());
                            }

                            if (img != null) {
                                comp = new ImagePanel(img);
                                supp = () -> ("error in constrained image/* binary type");
                                break;
                            }
                        }
                    }
                }

                // TODO: implement more constrained types
                comp = new JLabel("Unknown constrained binary type");
                supp = () -> ("not implemented 04");
                break;
            case BOOLEAN:
                return BasicNodeFactory.createBooleanTypeFromJson(jsonNode, true);
            case DATE:
                if (constraints.getSet() != null) {
                    final List<String> dateSet = constraints.getSet().getValue();
                    final LocalDate[] dates = new LocalDate[dateSet.size()];
                    for (int i = 0; i < dateSet.size(); i++) {
                        dates[i] = DateTimeParser.parseIsoDate(dateSet.get(i));
                    }
                    final JComboBox<LocalDate> dateComboBox = new JComboBox<>(dates);
                    dateComboBox.setMaximumSize(BasicNodeFactory.MAX_SIZE_DATE_TIME_SPINNER);
                    supp = () -> {
                        return dateComboBox.getSelectedItem().toString();
                    };
                    comp = dateComboBox;
                } else {
                    String minBounds = null;
                    if (constraints.getMinimalExclusive() != null) {
                        minBounds = GREATER_THAN + DateTimeParser.parseIsoDate(
                                constraints.getMinimalExclusive()).toString();
                    } else if (constraints.getMinimalInclusive() != null) {
                        minBounds = GREATER_OR_EQUAL + DateTimeParser.parseIsoDate(
                                constraints.getMinimalInclusive()).toString();
                    }

                    String maxBounds = null;
                    if (constraints.getMaximalExclusive() != null) {
                        maxBounds = LESS_THAN + DateTimeParser.parseIsoDate(
                                constraints.getMaximalExclusive()).toString();
                    } else if (constraints.getMaximalInclusive() != null) {
                        maxBounds = LESS_OR_EQUAL + DateTimeParser.parseIsoDate(
                                constraints.getMaximalInclusive()).toString();
                    }

                    final String conditionDesc;
                    if (minBounds != null && maxBounds != null) {
                        conditionDesc = minBounds + AND_SIGN + maxBounds;
                    } else if (minBounds != null) {
                        conditionDesc = minBounds;
                    } else if (maxBounds != null) {
                        conditionDesc = maxBounds;
                    } else {
                        conditionDesc = INVALID_CONSTRAINT;
                    }

                    LocalDate initDate = LocalDate.now();
                    if (jsonNode != null) {
                        try {
                            initDate = DateTimeParser.parseIsoDate(jsonNode.asText());
                        } catch (Exception ex) {
                            // do nothing
                        }
                    }

                    final JSpinner dateSpinner = new JSpinner(createRangeConstrainedDateModel(initDate, constraints));
                    dateSpinner.setMaximumSize(BasicNodeFactory.MAX_SIZE_DATE_TIME_SPINNER);
                    dateSpinner.setEditor(new LocalDateSpinnerEditor(dateSpinner));
                    supp = () -> {
                        return dateSpinner.getValue().toString();
                    };
                    final Box hbox = Box.createHorizontalBox();
                    hbox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
                    hbox.add(dateSpinner);
                    hbox.add(Box.createHorizontalStrut(HORIZONTAL_STRUT));
                    hbox.add(new JLabel(conditionDesc));
                    comp = hbox;
                }
                break;
            case INTEGER:
            case REAL:
                if (constraints.getSet() != null) {
                    final List<String> numberSet = constraints.getSet().getValue();
                    final JComboBox<String> numberComboBox = new JComboBox<>(numberSet.toArray(new String[0]));
                    numberComboBox.setMaximumSize(BasicNodeFactory.MAX_SIZE_NUMERIC_SPINNER);

                    if (jsonNode != null) {
                        numberComboBox.setSelectedItem(jsonNode.asText());
                    }

                    if (type == BasicType.INTEGER) {
                        supp = () -> (Integer.valueOf(numberComboBox.getSelectedItem().toString()).toString());
                    } else { // REAL
                        supp = () -> (Double.valueOf(numberComboBox.getSelectedItem().toString()).toString());
                    }
                    comp = numberComboBox;
                } else {
                    final SpinnerModel model = (type == BasicType.INTEGER)
                            ? createRangeConstrainedIntModel(constraints)
                            : createRangeConstrainedRealModel(constraints);
                    final JSpinner numericSpinner = new JSpinner(model);
                    numericSpinner.setMaximumSize(BasicNodeFactory.MAX_SIZE_NUMERIC_SPINNER);
                    if (jsonNode != null) {
                        if (type == BasicType.INTEGER) {
                            numericSpinner.setValue(jsonNode.asInt());
                        } else { // REAL
                            numericSpinner.setValue(jsonNode.asDouble());
                        }
                    }

                    final String conditionDesc;
                    if (constraints.getUnit() != null) {
                        conditionDesc = constraints.getUnit().getLabel();
                    } else {
                        String minBounds = null;
                        if (constraints.getMinimalExclusive() != null) {
                            minBounds = GREATER_THAN + constraints.getMinimalExclusive();
                        } else if (constraints.getMinimalInclusive() != null) {
                            minBounds = GREATER_OR_EQUAL + constraints.getMinimalInclusive();
                        }

                        String maxBounds = null;
                        if (constraints.getMaximalExclusive() != null) {
                            maxBounds = LESS_THAN + constraints.getMaximalExclusive();
                        } else if (constraints.getMaximalInclusive() != null) {
                            maxBounds = LESS_OR_EQUAL + constraints.getMaximalInclusive();
                        }

                        if (minBounds != null && maxBounds != null) {
                            conditionDesc = minBounds + AND_SIGN + maxBounds;
                        } else if (minBounds != null) {
                            conditionDesc = minBounds;
                        } else if (maxBounds != null) {
                            conditionDesc = maxBounds;
                        } else {
                            conditionDesc = INVALID_CONSTRAINT;;
                        }
                    }

                    final Box hbox = Box.createHorizontalBox();
                    hbox.add(numericSpinner);
                    hbox.add(Box.createHorizontalStrut(HORIZONTAL_STRUT));
                    hbox.add(new JLabel(conditionDesc));
                    hbox.setMaximumSize(BasicNodeFactory.MAX_SIZE_TEXT_FIELD);
                    comp = hbox;
                    supp = () -> (numericSpinner.getValue().toString());
                }
                break;
            case STRING:
                final Constraints.Set conSet = constraints.getSet();
                if (conSet != null) {
                    final JComboBox<String> comboBox = new JComboBox<>();
                    comboBox.setMaximumSize(BasicNodeFactory.MAX_SIZE_TEXT_FIELD);
                    for (final String item : conSet.getValue()) {
                        comboBox.addItem(item);
                    }
                    comp = comboBox;
                    supp = () -> ((String) comboBox.getSelectedItem());
                } else {
                    final JFormattedTextField strField = new JFormattedTextField();
                    strField.setMaximumSize(BasicNodeFactory.MAX_SIZE_TEXT_FIELD);
                    final Supplier<Boolean> validator;
                    final String conditionDesc;

                    if (constraints.getPattern() != null) {
                        final String pattern = constraints.getPattern();
                        validator = () -> (strField.getText().matches(pattern));
                        conditionDesc = "match " + pattern;
                    } else if (constraints.getLength() != null) {
                        final int len = constraints.getLength().intValue();
                        ((AbstractDocument) strField.getDocument()).setDocumentFilter(
                                new DocumentLengthFilter(len));
                        validator = () -> (strField.getText().length() == len);
                        conditionDesc = "= " + len;
                    } else if (constraints.getSchema() != null) {
                        final Constraints.Schema schema = constraints.getSchema();
                        final String schemaType = schema.getType();
                        if (schemaType.equalsIgnoreCase("Xml")) {
                            if (schema.getUrl() != null) {
                                validator = () -> (isXmlWellFormed(new ByteArrayInputStream(
                                        strField.getText().getBytes(StandardCharsets.UTF_8))) // 
                                        // FIXME: A proper validation against the schema is not done
                                        //        yet due to the poor support and the optional 
                                        //        character of this feature. To enable validation,
                                        //        simply uncomment the code blocks below. 
                                        //        (2020-09-06, florian.bauer.dev@gmail.com)
                                        /* && isXmlValid(new ByteArrayInputStream(
                                                   strField.getText().getBytes(StandardCharsets.UTF_8)),
                                                   new StreamSource(schema.getUrl())) */);
                            } else if (schema.getInline() != null) {
                                validator = () -> (isXmlWellFormed(new ByteArrayInputStream(
                                        strField.getText().getBytes(StandardCharsets.UTF_8))) /*
                                        && isXmlValid(new ByteArrayInputStream(
                                                strField.getText().getBytes(StandardCharsets.UTF_8)),
                                                new StreamSource(new ByteArrayInputStream(schema
                                                        .getInline()
                                                        .getBytes(StandardCharsets.UTF_8))))*/);
                            } else {
                                validator = () -> (false);
                            }
                            conditionDesc = "Xml";
                        } else if (schemaType.equalsIgnoreCase("Json")) {
                            // TODO: Implement proper JSON schema handling by URL and Inline.
                            validator = () -> (isJsonValid(strField.getText()));
                            conditionDesc = "Json";
                        } else {
                            validator = () -> (false);
                            conditionDesc = INVALID_CONSTRAINT;
                        }
                    } else if (constraints.getFullyQualifiedIdentifier() != null) {
                        final String fqiType = constraints.getFullyQualifiedIdentifier();
                        validator = () -> (vlidateFullyQualifiedIdentifier(fqiType,
                                strField.getText(),
                                typeDefs));
                        conditionDesc = fqiType;
                    } else {
                        final BigInteger min = constraints.getMinimalLength();
                        final BigInteger max = constraints.getMaximalLength();
                        if (max != null) {
                            ((AbstractDocument) strField.getDocument()).setDocumentFilter(
                                    new DocumentLengthFilter(max.intValue()));
                        }

                        if (min != null && max != null) {
                            validator = () -> {
                                final int len = strField.getText().length();
                                return (len >= min.intValue() && len <= max.intValue());
                            };
                            conditionDesc = GREATER_OR_EQUAL + min + AND_SIGN + GREATER_OR_EQUAL + max;
                        } else if (min != null) {
                            validator = () -> (strField.getText().length() >= min.intValue());
                            conditionDesc = GREATER_OR_EQUAL + min;
                        } else if (max != null) {
                            validator = () -> (strField.getText().length() <= max.intValue());
                            conditionDesc = LESS_OR_EQUAL + max;
                        } else {
                            validator = () -> (false);
                            conditionDesc = INVALID_CONSTRAINT;
                        }
                    }

                    final JLabel validationLabel = new JLabel(VALIDATON_OK);
                    validationLabel.setDisabledIcon(VALIDATON_WARN);
                    validationLabel.setEnabled(false);

                    // validate on enter
                    strField.addActionListener((evt) -> {
                        validationLabel.setEnabled(validator.get());
                    });

                    // validate after focus was lost
                    strField.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent evt) {
                            validationLabel.setEnabled(validator.get());
                        }
                    });

                    final Box hbox = Box.createHorizontalBox();
                    hbox.add(strField);
                    hbox.add(Box.createHorizontalStrut(HORIZONTAL_STRUT));
                    hbox.add(new JLabel(conditionDesc));
                    hbox.add(Box.createHorizontalStrut(HORIZONTAL_STRUT));
                    hbox.add(validationLabel);
                    hbox.setMaximumSize(BasicNodeFactory.MAX_SIZE_TEXT_FIELD);

                    if (jsonNode != null) {
                        strField.setText(jsonNode.asText());
                        // validate after import
                        validationLabel.setEnabled(validator.get());
                    }
                    comp = hbox;
                    supp = () -> (strField.getText());
                }
                break;
            case TIME:
                if (constraints.getSet() != null) {
                    final List<String> timeSet = constraints.getSet().getValue();
                    final OffsetTime[] times = new OffsetTime[timeSet.size()];
                    for (int i = 0; i < timeSet.size(); i++) {
                        times[i] = DateTimeParser.parseIsoTime(timeSet.get(i));
                    }
                    final JComboBox<OffsetTime> timeComboBox = new JComboBox<>(times);
                    timeComboBox.setMaximumSize(BasicNodeFactory.MAX_SIZE_DATE_TIME_SPINNER);
                    supp = () -> {
                        return ((OffsetTime) timeComboBox.getSelectedItem())
                                .withOffsetSameInstant(ZoneOffset.UTC)
                                .toString();
                    };
                    comp = timeComboBox;
                } else {
                    String minBounds = null;
                    if (constraints.getMinimalExclusive() != null) {
                        minBounds = GREATER_THAN + DateTimeParser.parseIsoTime(
                                constraints.getMinimalExclusive()).toLocalTime().toString();
                    } else if (constraints.getMinimalInclusive() != null) {
                        minBounds = GREATER_OR_EQUAL + DateTimeParser.parseIsoTime(
                                constraints.getMinimalInclusive()).toLocalTime().toString();
                    }

                    String maxBounds = null;
                    if (constraints.getMaximalExclusive() != null) {
                        maxBounds = LESS_THAN + DateTimeParser.parseIsoTime(
                                constraints.getMaximalExclusive()).toLocalTime().toString();
                    } else if (constraints.getMaximalInclusive() != null) {
                        maxBounds = LESS_OR_EQUAL + DateTimeParser.parseIsoTime(
                                constraints.getMaximalInclusive()).toLocalTime().toString();
                    }

                    final String conditionDescr;
                    if (minBounds != null && maxBounds != null) {
                        conditionDescr = minBounds + AND_SIGN + maxBounds;
                    } else if (minBounds != null) {
                        conditionDescr = minBounds;
                    } else if (maxBounds != null) {
                        conditionDescr = maxBounds;
                    } else {
                        conditionDescr = INVALID_CONSTRAINT;
                    }

                    final JSpinner timeSpinner = new JSpinner();
                    LocalTime initTime = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
                    if (jsonNode != null) {
                        try {
                            initTime = LocalTime.parse(jsonNode.asText());
                        } catch (Exception ex) {
                            // do nothing and use the current time instead
                        }
                    }
                    timeSpinner.setModel(createRangeConstrainedTimeModel(initTime, constraints));
                    timeSpinner.setMaximumSize(BasicNodeFactory.MAX_SIZE_DATE_TIME_SPINNER);
                    timeSpinner.setEditor(new LocalTimeSpinnerEditor(timeSpinner));

                    supp = () -> {
                        return timeSpinner.getValue().toString();
                    };

                    final Box hbox = Box.createHorizontalBox();
                    hbox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
                    hbox.add(timeSpinner);
                    hbox.add(Box.createHorizontalStrut(HORIZONTAL_STRUT));
                    hbox.add(new JLabel(conditionDescr));
                    comp = hbox;
                }
                break;
            case TIMESTAMP:
                if (constraints.getSet() != null) {
                    final List<String> timeSet = constraints.getSet().getValue();
                    final OffsetDateTime[] times = new OffsetDateTime[timeSet.size()];
                    for (int i = 0; i < timeSet.size(); i++) {
                        times[i] = DateTimeParser.parseIsoDateTime(timeSet.get(i));
                    }
                    final JComboBox<OffsetDateTime> timestampComboBox = new JComboBox<>(times);
                    timestampComboBox.setMaximumSize(BasicNodeFactory.MAX_SIZE_TIMESTAMP_SPINNER);
                    supp = () -> {
                        return timestampComboBox.getSelectedItem().toString();
                    };
                    comp = timestampComboBox;
                } else {
                    final JSpinner timestampSpinner = new JSpinner(
                            // TODO: implement createRangeConstrainedTimestampModel(constraints)
                            new SpinnerDateModel()
                    );
                    timestampSpinner.setMaximumSize(BasicNodeFactory.MAX_SIZE_TIMESTAMP_SPINNER);
                    timestampSpinner.setEditor(new JSpinner.DateEditor(timestampSpinner, DATE_TIME_FORMAT));
                    supp = () -> {
                        Date time = (Date) timestampSpinner.getValue();
                        return OffsetDateTime.ofInstant(time.toInstant(), ZoneOffset.UTC).toString();
                    };
                    comp = timestampSpinner;
                }
                break;
            default:
                throw new IllegalArgumentException("Not a valid BasicType.");
        }
        return new ConstraintBasicNode(typeDefs, type, comp, supp, constraints);
    }

    @Override
    public BasicNode cloneNode() {
        return create(this.typeDefs, this.type, this.constraints, null);
    }

    @NonNull
    @Override
    public String toString() {
        return "(" + this.type + ", "
                + this.component.getClass() + ", "
                + this.valueSupplier.getClass() + ", "
                + this.constraints + ")";
    }

    protected Constraints getConstaint() {
        return this.constraints;
    }

    /**
     * Validates a <code>FullyQualifiedIdentifier</code>. Example:
     * <code>org.silastandard/core/SiLAService/v1</code>.
     *
     * @param fqiType The <code>FullyQualifiedIdentifier</code>-type to validate (e.g.
     * <code>FeatureIdentifier</code>).
     * @param fqiUri The FQI string to validate (e.g.
     * <code>org.silastandard/core/SiLAService/v1</code>).
     * @param typeDefs The type definitions.
     * @return <code>true</code> if valid, otherwise <code>false</code>.
     */
    private static boolean vlidateFullyQualifiedIdentifier(
            final String fqiType,
            final String fqiUri,
            final TypeDefLut typeDefs) {
        final List<Feature> featList = typeDefs.getServer().getFeatures();
        final String[] sections = fqiUri.split("/");
        if (fqiType.equalsIgnoreCase(FullyQualifiedIdentifier.FEATURE_IDENTIFIER.toString())) {
            if (sections.length < FullyQualifiedIdentifier.FEATURE_IDENTIFIER.getSectionCount()) {
                return false;
            }
            for (final Feature feat : featList) {
                if (feat.getIdentifier().equalsIgnoreCase(sections[2])) {
                    return true;
                }
            }
        } else if (fqiType.equalsIgnoreCase(FullyQualifiedIdentifier.COMMAND_IDENTIFIER.toString())) {
            if (sections.length < FullyQualifiedIdentifier.COMMAND_IDENTIFIER.getSectionCount()) {
                return false;
            }
            for (final Feature feat : featList) {
                if (feat.getIdentifier().equalsIgnoreCase(sections[2])) {
                    for (final Command cmd : feat.getCommand()) {
                        if (cmd.getIdentifier().equalsIgnoreCase(sections[5])) {
                            return true;
                        }
                    }
                }
            }
        } else if (fqiType.equalsIgnoreCase(FullyQualifiedIdentifier.COMMAND_PARAMETER_IDENTIFIER.toString())) {
            if (sections.length < FullyQualifiedIdentifier.COMMAND_PARAMETER_IDENTIFIER.getSectionCount()) {
                return false;
            }
            for (final Feature feat : featList) {
                if (feat.getIdentifier().equalsIgnoreCase(sections[2])) {
                    for (final Command cmd : feat.getCommand()) {
                        if (cmd.getIdentifier().equalsIgnoreCase(sections[5])) {
                            for (final SiLAElement param : cmd.getParameter()) {
                                if (param.getIdentifier().equalsIgnoreCase(sections[7])) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        } else if (fqiType.equalsIgnoreCase(FullyQualifiedIdentifier.COMMAND_RESPONSE_IDENTIFIER.toString())) {
            if (sections.length < FullyQualifiedIdentifier.COMMAND_RESPONSE_IDENTIFIER.getSectionCount()) {
                return false;
            }
            for (final Feature feat : featList) {
                if (feat.getIdentifier().equalsIgnoreCase(sections[2])) {
                    for (final Command cmd : feat.getCommand()) {
                        if (cmd.getIdentifier().equalsIgnoreCase(sections[5])) {
                            for (final SiLAElement resp : cmd.getResponse()) {
                                if (resp.getIdentifier().equalsIgnoreCase(sections[7])) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        } else if (fqiType.equalsIgnoreCase(FullyQualifiedIdentifier.INTERMEDIATE_COMMAND_RESPONSEIDENTIFIER.toString())) {
            if (sections.length < FullyQualifiedIdentifier.INTERMEDIATE_COMMAND_RESPONSEIDENTIFIER.getSectionCount()) {
                return false;
            }
            for (final Feature feat : featList) {
                if (feat.getIdentifier().equalsIgnoreCase(sections[2])) {
                    for (final Command cmd : feat.getCommand()) {
                        if (cmd.getIdentifier().equalsIgnoreCase(sections[5])) {
                            for (final SiLAElement interResp : cmd.getIntermediateResponse()) {
                                if (interResp.getIdentifier().equalsIgnoreCase(sections[7])) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        } else if (fqiType.equalsIgnoreCase(FullyQualifiedIdentifier.DEFINED_EXECUTION_ERROR_IDENTIFIER.toString())) {
            if (sections.length < FullyQualifiedIdentifier.DEFINED_EXECUTION_ERROR_IDENTIFIER.getSectionCount()) {
                return false;
            }
            for (final Feature feat : featList) {
                if (feat.getIdentifier().equalsIgnoreCase(sections[2])) {
                    for (final DefinedExecutionError err : feat.getDefinedExecutionError()) {
                        if (err.getIdentifier().equalsIgnoreCase(sections[5])) {
                            return true;
                        }
                    }
                }
            }
        } else if (fqiType.equalsIgnoreCase(FullyQualifiedIdentifier.PROPERTY_IDENTIFIER.toString())) {
            if (sections.length < FullyQualifiedIdentifier.PROPERTY_IDENTIFIER.getSectionCount()) {
                return false;
            }
            for (final Feature feat : featList) {
                if (feat.getIdentifier().equalsIgnoreCase(sections[2])) {
                    for (final Property prop : feat.getProperty()) {
                        if (prop.getIdentifier().equalsIgnoreCase(sections[5])) {
                            return true;
                        }
                    }
                }
            }
        } else if (fqiType.equalsIgnoreCase(FullyQualifiedIdentifier.TYPE_IDENTIFIER.toString())) {
            if (sections.length < FullyQualifiedIdentifier.TYPE_IDENTIFIER.getSectionCount()) {
                return false;
            }
            for (final Feature feat : featList) {
                if (feat.getIdentifier().equalsIgnoreCase(sections[2])) {
                    for (final SiLAElement dataTypeDef : feat.getDataTypeDefinition()) {
                        if (dataTypeDef.getIdentifier().equalsIgnoreCase(sections[5])) {
                            return true;
                        }
                    }
                }
            }
        } else if (fqiType.equalsIgnoreCase(FullyQualifiedIdentifier.METADATA_IDENTIFIER.toString())) {
            if (sections.length < FullyQualifiedIdentifier.METADATA_IDENTIFIER.getSectionCount()) {
                return false;
            }
            for (final Feature feat : featList) {
                if (feat.getIdentifier().equalsIgnoreCase(sections[2])) {
                    for (final Metadata meta : feat.getMetadata()) {
                        if (meta.getIdentifier().equalsIgnoreCase(sections[5])) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Creates a Integer based, range-limited model for constraining input in
     * <code>JSpinner</code>-components. This functions does not consider any
     * <code>Set</code>-constraints. Only the following functions describing the range-limits are
     * considered:
     * <ul>
     * <li><code>getMinimalExclusive()</code></li>
     * <li><code>getMinimalInclusive()</code></li>
     * <li><code>getMaximalExclusive()</code></li>
     * <li><code>getMaximalInclusive()</code></li>
     * </ul>
     *
     * @param constraints The SiLA-Constraints element defining the value limits.
     * @return The spinner-model for a <code>JSpinner</code>-component.
     */
    private static SpinnerModel createRangeConstrainedIntModel(final Constraints constraints) {
        int initVal = 0;
        Integer min = null;
        Integer max = null;
        String conStr = constraints.getMinimalExclusive();
        if (conStr != null) {
            min = Integer.parseInt(conStr) + 1;
            initVal = Math.max(min, initVal);
        }
        conStr = constraints.getMinimalInclusive();
        if (conStr != null) {
            min = Integer.parseInt(conStr);
            initVal = Math.max(min, initVal);
        }

        conStr = constraints.getMaximalExclusive();
        if (conStr != null) {
            max = Integer.parseInt(conStr) - 1;
        }
        conStr = constraints.getMaximalInclusive();
        if (conStr != null) {
            max = Integer.parseInt(conStr);
        }
        initVal = (max != null && max < initVal) ? max : initVal;
        return new SpinnerNumberModel((Number) initVal, min, max, 1);
    }

    /**
     * Creates a Double based, range-limited model to constrain input in
     * <code>JSpinner</code>-components. This functions does not consider any
     * <code>Set</code>-constraints. Only the following functions describing the range-limits are
     * considered:
     * <ul>
     * <li><code>getMinimalExclusive()</code></li>
     * <li><code>getMinimalInclusive()</code></li>
     * <li><code>getMaximalExclusive()</code></li>
     * <li><code>getMaximalInclusive()</code></li>
     * </ul>
     *
     * @param constraints The SiLA-Constraints element defining the value limits.
     * @return The spinner-model for a <code>JSpinner</code>-component.
     */
    private static SpinnerModel createRangeConstrainedRealModel(final Constraints constraints) {
        double initVal = 0.0;
        Double min = null;
        Double max = null;
        if (constraints.getMinimalExclusive() != null) {
            min = Double.parseDouble(constraints.getMinimalExclusive()) + REAL_EXCLUSIVE_OFFSET;
            initVal = Math.max(min, initVal);
        } else if (constraints.getMinimalInclusive() != null) {
            min = Double.parseDouble(constraints.getMinimalInclusive());
            initVal = Math.max(min, initVal);
        }

        if (constraints.getMaximalExclusive() != null) {
            max = Double.parseDouble(constraints.getMaximalExclusive()) - REAL_EXCLUSIVE_OFFSET;
        } else if (constraints.getMaximalInclusive() != null) {
            max = Double.parseDouble(constraints.getMaximalInclusive());
        }
        initVal = (max != null && max < initVal) ? max : initVal;
        return new SpinnerNumberModel((Number) initVal, min, max, REAL_STEP_SIZE);
    }

    /**
     * Creates a date based, range-limited model to constrain input in
     * <code>JSpinner</code>-components. This functions does not consider any
     * <code>Set</code>-constraints.
     *
     * @param initDate The initial date value the model is set to.
     * @param constraints The SiLA-Constraints element defining the date limits.
     * @return The spinner-model for a <code>JSpinner</code>-component.
     */
    public static AbstractSpinnerModel createRangeConstrainedDateModel(
            LocalDate initDate,
            final Constraints constraints) {

        LocalDate start = null;
        if (constraints.getMinimalExclusive() != null) {
            start = DateTimeParser.parseIsoDate(constraints.getMinimalExclusive()).plusDays(1);
        } else if (constraints.getMinimalInclusive() != null) {
            start = DateTimeParser.parseIsoDate(constraints.getMinimalInclusive());
        }

        if (start != null && start.isAfter(initDate)) {
            initDate = start;
        }

        LocalDate end = null;
        if (constraints.getMaximalExclusive() != null) {
            end = DateTimeParser.parseIsoDate(constraints.getMaximalExclusive()).minusDays(1);
        } else if (constraints.getMaximalInclusive() != null) {
            end = DateTimeParser.parseIsoDate(constraints.getMaximalInclusive());
        }

        if (end != null && end.isBefore(initDate)) {
            initDate = end;
        }
        return new LocalDateSpinnerModel(initDate, start, end, ChronoUnit.DAYS);
    }

    /**
     * Creates a local time based, range-limited model to constrain input in
     * <code>JSpinner</code>-components. This functions does not consider any
     * <code>Set</code>-constraints.
     *
     * @param initTime The initial time value the model is set to.
     * @param constraints The SiLA-Constraints element defining the time limits.
     * @return The spinner-model for a <code>JSpinner</code>-component.
     */
    public static AbstractSpinnerModel createRangeConstrainedTimeModel(
            LocalTime initTime,
            final Constraints constraints) {
        final LocalTime start;
        if (constraints.getMinimalExclusive() != null) {
            start = DateTimeParser.parseIsoTime(constraints.getMinimalExclusive())
                    .toLocalTime()
                    .plusSeconds(1);
        } else if (constraints.getMinimalInclusive() != null) {
            start = DateTimeParser.parseIsoTime(constraints.getMinimalInclusive())
                    .toLocalTime();
        } else {
            start = LocalTime.MIN;
        }

        final LocalTime end;
        if (constraints.getMaximalExclusive() != null) {
            end = DateTimeParser.parseIsoTime(constraints.getMaximalExclusive())
                    .toLocalTime()
                    .minusSeconds(1);
        } else if (constraints.getMaximalInclusive() != null) {
            end = DateTimeParser.parseIsoTime(constraints.getMaximalInclusive())
                    .toLocalTime();
        } else {
            end = LocalTime.MAX.truncatedTo(ChronoUnit.SECONDS);
        }

        if (initTime.compareTo(start) < 0) {
            initTime = start;
        }

        if (initTime.compareTo(end) > 0) {
            initTime = end;
        }
        return new LocalTimeSpinnerModel(initTime, start, end, ChronoUnit.MINUTES);
    }

    /**
     * Checks if the given XML input contains correct, well-formed XML syntax. To validate against a
     * schema, please see <code> isXmlValid()</code>.
     *
     * @param xml The XML input to check.
     * @return <code>true</code> if valid, otherwise <code>false</code>.
     *
     * @see #isXmlValid
     */
    public static boolean isXmlWellFormed(final InputStream xml) {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);
        try {
            final DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(null); // suppress error prints
            builder.parse(xml);
            return true;
        } catch (IOException | ParserConfigurationException | SAXException ex) {
            log.warn(ex.getMessage());
            return false;
        }
    }

    /**
     * Validates the given XML data against the provided XML Schema Definition (XSD). If just a
     * check on correct XML syntax is needed, please use <code>isXmlWellFormed()</code> instead.
     *
     * @deprecated This code is experimental and is most likely not conform with the intended
     * behavior described in the SiLA 2 standard (v1.0). However, it serves as a useful placeholder
     * and may be rewritten, extended or removed in the future. (2020-09-05,
     * florian.bauer.dev@gmail.com)
     *
     * @param xml The XML input to validate.
     * @param xsd The stream source of the XSD data to validate against.
     *
     * @return <code>true</code> if valid, otherwise <code>false</code>.
     *
     * @see https://www.edankert.com/validate.html
     * @see https://docs.oracle.com/javase/tutorial/jaxp/dom/validating.html
     * @see #isXmlWellFormed
     */
    @Deprecated
    public static boolean isXmlValid(final InputStream xml, final StreamSource xsd) {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);
        try {
            final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            final Schema schema = schemaFactory.newSchema(xsd);
            factory.setSchema(schema);
            final Validator validator = schema.newValidator();
            validator.validate(new StreamSource(xml));
            return true;
        } catch (IOException | SAXException ex) {
            log.warn(ex.getMessage());
            return false;
        }
    }

    /**
     * Checks if the given JSON string is valid.
     *
     * @deprecated A proper schema validation is not done yet. This may change in the future when
     * the standardization of JSON-schema is final. A library to do this can be found here:
     * https://github.com/networknt/json-schema-validator (2020-09-05, florian.bauer.dev@gmail.com)
     *
     * @param jsonStr The JSON string to validate.
     * @return <code>true</code> if valid, otherwise <code>false</code>.
     */
    @Deprecated
    public static boolean isJsonValid(final String jsonStr) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(jsonStr);
            return true;
        } catch (IOException ex) {
            log.warn(ex.getMessage());
            return false;
        }
    }
}
