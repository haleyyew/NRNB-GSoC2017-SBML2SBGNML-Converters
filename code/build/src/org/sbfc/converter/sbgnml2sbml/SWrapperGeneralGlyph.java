package org.sbfc.converter.sbgnml2sbml;

import java.util.HashMap;
import java.util.List;

import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Glyph;
import org.sbml.jsbml.ext.layout.GeneralGlyph;
import org.sbml.jsbml.ext.layout.GraphicalObject;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;

/**
 * SWrapperGeneralGlyph maps SBGN arcs and glyphs to a single SBML GeneralGlyph
 * (case 1) Mapping SBGN Glyph->SBML GeneralGlyph+TextGlyph or
 * (case 2) Mapping SBGN Arc->SBML GeneralGlyph or
 * (case 3) Mapping multiple SBGN Glyphs+Arcs->SBML GeneralGlyph+TextGlyph
 * arc is the single Arc that maps to the GeneralGlyph (case 2)
 * arcs stores all Arcs that maps to the GeneralGlyph (case 3)
 * referenceGlyphs stores all ReferenceGlyphs the GeneralGlyph contains (case 1,2,3)
 * @author haoran
 *
 */
public class SWrapperGeneralGlyph {
	public String id;
	public String clazz;
	public GeneralGlyph generalGlyph;
	TextGlyph textGlyph;
	public Glyph glyph;
	
	//hasParent true means if we have a units of information or state variable
	boolean hasParent;
	GraphicalObject parent;
		
	//glyphIsMissing means if we have a logic arc, then there is only a ReferenceGlyph (no SubGlyphs)
	boolean glyphIsMissing;
	Arc arc;
	String sourceId;
	String targetId;
	
	public HashMap<String, SWrapperArc> arcs = new HashMap<String, SWrapperArc>();
	HashMap<String, ReferenceGlyph> referenceGlyphs = new HashMap<String, ReferenceGlyph>();
	
	//store all the nested glyphs
	List<GraphicalObject> listOfGeneralGlyphs;
	
	SWrapperModel sWrapperModel;
	
	// an Sbgn annotation glyph is converted a GeneralGlyph
	boolean isAnnotation = false;
	Point calloutPoint;
	String calloutTarget;
		
	SWrapperGeneralGlyph(GeneralGlyph generalGlyph, Glyph glyph, GraphicalObject parent, TextGlyph textGlyph,
			SWrapperModel sWrapperModel) {
		this.id = glyph.getId();
		this.generalGlyph = generalGlyph;
		this.clazz = glyph.getClazz();
		this.hasParent = true;
		this.parent = parent;
		this.glyph = glyph;
		this.glyphIsMissing = false;
		this.textGlyph = textGlyph;
		
		this.sWrapperModel = sWrapperModel;
	}
	
	SWrapperGeneralGlyph(GeneralGlyph generalGlyph, Glyph glyph, TextGlyph textGlyph, SWrapperModel sWrapperModel){
		this.id = glyph.getId();
		this.generalGlyph = generalGlyph;
		this.clazz = glyph.getClazz();
		this.hasParent = false;
		this.glyph = glyph;	
		this.glyphIsMissing = false;
		this.textGlyph = textGlyph;
		
		this.sWrapperModel = sWrapperModel;
	}
	
	SWrapperGeneralGlyph(GeneralGlyph generalGlyph, Arc arc, SWrapperModel sWrapperModel) {
		this.id = arc.getId();
		this.generalGlyph = generalGlyph;
		this.clazz = arc.getClazz();
		this.hasParent = false;
		this.glyphIsMissing = true;
		
		this.sWrapperModel = sWrapperModel;
	}
			
	/**
	 * This is a very important function for the SBGN AF to SBML qual converter, it allows us to find all the referenceGlyphs connected to a generalGlyph
	 * @param arcId
	 * @param sWrapperReferenceGlyph
	 * @param arc
	 */
	public void addSpeciesReferenceGlyph(String arcId, SWrapperReferenceGlyph sWrapperReferenceGlyph, SWrapperArc arc){
		this.referenceGlyphs.put(arcId, sWrapperReferenceGlyph.referenceGlyph);
		this.arcs.put(arcId, arc);
		this.sWrapperModel.addSWrapperReferenceGlyph(arcId, sWrapperReferenceGlyph);
	}
	
}
