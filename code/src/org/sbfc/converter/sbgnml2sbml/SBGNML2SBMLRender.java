package org.sbfc.converter.sbgnml2sbml;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.sbfc.converter.sbgnml2sbml.qual.SWrapperQualitativeSpecies;
import org.sbfc.converter.sbml2sbgnml.SBML2SBGNMLUtil;
import org.sbgn.SbgnUtil;
import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Bbox;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Map;
import org.sbgn.bindings.Sbgn;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.GeneralGlyph;
import org.sbml.jsbml.ext.layout.GraphicalObject;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.ReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;
import org.sbml.jsbml.ext.render.ColorDefinition;
import org.sbml.jsbml.ext.render.Ellipse;
import org.sbml.jsbml.ext.render.FontFamily;
import org.sbml.jsbml.ext.render.GraphicalPrimitive2D.FillRule;
import org.sbml.jsbml.ext.render.HTextAnchor;
import org.sbml.jsbml.ext.render.Image;
import org.sbml.jsbml.ext.render.LineEnding;
import org.sbml.jsbml.ext.render.ListOfLocalRenderInformation;
import org.sbml.jsbml.ext.render.LocalRenderInformation;
import org.sbml.jsbml.ext.render.LocalStyle;
import org.sbml.jsbml.ext.render.Polygon;
import org.sbml.jsbml.ext.render.Rectangle;
import org.sbml.jsbml.ext.render.RenderConstants;
import org.sbml.jsbml.ext.render.RenderGraphicalObjectPlugin;
import org.sbml.jsbml.ext.render.RenderGroup;
import org.sbml.jsbml.ext.render.RenderLayoutPlugin;
import org.sbml.jsbml.ext.render.RenderPoint;
import org.sbml.jsbml.ext.render.Text;
import org.sbml.jsbml.ext.render.VTextAnchor;

/**
 * SBGNML2SBMLRender contains methods to create the RenderInformation element of the Model.
 * @author haoran
 *
 */
public class SBGNML2SBMLRender {
//	SWrapperModel sWrapperModel;
//	SBGNML2SBMLOutput sOutput;
//	SBGNML2SBMLUtil sUtil;
	
	public SBGNML2SBMLRender(SWrapperModel sWrapperModel, SBGNML2SBMLOutput sOutput, SBGNML2SBMLUtil sUtil) {
//		this.sWrapperModel = sWrapperModel;
//		this.sOutput = sOutput;
//		this.sUtil = sUtil;
	}
	
	public static void renderGeneralGlyphs(SWrapperModel sWrapperModel, SBGNML2SBMLOutput sOutput) {
		SWrapperGeneralGlyph sWrapperGeneralGlyph;
		
		for (String key : sWrapperModel.listOfWrapperGeneralGlyphs.keySet()){
			sWrapperGeneralGlyph = sWrapperModel.getWrapperGeneralGlyph(key);
			
			// units of information in SBGN AF has different shapes
			String auxiliaryClazz = "";
			if (sWrapperGeneralGlyph.glyph != null && sWrapperGeneralGlyph.glyph.getEntity() != null){
				Glyph.Entity entity = sWrapperGeneralGlyph.glyph.getEntity();
				auxiliaryClazz = entity.getName();
			}
			LocalStyle localStyle = createStyle(sWrapperGeneralGlyph.generalGlyph, sWrapperGeneralGlyph.clazz+auxiliaryClazz, sOutput);
			
			// annotation glyphs has an extra callout glyph
			if (sWrapperGeneralGlyph.isAnnotation) {
				Dimensions dim = sWrapperGeneralGlyph.generalGlyph.getBoundingBox().getDimensions();
				Point startPoint = sWrapperGeneralGlyph.calloutPoint;
				Point bbPosition = sWrapperGeneralGlyph.generalGlyph.getBoundingBox().getPosition();
				Point endPoint1 = new Point(bbPosition.getX()+0.3*dim.getWidth(), bbPosition.getY()+dim.getHeight());
				Point endPoint2 = new Point(bbPosition.getX()+0.4*dim.getWidth(),bbPosition.getY()+dim.getHeight());
				createTriangle(localStyle, startPoint, endPoint1, endPoint2);
			}
			
			renderReferenceGlyphs(sWrapperGeneralGlyph, sOutput);
		}
		
		for (String key : sWrapperModel.listOfWrapperReferenceGlyphs.keySet()){
			SWrapperReferenceGlyph sWrapperReferenceGlyph = sWrapperModel.listOfWrapperReferenceGlyphs.get(key);
			
			LocalStyle localStyle = createStyle(sWrapperReferenceGlyph.referenceGlyph, sWrapperReferenceGlyph.arc.getClazz(), sOutput);
		}
	}	
		
	public static void renderCompartmentGlyphs(SWrapperModel sWrapperModel, SBGNML2SBMLOutput sOutput) {
		SWrapperCompartmentGlyph sWrapperCompartmentGlyph;
		
		// sorting is not needed
		sWrapperModel.sortCompartmentOrderList();
		for (String key : sWrapperModel.compartmentOrderList.keySet()){
			sWrapperCompartmentGlyph = sWrapperModel.getWrapperCompartmentGlyph(key);
			createStyle(sWrapperCompartmentGlyph.compartmentGlyph, sWrapperCompartmentGlyph.clazz, sOutput);

		}			
	}
	
	public static void renderSpeciesGlyphs(SWrapperModel sWrapperModel, SBGNML2SBMLOutput sOutput) {
		SWrapperSpeciesGlyph sWrapperSpeciesGlyph;
		for (String key : sWrapperModel.listOfWrapperSpeciesGlyphs.keySet()){
			sWrapperSpeciesGlyph = sWrapperModel.getWrapperSpeciesGlyph(key);
			LocalStyle localStyle = createStyle(sWrapperSpeciesGlyph.speciesGlyph, sWrapperSpeciesGlyph.clazz, sOutput);
			
			if (sWrapperSpeciesGlyph.hasClone){
				// TODO: broken, need to fix
				//addCloneText(localStyle, sWrapperSpeciesGlyph.speciesGlyph, sWrapperSpeciesGlyph.cloneText);
			}
		}		
		SWrapperQualitativeSpecies sWrapperQualitativeSpecies;
		for (String key : sWrapperModel.listOfSWrapperQualitativeSpecies.keySet()){
			sWrapperQualitativeSpecies = sWrapperModel.getSWrapperQualitativeSpecies(key);
			createStyle(sWrapperQualitativeSpecies.speciesGlyph, sWrapperQualitativeSpecies.clazz, sOutput);
		}	
	}
	
	public static void renderTextGlyphs(SWrapperModel sWrapperModel, SBGNML2SBMLOutput sOutput) {
		
		for (TextGlyph tg : sOutput.layout.getListOfTextGlyphs()){
			if (sWrapperModel.getTextSourceMap().containsKey(tg.getId())){
				//LocalStyle localStyle = createStyle(tg, "top_text");
				LocalStyle localStyle = createStyle(tg, "text", sOutput);
			} else {
				LocalStyle localStyle = createStyle(tg, "text", sOutput);
			}
		}
	}
		
	public static void renderReactionGlyphs(SWrapperModel sWrapperModel, SBGNML2SBMLOutput sOutput) {
		SWrapperReactionGlyph sWrapperReactionGlyph;
		for (String key : sWrapperModel.listOfWrapperReactionGlyphs.keySet()){
			sWrapperReactionGlyph = sWrapperModel.getWrapperReactionGlyph(key);
			createStyle(sWrapperReactionGlyph.reactionGlyph, sWrapperReactionGlyph.clazz, sOutput);
			renderSpeciesReferenceGlyphs(sWrapperReactionGlyph, sOutput);
			
			if (sWrapperReactionGlyph.clazz.equals("uncertain process")){
				Bbox labelBbox = null;

				TextGlyph tg = SBGNML2SBMLUtil.createJsbmlTextGlyph(sWrapperReactionGlyph.reactionGlyph, "?", labelBbox);
				sOutput.addTextGlyph(tg);
			}
		}		
	}
	
	public static void renderSpeciesReferenceGlyphs(SWrapperReactionGlyph sWrapperReactionGlyph, SBGNML2SBMLOutput sOutput) {
		Arc arc;
		SpeciesReferenceGlyph speciesReferenceGlyph;

		for (String arcKey : sWrapperReactionGlyph.speciesReferenceGlyphs.keySet()){
			speciesReferenceGlyph = sWrapperReactionGlyph.speciesReferenceGlyphs.get(arcKey);
			arc = sWrapperReactionGlyph.getArc(arcKey);

			createStyle((GraphicalObject) speciesReferenceGlyph, arc, sOutput);
		}		
	}
	
	public static void renderReferenceGlyphs(SWrapperGeneralGlyph sWrapperGeneralGlyph, SBGNML2SBMLOutput sOutput) {
		Arc arc;
		ReferenceGlyph referenceGlyph;
		for (String arcKey : sWrapperGeneralGlyph.referenceGlyphs.keySet()){
			referenceGlyph = sWrapperGeneralGlyph.referenceGlyphs.get(arcKey);
			arc = sWrapperGeneralGlyph.arcs.get(arcKey).arc;
			
			createStyle((GraphicalObject) referenceGlyph, arc, sOutput);
		}		
	}
	
	public static LocalStyle createStyle(GraphicalObject graphicalObject, String clazz, SBGNML2SBMLOutput sOutput) {
		RenderGroup renderGroup;
		RenderGraphicalObjectPlugin renderGraphicalObjectPlugin;
		LocalRenderInformation localRenderInformation = sOutput.localRenderInformation;
		Layout layout = sOutput.layout;
		
		String styleId = findObjectRole(clazz, sOutput);

		renderGraphicalObjectPlugin = (RenderGraphicalObjectPlugin) graphicalObject.getPlugin(RenderConstants.shortLabel);
		renderGraphicalObjectPlugin.setObjectRole(styleId);		

		for (LocalStyle localStyle: sOutput.listOfStyles_temp){
			if (localStyle.getId().equals(styleId)){
				return localStyle;
			}
		}

		return null;
	}
	
	public static void createStyle(GraphicalObject graphicalObject, Arc arc, SBGNML2SBMLOutput sOutput) {
		RenderGroup renderGroup;
		LocalStyle localStyle;
		RenderGraphicalObjectPlugin renderGraphicalObjectPlugin;
		LocalRenderInformation localRenderInformation = sOutput.localRenderInformation;
		Layout layout = sOutput.layout;
		
		String styleId = findObjectRole(arc.getClazz(), sOutput);
		
		renderGraphicalObjectPlugin = (RenderGraphicalObjectPlugin) graphicalObject.getPlugin(RenderConstants.shortLabel);
		renderGraphicalObjectPlugin.setObjectRole(styleId);		
	}	

	public static String findObjectRole(String clazz, SBGNML2SBMLOutput sOutput){
		String styleId = null;
		
		// TODO: horizontal or vertical orientation
		if (clazz.equals("or")){
			styleId = "or";
		} else if (clazz.equals("and")){
			styleId = "and";
		} else if (clazz.equals("not")){
			styleId = "not";
		} 
		
		else if (clazz.equals("process")){
			styleId = "SBO0000167";
		} else if (clazz.equals("association")){
			styleId = "SBO0000177";
		} else if (clazz.equals("dissociation")){
			styleId = "SBO0000180";
		} else if (clazz.equals("omitted process")){
			styleId = "SBO0000397";
		} else if (clazz.equals("uncertain process")){
			styleId = "SBO0000396";
		}
		
		else if (clazz.equals("macromolecule")){
			styleId = "SBO0000245";
		} else if (clazz.equals("simple chemical")){
			styleId = "SBO0000247";
		} else if (clazz.equals("source and sink")){
			styleId = "SBO0000291";
		} else if (clazz.equals("nucleic acid feature")){
			styleId = "SBO0000354";
		} else if (clazz.equals("complex")){
			styleId = "SBO0000253";
		} else if (clazz.equals("perturbing agent")){
			styleId = "SBO0000405";
		} else if (clazz.equals("perturbation")){
				styleId = "perturbation";
		} else if (clazz.equals("unspecified entity")){
			styleId = "SBO0000285";
		}
		
		else if (clazz.equals("biological activity")){
			styleId = "SBO0000412";
		} else if (clazz.equals("phenotype")){
			styleId = "SBO0000358";
		} else if (clazz.equals("annotation")){
			styleId = "unitofinfo";
		} else if (clazz.equals("delay")){
			styleId = "SBO0000225";
		}
		
		else if (clazz.equals("simple chemical_clone")){
			styleId = "SBO0000247clone";
		} else if (clazz.equals("macromolecule_clone")){
			styleId = "SBO0000245clone";
		} else if (clazz.equals("nucleic acid feature_clone")){
			styleId = "SBO0000354clone";
		} 
		
		else if (clazz.equals("macromolecule multimer_clone")){
			styleId = "SBO0000245multimerclone";
		} else if (clazz.equals("simple chemical multimer_clone")){
			styleId = "SBO0000247multimerclone";
		} else if (clazz.equals("nucleic acid feature multimer_clone")){
			styleId = "SBO0000354multimerclone";
		} 
		
		else if (clazz.equals("simple chemical multimer")){
			styleId = "SBO0000247multimer";
		} else if (clazz.equals("macromolecule multimer")){
			styleId = "SBO0000245multimer";
		} else if (clazz.equals("nucleic acid feature multimer")){
			styleId = "SBO0000354multimer";
		} else if (clazz.equals("complex multimer")){
			styleId = "SBO0000253multimer";
		}
		
		else if (clazz.equals("unit of information")){
			styleId = "unitofinfo";
		} else if (clazz.equals("cardinality")){
			styleId = "unitofinfo";
		} else if (clazz.equals("state variable")){
			styleId = "statevar";
		} else if (clazz.equals("unit of informationmacromolecule")){
			styleId = "unitofinfoSBO0000245";
		} else if (clazz.equals("unit of informationnucleic acid feature")){
			styleId = "unitofinfoSBO0000354";
		} else if (clazz.equals("unit of informationsimple chemical")){
			styleId = "unitofinfoSBO0000247";
		} else if (clazz.equals("unit of informationunspecified entity")){
			styleId = "unitofinfoSBO0000285";
		} else if (clazz.equals("unit of informationcomplex")){
			styleId = "unitofinfoSBO0000253";
		} else if (clazz.equals("unit of informationperturbation")){
			styleId = "unitofinfoSBO0000405";
		}
		
		else if (clazz.equals("compartment")){
			styleId = "SBO0000289";
		} else if (clazz.equals("submap")){
			styleId = "SBO0000395";
		} 
		
		else if (clazz.equals("tag_left")){
			styleId = "Tagleft";
		} else if (clazz.equals("tag_right")){
			styleId = "Tagright";
		} else if (clazz.equals("tag_up")){
			styleId = "Tagup";
		} else if (clazz.equals("tag_down")){
			styleId = "Tagdown";
		}
		
		else if (clazz.equals("terminal_left")){
			styleId = "Tagleft";
		} else if (clazz.equals("terminal_right")){
			styleId = "Tagright";
		} else if (clazz.equals("terminal_up")){
			styleId = "Tagup";
		} else if (clazz.equals("terminal_down")){
			styleId = "Tagdown";
		}		
		
		if (clazz.equals("catalysis")){
			sOutput.lineEndingsInModel.put("activator", null);
			styleId = "SBO0000172";
		} else if (clazz.equals("production")){
			sOutput.lineEndingsInModel.put("product", null);
			styleId = "product";
		} else if (clazz.equals("consumption")){	
			styleId = "substrate";
		} else if (clazz.equals("stimulation")){
			sOutput.lineEndingsInModel.put("stimulation", null);
			styleId = "SBO0000170";
		} else if (clazz.equals("positive influence")){
			sOutput.lineEndingsInModel.put("stimulation", null);
			styleId = "SBO0000170";
		} else if (clazz.equals("necessary stimulation")){
			sOutput.lineEndingsInModel.put("necessary_stimulation", null);
			styleId = "SBO0000171";
		} else if (clazz.equals("unknown influence")){
			sOutput.lineEndingsInModel.put("modulation", null);
			styleId = "SBO0000168";
		} else if (clazz.equals("inhibition")){
			sOutput.lineEndingsInModel.put("inhibitor", null);
			styleId = "SBO0000169";
		} else if (clazz.equals("negative influence")){
			sOutput.lineEndingsInModel.put("inhibitor", null);
			styleId = "SBO0000169";
		} else if (clazz.equals("modulation")){
			sOutput.lineEndingsInModel.put("modulation", null);
			styleId = "SBO0000168";
		}
		
		else if (clazz.equals("equivalence arc")){
			styleId = "equivalence";
		} else if (clazz.equals("logic arc")){
			styleId = "SBO0000398";
		}

		else if (clazz.equals("text")){
			styleId = "defaultText";
		} else if (clazz.equals("top_text")){
			styleId = "topText";
		}

		sOutput.stylesInModel.put(styleId, null);

		return styleId;
	}
	
	public static void createTriangle(LocalStyle localStyle, Point p1, Point p2, Point p3){
		Polygon polygon = new Polygon();
		ListOf<RenderPoint> elements = polygon.getListOfElements();
				
		elements.add(createRenderPoint(p1.getX(), p1.getY(), true, true));
		elements.add(createRenderPoint(p2.getX(), p2.getY(), true, true));
		elements.add(createRenderPoint(p3.getX(), p3.getY(), true, true));
		
		RenderGroup renderGroup = localStyle.getGroup();
		renderGroup.addElement(polygon);
	}
	
	public static RenderPoint createRenderPoint(double x, double y, boolean absoluteX, boolean absoluteY){
		RenderPoint renderPoint = new RenderPoint();
		renderPoint.setX(x);
		renderPoint.setY(y);
		renderPoint.setAbsoluteX(absoluteX);
		renderPoint.setAbsoluteY(absoluteY);	
		
		return renderPoint;
	}
	
	public static Text createText(double x, double y, 
			boolean absoluteX, boolean absoluteY) {
		Text text = new Text();
		
		text.setAbsoluteX(absoluteX);
		text.setAbsoluteY(absoluteY);
		text.setX(x);
		text.setY(y);	
			
		return text;
	}
	
	/**
	 * Add text to the clone marker.
	 * TODO: broken, need to fix
	 * @param localStyle
	 * @param speciesGlyph
	 * @param cloneText
	 */
	public static void addCloneText(LocalStyle localStyle, SpeciesGlyph speciesGlyph, String cloneText) {
		Text text = createText(0, 80, false, false);
		text.setFontFamily(FontFamily.MONOSPACE);
		text.setTextAnchor(HTextAnchor.MIDDLE);
		text.setVTextAnchor(VTextAnchor.BOTTOM);	
		text.setName(cloneText);	
		
		RenderGroup renderGroup = localStyle.getGroup();
		renderGroup.setStroke("white");
		renderGroup.addElement(text);
	}
	
	public static Rectangle createRectangle(double x, double y, 
			double width, double height, 
			double rx, double ry, 
			boolean absoluteX, boolean absoluteY, 
			boolean absoluteRx, boolean absoluteRy,
			boolean absoluteW, boolean absoluteH) {
		Rectangle rectangle = new Rectangle();
		
		rectangle.setHeight(height);
		rectangle.setWidth(width);
		rectangle.setX(x);
		rectangle.setY(y);
		rectangle.setRx(rx);
		rectangle.setRy(ry);
		rectangle.setAbsoluteX(absoluteX);
		rectangle.setAbsoluteY(absoluteY);		
		rectangle.setAbsoluteRx(absoluteRx);
		rectangle.setAbsoluteRy(absoluteRy);		
		rectangle.setAbsoluteWidth(absoluteW);
		rectangle.setAbsoluteHeight(absoluteH);
		
		return rectangle;
	}
	
	public static Ellipse createEllipse(double cx, double cy, double rx,
			boolean absoluteCx, boolean absoluteCy, boolean absoluteRx) {
		Ellipse ellipse = new Ellipse(3, 1);
		
		ellipse.setRx(rx);
		ellipse.setCx(cx);
		ellipse.setCy(cy);
		ellipse.setAbsoluteCx(absoluteCx);
		ellipse.setAbsoluteCy(absoluteCy);	
		ellipse.setAbsoluteRx(absoluteRx);	
		
		ellipse.setFill("white");
		
		return ellipse;
	}
	
	public static void createColourDefinitions(SBGNML2SBMLOutput sOutput) {
		LocalRenderInformation localRenderInformation = sOutput.localRenderInformation;
		Layout layout = sOutput.layout;
		
		ColorDefinition colorDefinition;
		colorDefinition = new ColorDefinition(layout.getLevel(), layout.getVersion());
		colorDefinition.setId("white");
		colorDefinition.setValue(Color.decode("#FFFFFF"));		
		localRenderInformation.addColorDefinition(colorDefinition);		
		
		colorDefinition = new ColorDefinition(layout.getLevel(), layout.getVersion());
		colorDefinition.setId("black");
		colorDefinition.setValue(Color.decode("#000000"));		
		localRenderInformation.addColorDefinition(colorDefinition);		
	}
	
	public static void initializeDefaultRenderGroup(RenderGroup renderGroup) {
		renderGroup.setStroke("black");
		renderGroup.setStrokeWidth(3);
		renderGroup.setFillRule(FillRule.NONZERO);
		renderGroup.setFontSize((short) 12);
		renderGroup.setFontFamily(FontFamily.SANS_SERIF);
		renderGroup.setFontStyleItalic(false);
		renderGroup.setFontWeightBold(false);
		renderGroup.setTextAnchor(HTextAnchor.MIDDLE);
		renderGroup.setVTextAnchor(VTextAnchor.MIDDLE);
	}	
	
	/**
	 * For debugging
	 */
	public void display(SBGNML2SBMLOutput sOutput) {
		for (LineEnding lineEnding: sOutput.listOfLineEndings) {
			System.out.println("[renderGeneralGlyphs] lineEnding "+lineEnding.getId());
		}		
	}




}
