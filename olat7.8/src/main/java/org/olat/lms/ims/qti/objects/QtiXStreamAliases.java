package org.olat.lms.ims.qti.objects;

import com.thoughtworks.xstream.XStream;
import org.olat.data.commons.xml.XStreamHelper;

/**
 * Helper class for qti related aliases.
 * 
 * Initial Date: 01.06.2012 <br>
 * 
 * @author lavinia
 */
public class QtiXStreamAliases {

    /**
     * Used for reading editortreemodel.xml and runstructure.xml. Creates a new XStream with the aliases used in the mentioned xml files.
     * 
     * @return
     */
    public static XStream getAliasedXStream() {
        XStream xstream = XStreamHelper.createXStreamInstance();

        xstream.alias("org.olat.ims.qti.editor.beecom.objects.Assessment", Assessment.class);
        xstream.alias("org.olat.ims.qti.editor.beecom.objects.ChoiceQuestion", ChoiceQuestion.class);
        xstream.alias("org.olat.ims.qti.editor.beecom.objects.ChoiceResponse", ChoiceResponse.class);
        xstream.alias("org.olat.ims.qti.editor.beecom.objects.Control", Control.class);
        xstream.alias("org.olat.ims.qti.editor.beecom.objects.Duration", Duration.class);
        xstream.alias("org.olat.ims.qti.editor.beecom.objects.EssayQuestion", EssayQuestion.class);
        xstream.alias("org.olat.ims.qti.editor.beecom.objects.EssayResponse", EssayResponse.class);
        xstream.alias("org.olat.ims.qti.editor.beecom.objects.Feedback", Feedback.class);
        xstream.alias("org.olat.ims.qti.editor.beecom.objects.FIBQuestion", FIBQuestion.class);
        xstream.alias("org.olat.ims.qti.editor.beecom.objects.FIBResponse", FIBResponse.class);
        xstream.alias("org.olat.ims.qti.editor.beecom.objects.Item", Item.class);
        xstream.alias("org.olat.ims.qti.editor.beecom.objects.Matbreak", Matbreak.class);
        xstream.alias("org.olat.ims.qti.editor.beecom.objects.MatElement", MatElement.class);
        xstream.alias("org.olat.ims.qti.editor.beecom.objects.Material", Material.class);
        xstream.alias("org.olat.ims.qti.editor.beecom.objects.Matimage", Matimage.class);
        xstream.alias("org.olat.ims.qti.editor.beecom.objects.Mattext", Mattext.class);
        xstream.alias("org.olat.ims.qti.editor.beecom.objects.Matvideo", Matvideo.class);
        xstream.alias("org.olat.ims.qti.editor.beecom.objects.Metadata", Metadata.class);
        xstream.alias("org.olat.ims.qti.editor.beecom.objects.OutcomesProcessing", OutcomesProcessing.class);
        xstream.alias("org.olat.ims.qti.editor.beecom.objects.QTIDocument", QTIDocument.class);
        xstream.alias("org.olat.ims.qti.editor.beecom.objects.QTIObject", QTIObject.class);// needed ?
        xstream.alias("org.olat.ims.qti.editor.beecom.objects.QTIXMLWrapper", QTIXMLWrapper.class);// needed ?

        xstream.alias("org.olat.ims.qti.editor.beecom.objects.Question", Question.class);
        xstream.alias("org.olat.ims.qti.editor.beecom.objects.Response", Response.class);
        xstream.alias("org.olat.ims.qti.editor.beecom.objects.Section", Section.class);
        xstream.alias("org.olat.ims.qti.editor.beecom.objects.SelectionOrdering", SelectionOrdering.class);

        return xstream;
    }
}
